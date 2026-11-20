package net.frcp.app

import net.frcp.app.logic.CIVS
import net.frcp.app.logic.Draw
import net.frcp.app.logic.EDITIONS
import net.frcp.app.logic.idOf
import net.frcp.app.logic.makeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// The Draw tab's actions, as the desktop app's model tests and the web version's swipe test have them: draws, undo and
// redo, re-rolls and resets, the civ each swipe shows coming in, and the sounds they make.
class DrawingTest {
    /** A picker with n draws made. */
    private fun drawn(n: Int, settings: Settings = Settings()) = TestPicker(settings = settings).also { p -> repeat(n) { p.vm.draw() } }

    /** A state whose pool is the one civ named. */
    private fun onlyCiv(name: String) = makeState(off = CIVS.filter { it.name != name }.map { it.name })

    private val error = uiSound(UiSound.Error)

    @Test
    fun `a draw takes a civ from the pool, counts it out of the pool and saves it`() {
        val p = drawn(1)
        val id = p.current!!
        assertEquals(1, p.vm.view.drawn)
        assertFalse(id in p.vm.view.remaining)
        assertEquals(listOf(listOf(Draw(id, 1, CIVS.size))), p.saved!!.log)
    }

    @Test
    fun `drawing a whole pool draws every civ once, in every edition, and the next draw starts a new round`() {
        for (ed in EDITIONS) {
            val p = TestPicker(makeState(edition = ed))
            val pool = p.vm.view.pool
            repeat(pool.size) { p.vm.draw() }
            assertEquals(ed, pool, p.round.sorted())
            assertEquals(ed, emptyList<Int>(), p.vm.view.remaining)
            p.vm.draw()
            assertEquals(ed, 1, p.vm.view.drawn)
            assertEquals(ed, 2, p.vm.view.state.log.size)
        }
    }

    @Test
    fun `a swipe left shows the civ it then draws`() {
        val p = TestPicker()
        repeat(CIVS.size * 2) {
            val shown = p.vm.swipeCell(SwipeKind.Draw)
            p.vm.swipe(SwipeKind.Draw)
            assertEquals(shown, p.current)
        }
    }

    @Test
    fun `the next draw's jingle and random's load ahead, and a draw plays the one loaded`() {
        val p = TestPicker()
        assertTrue(jingleOf(null) in p.sounds.loaded)
        repeat(10) {
            val next = p.vm.swipeCell(SwipeKind.Draw)
            assertTrue(jingleOf(next) in p.sounds.loaded)
            p.vm.draw()
            assertEquals(jingleOf(next), p.lastSound)
        }
    }

    @Test
    fun `with jingles off, a draw sounds the button and no jingle loads`() {
        val p = drawn(1, Settings(jingles = false))
        assertEquals(uiSound(UiSound.Button), p.lastSound)
        assertTrue(p.sounds.loaded.none { it.startsWith("sounds/civ/") })
    }

    @Test
    fun `a civ plays its legacy jingle while legacy jingles are on, and its new one while they're off`() {
        val britons = idOf("Britons")
        assertTrue(CIVS[britons].legacy)
        assertNotEquals(jingleOf(britons, true), jingleOf(britons, false))
        for (legacy in listOf(true, false)) {
            val p = TestPicker(onlyCiv("Britons"), Settings(legacyJingles = legacy))
            p.vm.draw()
            assertEquals(jingleOf(britons, legacy), p.lastSound)
        }
    }

    @Test
    fun `an empty pool draws nothing, says so and sounds the error, until the pool changes`() {
        val p = TestPicker(makeState(off = CIVS.map { it.name }))
        assertNull(p.vm.swipeCell(SwipeKind.Draw))
        p.vm.draw()
        assertEquals("Empty pool!", p.vm.view.flash)
        assertEquals(error, p.lastSound)
        assertEquals(0, p.vm.view.drawn)
        p.vm.toggleCiv(idOf("Celts"))
        assertNull(p.vm.view.flash)
    }

    @Test
    fun `without a random source, nothing is drawn and the screen says why`() {
        val p = TestPicker(rng = null)
        p.vm.draw()
        assertEquals("No random source", p.vm.view.flash)
        assertNull(p.current)
    }

    @Test
    fun `undo takes back draws and resets one at a time, and redo brings them back`() {
        val p = drawn(3)
        val three = p.round
        p.vm.reset()
        p.vm.undo()
        assertEquals(three, p.round)
        p.vm.undo()
        assertEquals(three.take(2), p.round)
        assertEquals("undo plays the jingle of the civ it goes back to", jingleOf(three[1]), p.lastSound)
        p.vm.redo()
        p.vm.redo()
        assertEquals(emptyList<Int>(), p.round)
        assertEquals(listOf(three, emptyList()), p.saved!!.log.map { r -> r.map { it.id } })
    }

    @Test
    fun `a new draw clears what redo would bring back`() {
        val p = drawn(2)
        p.vm.undo()
        p.vm.draw()
        val round = p.round
        p.vm.redo()
        assertEquals(error, p.lastSound)
        assertEquals(round, p.round)
    }

    @Test
    fun `undo with nothing to undo changes nothing and sounds the error`() {
        val p = TestPicker()
        p.vm.undo()
        p.vm.redo()
        assertEquals(listOf(error, error), p.sounds.played)
        assertNull(p.current)
    }

    @Test
    fun `undo goes back up to 200 steps`() {
        val p = drawn(210)
        repeat(200) { p.vm.undo() }
        assertEquals(10, p.vm.view.state.log.sumOf { it.size })
        p.vm.undo()
        assertEquals(error, p.lastSound)
        assertEquals(10, p.vm.view.state.log.sumOf { it.size })
    }

    @Test
    fun `a swipe right shows the civ undo goes back to, and random before the first draw`() {
        val p = drawn(2)
        val first = p.round[0]
        assertEquals(first, p.vm.swipeCell(SwipeKind.Undo))
        p.vm.swipe(SwipeKind.Undo)
        assertEquals(first, p.current)
        assertEquals(p.vm.randomCell, p.vm.swipeCell(SwipeKind.Undo))
        p.vm.swipe(SwipeKind.Undo)
        assertNull(p.current)
        assertNull("nothing is left to undo", p.vm.swipeCell(SwipeKind.Undo))
    }

    @Test
    fun `a swipe up swaps the civ it shows for the current one, without counting another draw`() {
        val p = drawn(3)
        val before = p.vm.view.state.log.last().toList()
        val shown = p.vm.swipeCell(SwipeKind.Reroll)
        p.vm.swipe(SwipeKind.Reroll)
        val after = p.vm.view.state.log.last()
        assertEquals(shown, p.current)
        assertNotEquals(before.last().id, p.current)
        assertEquals(before.dropLast(1), after.dropLast(1))
        assertEquals(before.last().k, after.last().k)
        assertTrue("the swapped-out civ can still come up this round", before.last().id in p.vm.view.remaining)
        assertEquals(jingleOf(p.current), p.lastSound)
    }

    @Test
    fun `undo passes over a re-roll, back to before the draw it swapped`() {
        val p = drawn(3)
        val two = p.round.take(2)
        p.vm.reroll()
        p.vm.undo()
        assertEquals(two, p.round)
    }

    @Test
    fun `a re-roll clears redo, which would otherwise undo it`() {
        val p = drawn(3)
        p.vm.undo()
        p.vm.reroll()
        val rerolled = p.round
        p.vm.redo()
        assertEquals(error, p.lastSound)
        assertEquals(rerolled, p.round)
    }

    @Test
    fun `a re-roll needs a draw to swap and another civ to swap in`() {
        val none = TestPicker()
        assertNull(none.vm.swipeCell(SwipeKind.Reroll))
        none.vm.reroll()
        assertEquals(error, none.lastSound)

        val one = TestPicker(onlyCiv("Celts")).also { it.vm.draw() }
        assertNull(one.vm.swipeCell(SwipeKind.Reroll))
        one.vm.reroll()
        assertEquals(error, one.lastSound)
        assertEquals(listOf(idOf("Celts")), one.round)
    }

    @Test
    fun `a swipe down closes the round with random's jingle, and does nothing on a round with no draws`() {
        val p = TestPicker()
        assertNull(p.vm.swipeCell(SwipeKind.Reset))
        p.vm.reset()
        assertEquals(emptyList<String>(), p.sounds.played)
        assertEquals(1, p.vm.view.state.log.size)
        p.vm.draw()
        assertEquals(p.vm.randomCell, p.vm.swipeCell(SwipeKind.Reset))
        p.vm.swipe(SwipeKind.Reset)
        assertEquals(0, p.vm.view.drawn)
        assertEquals(2, p.vm.view.state.log.size)
        assertEquals(jingleOf(null), p.lastSound)
    }

    @Test
    fun `clearing the log keeps this round only, and clears undo as the web page does`() {
        val p = drawn(CIVS.size + 2)
        p.vm.clearLog()
        assertEquals(1, p.vm.view.state.log.size)
        assertEquals(2, p.round.size)
        p.vm.undo()
        assertEquals(error, p.lastSound)
        assertEquals(1, p.saved!!.log.size)
    }
}
