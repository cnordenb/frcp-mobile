package net.frcp.app.picker.pool

import net.frcp.app.logic.CIVS
import net.frcp.app.logic.Named
import net.frcp.app.logic.idOf
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import net.frcp.app.logic.namedLog
import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.Settings
import net.frcp.app.picker.TestPicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmPoolRoundTest {
    private val twoDraws = listOf(Named("Huns", 1, 56), Named("Celts", 2, 56))

    private fun poolChangedAfterUndo() = TestPicker().also { p ->
        repeat(2) { p.vm.draw() }
        p.vm.undo()
        p.vm.toggleCiv(idOf("Celts"))
        p.vm.undo()
        p.vm.redo()
    }

    private fun wuToggledOff(settings: Settings) =
        TestPicker(makeState(log = listOf(twoDraws)), settings).also { it.vm.toggleCiv(idOf("Wu")) }

    private fun autoToggled(times: Int) =
        TestPicker(makeState(off = CIVS.map { it.name })).also { p -> repeat(times) { p.vm.toggleAutoToggle() } }

    private fun olderCivs(ed: String) = civsOf(logic.editionInfo.getValue(ed).olderDlcs)

    @Test
    fun t264_changingThePoolClearsUndoAndRedo() {
        assertEquals(listOf(ERROR_SOUND, ERROR_SOUND), poolChangedAfterUndo().sounds.played.takeLast(2))
    }

    @Test
    fun t265_changingThePoolKeepsTheRoundsDraws() {
        assertEquals(1, poolChangedAfterUndo().round.size)
    }

    @Test
    fun t266_changeThatLeavesThePoolAsItWasKeepsUndo() {
        val p = TestPicker()
        p.vm.draw()
        p.vm.enableAll()
        p.vm.undo()
        assertEquals(emptyList<Int>(), p.round)
    }

    @Test
    fun t267_withoutAutoResetAChangeOfThePoolKeepsTheRound() {
        assertEquals(1, wuToggledOff(Settings()).vm.view.state.log.size)
    }

    @Test
    fun t268_withAutoResetAChangeOfThePoolStartsANewRound() {
        val p = wuToggledOff(Settings(autoReset = true))
        assertEquals(listOf(twoDraws, emptyList()), namedLog(p.vm.view.state))
    }

    @Test
    fun t269_turningAutoToggleOnIsSaved() {
        assertTrue(autoToggled(1).savedSettings.autoToggle)
    }

    @Test
    fun t270_turningAutoToggleOnTurnsOnTheEditionsOlderCivs() {
        assertEquals(olderCivs("DE"), autoToggled(1).vm.view.pool)
    }

    @Test
    fun t271_turningAutoToggleOffIsSaved() {
        assertFalse(autoToggled(2).savedSettings.autoToggle)
    }

    @Test
    fun t272_turningAutoToggleOffTurnsTheOlderCivsOff() {
        assertEquals(emptyList<Int>(), autoToggled(2).vm.view.pool)
    }

    @Test
    fun t273_withAutoToggleSwitchingEditionTurnsOnItsOlderCivs() {
        val p = TestPicker(makeState(off = CIVS.map { it.name }), Settings(autoToggle = true))
        p.vm.setEdition("HD")
        assertEquals(olderCivs("HD"), p.vm.view.pool)
    }

    @Test
    fun t274_autoResetIsSaved() {
        val p = TestPicker()
        p.vm.toggleAutoReset()
        assertTrue(p.savedSettings.autoReset)
    }
}
