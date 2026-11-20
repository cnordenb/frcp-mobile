package net.frcp.app.picker

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import net.frcp.app.device.UiSound
import net.frcp.app.logic.idOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmSettingsActionsTest {
    private fun nameToggled() = TestPicker().also { p -> p.vm.toggleSetting { copy(showName = !showName) } }

    private fun jinglesOnThenOff() = TestPicker().also { p ->
        p.vm.draw()
        p.vm.setJingles(false)
    }

    private fun legacySwitchedOff(civ: String) = TestPicker(onlyCiv(civ)).also { p ->
        p.vm.draw()
        p.vm.setLegacyJingles(false)
    }

    private fun owned(p: TestPicker) = ViewModelStore().also { owner ->
        ViewModelProvider(owner, viewModelFactory { initializer { p.vm } })[PickerViewModel::class.java]
    }

    @Test
    fun t121_checkboxSettingChanges() {
        assertFalse(nameToggled().vm.settings.showName)
    }

    @Test
    fun t122_checkboxSettingIsSaved() {
        assertFalse(nameToggled().savedSettings.showName)
    }

    @Test
    fun t123_checkboxSettingSoundsTheButton() {
        assertEquals(uiSound(UiSound.Button), nameToggled().lastSound)
    }

    @Test
    fun t124_freshnessSliderSavesItsValue() {
        val p = TestPicker()
        p.vm.setFreshN(5, 28)
        assertEquals(5, p.savedSettings.freshN)
    }

    @Test
    fun t125_freshnessSlidersRightEndMeansTheMost() {
        val p = TestPicker()
        p.vm.setFreshN(5, 28)
        p.vm.setFreshN(28, 28)
        assertEquals(Settings.FRESH_MOST, p.savedSettings.freshN)
    }

    @Test
    fun t126_turningJinglesOnPlaysTheCurrentCivs() {
        val p = TestPicker(settings = Settings(jingles = false))
        p.vm.draw()
        p.vm.setJingles(true)
        assertEquals(jingleOf(p.current), p.lastSound)
    }

    @Test
    fun t127_turningJinglesOffPlaysTheMuteSound() {
        assertEquals(uiSound(UiSound.Mute), jinglesOnThenOff().lastSound)
    }

    @Test
    fun t128_turningJinglesOffIsSaved() {
        assertFalse(jinglesOnThenOff().savedSettings.jingles)
    }

    @Test
    fun t129_turningUiSoundsOffPlaysTheMuteSound() {
        val p = TestPicker()
        p.vm.setUiSounds(false)
        assertEquals(uiSound(UiSound.Mute), p.lastSound)
    }

    @Test
    fun t130_withUiSoundsOffTheButtonsAreSilent() {
        val p = TestPicker()
        p.vm.setUiSounds(false)
        val played = p.sounds.played.size
        p.vm.toggleCiv(idOf("Celts"))
        p.vm.selectTab(2)
        p.vm.undo()
        assertEquals(played, p.sounds.played.size)
    }

    @Test
    fun t131_switchingLegacyJinglesPlaysTheJingleInTheNewStyle() {
        assertEquals(jingleOf(idOf("Britons"), false), legacySwitchedOff("Britons").lastSound)
    }

    @Test
    fun t132_switchingLegacyJinglesSoundsTheViewForACivWithoutALegacyOne() {
        assertEquals(uiSound(UiSound.View), legacySwitchedOff("Wu").lastSound)
    }

    @Test
    fun t133_switchingLegacyJinglesIsSavedAndLeavesJinglesOn() {
        val saved = legacySwitchedOff("Wu").savedSettings
        assertTrue(saved.jingles && !saved.legacyJingles)
    }

    @Test
    fun t134_everyUiSoundLoadsAhead() {
        val p = TestPicker()
        assertEquals(emptyList<UiSound>(), UiSound.entries.filter { uiSound(it) !in p.sounds.loaded })
    }

    @Test
    fun t135_soundsStayWhileTheViewModelLives() {
        val p = TestPicker()
        owned(p)
        assertFalse(p.sounds.released)
    }

    @Test
    fun t136_soundsAreLetGoWithTheViewModel() {
        val p = TestPicker()
        owned(p).clear()
        assertTrue(p.sounds.released)
    }
}
