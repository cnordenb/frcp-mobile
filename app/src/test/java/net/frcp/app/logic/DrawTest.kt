package net.frcp.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DrawTest {
    private val most = 1000
    private val twenty = (0..19).toList()

    private fun play(s: PickerState, f: Freshness, rng: MersenneTwister, n: Int): List<Int> {
        val picker = Picker(rng)
        return List(n) {
            val id = picker.next(logic.drawCandidates(s, f))!!
            logic.recordDraw(s, id)
            id
        }
    }

    private fun twoCivs() = makeState(edition = "AOK", off = allBut("Britons", "Celts"))

    private fun brokenRounds(start: PickerState, f: Freshness): List<String> {
        val s = start.deepCopy()
        val pool = logic.poolIds(s)
        val rounds = play(s, f, seeded(), pool.size * 12).chunked(pool.size)
        return rounds.indices.filter { rounds[it].sorted() != pool }.map { "round ${it + 1} of ${pool.size} with $f" }
    }

    private fun staleDraws(freshN: Int): List<String> {
        val s = makeState()
        val size = logic.poolIds(s).size
        val n = minOf(freshN, logic.freshMax(size))
        val draws = play(s, Freshness(true, freshN), seeded(), size * 30)
        return (size until draws.size step size).flatMap { start ->
            (0 until n).filter { j -> draws[start + j] in draws.subList(start - (n - j), start) }
                .map { j -> "freshness $freshN: draw ${j + 1} of the round starting at $start" }
        }
    }

    private fun picks(trials: Int, then: (Picker) -> Int?): List<Int?> {
        val rng = seeded()
        return List(trials) { then(Picker(rng)) }
    }

    private fun joining(picker: Picker): Int? {
        picker.next((0..9).toList())
        return picker.next((0..14).toList())
    }

    private fun dropping(picker: Picker): Int? {
        picker.next((0..14).toList())
        return picker.next((0..9).toList())
    }

    private fun swapping(picker: Picker): Int? {
        picker.next((0..9).toList())
        return picker.next((5..14).toList())
    }

    private fun strays(picked: List<Int?>, ids: IntRange) = picked.filter { it == null || it !in ids }.distinct()

    private fun chiSquareOf(picked: List<Int?>, ids: IntRange) =
        chiSquare(ids.associateWith { id -> picked.count { it == id } })

    private fun unevenness(picked: List<Int?>, ids: IntRange) =
        chiSquareOf(picked, ids) / chiSquareLimit(ids.count() - 1)

    private fun movedPicks(change: (List<Int>, Int) -> List<Int>): Int {
        val rng = seeded()
        return (1..200).count {
            val picker = Picker(rng)
            val id = picker.next(twenty)!!
            picker.next(change(twenty, id)) != id
        }
    }

    @Test
    fun t047_mersenneTwisterGivesTheReferenceOutputForASeed() {
        assertEquals(3499211612L, MersenneTwister(5489).nextUint32())
    }

    @Test
    fun t048_mersenneTwisterGivesTheReferenceOutputsForASeedArray() {
        val mt = MersenneTwister(intArrayOf(0x123, 0x234, 0x345, 0x456))
        val reference = listOf(1067595299L, 955945823L, 477289528L, 4107218783L, 4228976476L)
        assertEquals(reference, List(5) { mt.nextUint32() })
    }

    @Test
    fun t049_mersenneTwistersIntegersStayInRange() {
        val rng = seeded()
        assertEquals(emptyList<Int>(), List(1000) { rng.int(3, 7) }.filter { it !in 3..7 })
    }

    @Test
    fun t050_mersenneTwistersRangeOfOneGivesThatNumber() {
        assertEquals(4, seeded().int(4, 4))
    }

    @Test
    fun t051_mersenneTwisterRefusesARangeTurnedAround() {
        assertTrue(runCatching { seeded().int(5, 4) }.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun t052_everyCivInThePoolComesUpOnceARound() {
        val thirdOff = makeState(off = CIVS.filter { it.id % 3 == 0 }.map { it.name })
        val pools = listOf(makeState(), makeState(edition = "HD"), thirdOff)
        val settings = listOf(Freshness(true, most), Freshness(true, 5), Freshness(false, most))
        val broken = pools.flatMap { start -> settings.flatMap { brokenRounds(start, it) } }
        assertEquals(emptyList<String>(), broken)
    }

    @Test
    fun t053_freshnessKeepsTheNewestCivsOutOfTheNextRoundsFirstDraws() {
        assertEquals(emptyList<String>(), listOf(1, 5, most).flatMap(::staleDraws))
    }

    @Test
    fun t054_withoutFreshnessARoundCanStartWithTheLastCiv() {
        val draws = play(twoCivs(), Freshness(false, most), seeded(), 400)
        assertTrue(draws.zipWithNext().any { (a, b) -> a == b })
    }

    @Test
    fun t055_withFreshnessAPoolOfTwoCivsTakesTurns() {
        val draws = play(twoCivs(), Freshness(true, most), seeded(), 400)
        assertTrue(draws.zipWithNext().all { (a, b) -> a != b })
    }

    @Test
    fun t056_poolOfOneCivDrawsItEveryTime() {
        val one = makeState(off = allBut("Celts"))
        assertEquals(List(5) { "Celts" }, namesOf(play(one, Freshness(true, most), seeded(), 5)))
    }

    @Test
    fun t057_emptyPoolHasNothingToDraw() {
        val none = makeState(off = CIVS.map { it.name })
        assertEquals(emptyList<Int>(), logic.drawCandidates(none, Freshness(true, most)))
    }

    @Test
    fun t058_nothingToPickFromGivesNoPick() {
        assertNull(Picker(seeded()).next(emptyList()))
    }

    @Test
    fun t059_withoutARandomSourceThereIsNoPick() {
        assertNull(Picker(null).next(listOf(1, 2, 3)))
    }

    @Test
    fun t060_nextDrawIsOneOfTheCivsItCanBe() {
        assertEquals(emptyList<Int?>(), strays(picks(24000) { it.next((0..11).toList()) }, 0..11))
    }

    @Test
    fun t061_nextDrawIsAnEvenPick() {
        assertTrue("uneven picks", unevenness(picks(24000) { it.next((0..11).toList()) }, 0..11) < 1.0)
    }

    @Test
    fun t062_nextDrawIsOneOfTheCivsWhenCivsJoin() {
        assertEquals(emptyList<Int?>(), strays(picks(30000, ::joining), 0..14))
    }

    @Test
    fun t063_nextDrawStaysAnEvenPickWhenCivsJoin() {
        assertTrue("uneven picks", unevenness(picks(30000, ::joining), 0..14) < 1.0)
    }

    @Test
    fun t064_nextDrawIsOneOfTheCivsLeftWhenCivsDropOut() {
        assertEquals(emptyList<Int?>(), strays(picks(20000, ::dropping), 0..9))
    }

    @Test
    fun t065_nextDrawStaysAnEvenPickWhenCivsDropOut() {
        assertTrue("uneven picks", unevenness(picks(20000, ::dropping), 0..9) < 1.0)
    }

    @Test
    fun t066_nextDrawIsOneOfTheNewCivsWhenCivsJoinAndOthersDropOut() {
        assertEquals(emptyList<Int?>(), strays(picks(20000, ::swapping), 5..14))
    }

    @Test
    fun t067_nextDrawStaysAnEvenPickWhenCivsJoinAndOthersDropOut() {
        assertTrue("uneven picks", unevenness(picks(20000, ::swapping), 5..14) < 1.0)
    }

    @Test
    fun t068_nextDrawStaysPutWhenTheCivsComeInAnotherOrder() {
        assertEquals(0, movedPicks { from, _ -> from.reversed() })
    }

    @Test
    fun t069_nextDrawStaysPutWhenOtherCivsDropOut() {
        assertEquals(0, movedPicks { from, id -> from.filter { it == id || it % 2 == id % 2 } })
    }

    @Test
    fun t070_afterADrawTheNextIsPickedFromTheCivsLeft() {
        val rng = seeded()
        val outside = (1..200).count {
            val picker = Picker(rng)
            val drawn = picker.next(twenty)
            val left = twenty.filter { it != drawn }
            picker.next(left) !in left
        }
        assertEquals(0, outside)
    }

    @Test
    fun t071_theMostFreshnessIsHalfThePoolInEveryEdition() {
        val pools = EDITIONS.map { logic.editionTotal(it) }
        assertEquals(pools.map { it / 2 }, pools.map { logic.freshLimit(it, most) })
    }
}
