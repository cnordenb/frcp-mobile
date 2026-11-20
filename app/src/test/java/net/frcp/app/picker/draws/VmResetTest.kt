package net.frcp.app.picker.draws

import net.frcp.app.logic.CIVS
import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.SwipeKind
import net.frcp.app.picker.TestPicker
import net.frcp.app.picker.drawnPicker
import net.frcp.app.picker.jingleOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmResetTest {
    private fun resetBeforeADraw() = TestPicker().also { it.vm.reset() }

    private fun swipedDown() = drawnPicker(1).also { it.vm.swipe(SwipeKind.Reset) }

    private fun logCleared() = drawnPicker(CIVS.size + 2).also { it.vm.clearLog() }

    @Test
    fun t187_swipeDownShowsNothingBeforeADraw() {
        assertNull(TestPicker().vm.swipeCell(SwipeKind.Reset))
    }

    @Test
    fun t188_resetBeforeADrawIsSilent() {
        assertEquals(emptyList<String>(), resetBeforeADraw().sounds.played)
    }

    @Test
    fun t189_resetBeforeADrawAddsNoRound() {
        assertEquals(1, resetBeforeADraw().vm.view.state.log.size)
    }

    @Test
    fun t190_swipeDownShowsRandomAfterADraw() {
        val p = drawnPicker(1)
        assertEquals(p.vm.randomCell, p.vm.swipeCell(SwipeKind.Reset))
    }

    @Test
    fun t191_swipeDownLeavesNoDrawsThisRound() {
        assertEquals(0, swipedDown().vm.view.drawn)
    }

    @Test
    fun t192_swipeDownStartsANewRound() {
        assertEquals(2, swipedDown().vm.view.state.log.size)
    }

    @Test
    fun t193_swipeDownPlaysRandomsJingle() {
        assertEquals(jingleOf(null), swipedDown().lastSound)
    }

    @Test
    fun t194_clearingTheLogKeepsOneRound() {
        assertEquals(1, logCleared().vm.view.state.log.size)
    }

    @Test
    fun t195_clearingTheLogKeepsThisRoundsDraws() {
        assertEquals(2, logCleared().round.size)
    }

    @Test
    fun t196_clearingTheLogClearsUndo() {
        val p = logCleared()
        p.vm.undo()
        assertEquals(ERROR_SOUND, p.lastSound)
    }

    @Test
    fun t197_clearedLogIsSaved() {
        assertEquals(1, logCleared().saved!!.log.size)
    }
}
