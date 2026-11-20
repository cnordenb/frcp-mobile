package net.frcp.app.picker.draws

import net.frcp.app.logic.Draw
import net.frcp.app.logic.idOf
import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.SwipeKind
import net.frcp.app.picker.TestPicker
import net.frcp.app.picker.drawnPicker
import net.frcp.app.picker.jingleOf
import net.frcp.app.picker.onlyCiv
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmRerollTest {
    private class Rerolled(val p: TestPicker, val before: List<Draw>) {
        val after get() = p.vm.view.state.log.last()
    }

    private fun rerolled(): Rerolled {
        val p = drawnPicker(3)
        val before = p.vm.view.state.log.last().toList()
        p.vm.swipe(SwipeKind.Reroll)
        return Rerolled(p, before)
    }

    private fun rerolledAfterUndo() = drawnPicker(3).also { p ->
        p.vm.undo()
        p.vm.reroll()
    }

    private fun rerolledBeforeADraw() = TestPicker().also { it.vm.reroll() }

    private fun oneCivDrawn() = TestPicker(onlyCiv("Celts")).also { it.vm.draw() }

    private fun oneCivRerolled() = oneCivDrawn().also { it.vm.reroll() }

    @Test
    fun t173_swipeUpShowsTheCivItSwapsIn() {
        val p = drawnPicker(3)
        val shown = p.vm.swipeCell(SwipeKind.Reroll)
        p.vm.swipe(SwipeKind.Reroll)
        assertEquals(shown, p.current)
    }

    @Test
    fun t174_rerollSwapsInAnotherCiv() {
        val r = rerolled()
        assertNotEquals(r.before.last().id, r.p.current)
    }

    @Test
    fun t175_rerollKeepsTheEarlierDraws() {
        val r = rerolled()
        assertEquals(r.before.dropLast(1), r.after.dropLast(1))
    }

    @Test
    fun t176_rerollCountsNoOtherDraw() {
        val r = rerolled()
        assertEquals(r.before.last().k, r.after.last().k)
    }

    @Test
    fun t177_rerolledCivCanStillComeUpThisRound() {
        val r = rerolled()
        assertTrue(r.before.last().id in r.p.vm.view.remaining)
    }

    @Test
    fun t178_rerollPlaysTheNewCivsJingle() {
        val p = rerolled().p
        assertEquals(jingleOf(p.current), p.lastSound)
    }

    @Test
    fun t179_undoPassesOverAReroll() {
        val p = drawnPicker(3)
        val two = p.round.take(2)
        p.vm.reroll()
        p.vm.undo()
        assertEquals(two, p.round)
    }

    @Test
    fun t180_redoAfterARerollSoundsTheError() {
        val p = rerolledAfterUndo()
        p.vm.redo()
        assertEquals(ERROR_SOUND, p.lastSound)
    }

    @Test
    fun t181_redoAfterARerollChangesNothing() {
        val p = rerolledAfterUndo()
        val round = p.round
        p.vm.redo()
        assertEquals(round, p.round)
    }

    @Test
    fun t182_swipeUpShowsNothingBeforeADraw() {
        assertNull(TestPicker().vm.swipeCell(SwipeKind.Reroll))
    }

    @Test
    fun t183_rerollBeforeADrawSoundsTheError() {
        assertEquals(ERROR_SOUND, rerolledBeforeADraw().lastSound)
    }

    @Test
    fun t184_swipeUpShowsNothingWithNoOtherCivToSwapIn() {
        assertNull(oneCivDrawn().vm.swipeCell(SwipeKind.Reroll))
    }

    @Test
    fun t185_rerollWithNoOtherCivSoundsTheError() {
        assertEquals(ERROR_SOUND, oneCivRerolled().lastSound)
    }

    @Test
    fun t186_rerollWithNoOtherCivKeepsTheCiv() {
        assertEquals(listOf(idOf("Celts")), oneCivRerolled().round)
    }
}
