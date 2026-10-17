package net.frcp.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Drawing: whole rounds, continuous freshness, and the fairness of the next draw picked ahead of time.
class DrawTest {
    private val most = 1000 // a freshness setting at or above half the pool means "the most", as in Settings

    /** Draws n civs as the app does, asking the picker for the next draw each time. */
    private fun play(s: PickerState, f: Freshness, rng: MersenneTwister, n: Int): List<Int> {
        val picker = Picker(rng)
        return List(n) {
            val id = picker.next(logic.drawCandidates(s, f))!!
            logic.recordDraw(s, id)
            id
        }
    }

    /** How often each of the ids comes out of trials runs of pick(). */
    private fun countPicks(ids: IntRange, trials: Int, pick: () -> Int?): Map<Int, Int> {
        val counts = ids.associateWithTo(mutableMapOf()) { 0 }
        repeat(trials) {
            val id = pick()
            assertTrue("picked $id, which isn't one of the civs", id in counts)
            counts[id!!] = counts.getValue(id) + 1
        }
        return counts
    }

    private fun assertEven(counts: Map<Int, Int>) {
        val df = counts.size - 1
        val chi = chiSquare(counts)
        assertTrue("uneven picks (chi-square $chi for $df degrees of freedom): $counts", chi < chiSquareLimit(df))
    }

    private val twoCivs get() = makeState(edition = "AOK", off = CIVS.filter { it.name !in listOf("Britons", "Celts") }.map { it.name })

    @Test
    fun `the Mersenne Twister gives the reference outputs of mt19937ar_c`() {
        assertEquals(3499211612L, MersenneTwister(5489).nextUint32())
        val mt = MersenneTwister(intArrayOf(0x123, 0x234, 0x345, 0x456))
        assertEquals(listOf(1067595299L, 955945823L, 477289528L, 4107218783L, 4228976476L), List(5) { mt.nextUint32() })
    }

    @Test
    fun `the Mersenne Twister's integers stay in range and refuse bad ranges`() {
        val rng = seeded()
        repeat(1000) { assertTrue(rng.int(3, 7) in 3..7) }
        assertEquals(4, rng.int(4, 4))
        assertTrue(runCatching { rng.int(5, 4) }.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `every civ in the pool comes up once a round, before any repeats`() {
        val pools = listOf(makeState(), makeState(edition = "HD"), makeState(off = CIVS.filter { it.id % 3 == 0 }.map { it.name }))
        val settings = listOf(Freshness(true, most), Freshness(true, 5), Freshness(false, most))
        for (start in pools) {
            for (f in settings) {
                val s = start.deepCopy()
                val pool = logic.poolIds(s)
                for (r in play(s, f, seeded(), pool.size * 12).chunked(pool.size)) {
                    assertEquals("a round of ${pool.size} with $f", pool, r.sorted())
                }
            }
        }
    }

    @Test
    fun `continuous freshness keeps the newest civs out of the next round's first draws`() {
        for (freshN in listOf(1, 5, most)) {
            val s = makeState()
            val size = logic.poolIds(s).size
            val n = minOf(freshN, logic.freshMax(size))
            val draws = play(s, Freshness(true, freshN), seeded(), size * 30)
            for (start in size until draws.size step size) {
                // The j-th draw of a round still has the last n - j civs of the round before sitting out.
                for (j in 0 until n) {
                    val sittingOut = draws.subList(start - (n - j), start)
                    assertFalse("freshness $freshN: draw ${j + 1} of a round repeats a civ from the last round's last ${n - j}", draws[start + j] in sittingOut)
                }
            }
        }
    }

    @Test
    fun `without continuous freshness, a round can start with the civ the last one ended on`() {
        val draws = play(twoCivs, Freshness(false, most), seeded(), 400)
        assertTrue(draws.zipWithNext().any { (a, b) -> a == b })
    }

    @Test
    fun `with continuous freshness, a pool of two civs takes turns`() {
        val draws = play(twoCivs, Freshness(true, most), seeded(), 400)
        assertTrue(draws.zipWithNext().all { (a, b) -> a != b })
    }

    @Test
    fun `a pool of one civ draws it every time, and an empty pool has nothing to draw`() {
        val one = makeState(off = CIVS.filter { it.name != "Celts" }.map { it.name })
        assertTrue(play(one, Freshness(true, most), seeded(), 5).all { CIVS[it].name == "Celts" })
        val none = makeState(off = CIVS.map { it.name })
        assertEquals(emptyList<Int>(), logic.drawCandidates(none, Freshness(true, most)))
        assertNull(Picker(seeded()).next(emptyList()))
    }

    @Test
    fun `without a random source there's no pick`() {
        assertNull(Picker(null).next(listOf(1, 2, 3)))
    }

    @Test
    fun `the next draw is an even pick from the civs it can be`() {
        val rng = seeded()
        assertEven(countPicks(0..11, 24000) { Picker(rng).next((0..11).toList()) })
    }

    @Test
    fun `the next draw stays an even pick when civs join`() {
        val rng = seeded()
        assertEven(countPicks(0..14, 30000) {
            val picker = Picker(rng)
            picker.next((0..9).toList())
            picker.next((0..14).toList())
        })
    }

    @Test
    fun `the next draw stays an even pick when civs drop out`() {
        val rng = seeded()
        assertEven(countPicks(0..9, 20000) {
            val picker = Picker(rng)
            picker.next((0..14).toList())
            picker.next((0..9).toList())
        })
    }

    @Test
    fun `the next draw stays an even pick when civs join and others drop out`() {
        val rng = seeded()
        assertEven(countPicks(5..14, 20000) {
            val picker = Picker(rng)
            picker.next((0..9).toList())
            picker.next((5..14).toList())
        })
    }

    @Test
    fun `the next draw stays put while it can, so its jingle only loads once`() {
        val rng = seeded()
        repeat(200) {
            val picker = Picker(rng)
            val from = (0..19).toList()
            val id = picker.next(from)!!
            assertEquals("the same civs in another order", id, picker.next(from.reversed()))
            assertEquals("other civs dropping out", id, picker.next(from.filter { it == id || it % 2 == id % 2 }))
        }
    }

    @Test
    fun `after a draw, the next one is picked again from the civs left`() {
        val rng = seeded()
        repeat(200) {
            val picker = Picker(rng)
            val from = (0..19).toList()
            val drawn = picker.next(from)
            val left = from.filter { it != drawn }
            assertTrue(picker.next(left) in left)
        }
    }
}
