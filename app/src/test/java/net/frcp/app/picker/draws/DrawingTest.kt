package net.frcp.app.picker.draws

import net.frcp.app.logic.CIVS
import net.frcp.app.logic.Draw
import net.frcp.app.logic.EDITIONS
import net.frcp.app.logic.idOf
import net.frcp.app.logic.makeState
import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.SwipeKind
import net.frcp.app.picker.TestPicker
import net.frcp.app.picker.drawnPicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DrawingTest {
    private fun wholePoolDrawn(edition: String, extra: Int = 0): TestPicker {
        val p = TestPicker(makeState(edition = edition))
        repeat(p.vm.view.pool.size + extra) { p.vm.draw() }
        return p
    }

    private fun emptyPool() = TestPicker(makeState(off = CIVS.map { it.name }))

    private fun emptyPoolDrawn() = emptyPool().also { it.vm.draw() }

    private fun noRandomSourceDrawn() = TestPicker(rng = null).also { it.vm.draw() }

    @Test
    fun t137_drawCountsOneCivDrawn() {
        assertEquals(1, drawnPicker(1).vm.view.drawn)
    }

    @Test
    fun t138_drawTakesItsCivOutOfTheCivsRemaining() {
        val p = drawnPicker(1)
        assertFalse(p.current in p.vm.view.remaining)
    }

    @Test
    fun t139_drawIsSavedWithItsCountOutOfThePool() {
        val p = drawnPicker(1)
        assertEquals(listOf(listOf(Draw(p.current!!, 1, CIVS.size))), p.saved!!.log)
    }

    @Test
    fun t140_wholePoolDrawsEveryCivOnceInEveryEdition() {
        val wrong = EDITIONS.filter { ed -> wholePoolDrawn(ed).let { it.vm.view.pool != it.round.sorted() } }
        assertEquals(emptyList<String>(), wrong)
    }

    @Test
    fun t141_drawAfterAWholePoolStartsASecondRoundInEveryEdition() {
        assertEquals(EDITIONS.map { 2 }, EDITIONS.map { wholePoolDrawn(it, extra = 1).vm.view.state.log.size })
    }

    @Test
    fun t142_swipeLeftShowsTheCivItThenDraws() {
        val p = TestPicker()
        val shown = mutableListOf<Int?>()
        val drawn = mutableListOf<Int?>()
        repeat(CIVS.size * 2) {
            shown += p.vm.swipeCell(SwipeKind.Draw)
            p.vm.swipe(SwipeKind.Draw)
            drawn += p.current
        }
        assertEquals(shown, drawn)
    }

    @Test
    fun t143_emptyPoolShowsNoCivToDraw() {
        assertNull(emptyPool().vm.swipeCell(SwipeKind.Draw))
    }

    @Test
    fun t144_drawFromAnEmptyPoolSaysThePoolIsEmpty() {
        assertEquals("Empty pool!", emptyPoolDrawn().vm.view.flash)
    }

    @Test
    fun t145_drawFromAnEmptyPoolSoundsTheError() {
        assertEquals(ERROR_SOUND, emptyPoolDrawn().lastSound)
    }

    @Test
    fun t146_drawFromAnEmptyPoolDrawsNothing() {
        assertEquals(0, emptyPoolDrawn().vm.view.drawn)
    }

    @Test
    fun t147_emptyPoolMessageGoesWhenThePoolChanges() {
        val p = emptyPoolDrawn()
        p.vm.toggleCiv(idOf("Celts"))
        assertNull(p.vm.view.flash)
    }

    @Test
    fun t148_drawWithoutARandomSourceSaysWhy() {
        assertEquals("No random source", noRandomSourceDrawn().vm.view.flash)
    }

    @Test
    fun t149_drawWithoutARandomSourceDrawsNothing() {
        assertNull(noRandomSourceDrawn().current)
    }
}
