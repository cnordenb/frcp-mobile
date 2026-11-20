package net.frcp.app

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.frcp.app.logic.CIVS
import net.frcp.app.logic.EDITIONS
import net.frcp.app.logic.idOf
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// The settings: saved in the web version's format, so they move between the two, and read back as the web version
// reads them. Then the view model's settings actions: the Options dialog's checkboxes, sounds and the freshness slider.
class SettingsTest {
    // The web version's script.js DEFAULT_SETTINGS, less tooltips, hotkeys and charKeys, which a phone has no use for
    private val webDefaults = """{"v":1,"showName":true,"showIcon":true,"showCounter":true,"jingles":true,
        "legacyJingles":true,"uiSounds":true,"drawOnLoad":false,"newRoundOnVisit":false,"fresh":true,"freshN":1000,
        "showRemaining":true,"autoReset":false,"autoToggle":false}"""

    @Test
    fun `new settings are the web version's defaults, and save as it does`() {
        assertEquals(Json.parseToJsonElement(webDefaults), Json.parseToJsonElement(Settings().toJson()))
    }

    @Test
    fun `settings read back as they were saved`() {
        val changed = Settings(
            showName = false, showIcon = false, showCounter = false, jingles = false, legacyJingles = false,
            uiSounds = false, drawOnLoad = true, newRoundOnVisit = true, fresh = false, freshN = 7,
            showRemaining = false, autoReset = true, autoToggle = true,
        )
        assertEquals(changed, Settings.parse(changed.toJson()))
        assertEquals(Settings(), Settings.parse(Settings().toJson()))
    }

    @Test
    fun `settings the web version saved read back, passing over the ones a phone has no use for`() {
        val web = """{"v":1,"showName":false,"tooltips":false,"jingles":true,"hotkeys":"desktop","charKeys":false,"freshN":4}"""
        assertEquals(Settings(showName = false, freshN = 4), Settings.parse(web))
        assertFalse("the app saves no web-only settings", "tooltips" in Json.parseToJsonElement(Settings().toJson()).jsonObject)
    }

    @Test
    fun `a setting that's missing or of the wrong type keeps its default`() {
        val wrong = """{"showName":"false","jingles":0,"uiSounds":null,"fresh":[],"freshN":"5","autoReset":{}}"""
        assertEquals(Settings(), Settings.parse(wrong))
    }

    @Test
    fun `freshness below 1 or not whole reads as 1, and above the most as the most`() {
        for ((saved, read) in listOf("7" to 7, "1" to 1, "0" to 1, "-3" to 1, "2.5" to 1, "1000" to 1000, "5000" to 1000)) {
            assertEquals(saved, read, Settings.parse("""{"freshN":$saved}""").freshN)
        }
    }

    @Test
    fun `anything that isn't saved settings reads as the defaults`() {
        for (raw in listOf(null, "", "not json", "[]", "42", "null", "\"text\"")) {
            assertEquals(raw, Settings(), Settings.parse(raw))
        }
    }

    // The view model's settings actions

    @Test
    fun `a checkbox setting changes and is saved`() {
        val p = TestPicker()
        p.vm.toggleSetting { copy(showName = !showName) }
        assertFalse(p.vm.settings.showName)
        assertFalse(p.savedSettings.showName)
        assertEquals(uiSound(UiSound.Button), p.lastSound)
    }

    @Test
    fun `the freshness slider's right end means the most, which follows the pool's size`() {
        val p = TestPicker()
        p.vm.setFreshN(5, 28)
        assertEquals(5, p.savedSettings.freshN)
        p.vm.setFreshN(28, 28)
        assertEquals(Settings.FRESH_MOST, p.savedSettings.freshN)
        for (ed in EDITIONS) {
            val pool = logic.editionTotal(ed)
            assertEquals(ed, pool / 2, logic.freshLimit(pool, Settings.FRESH_MOST))
        }
    }

    @Test
    fun `turning jingles on plays the current civ's, and turning them off plays the mute sound`() {
        val p = TestPicker(settings = Settings(jingles = false))
        p.vm.draw()
        p.vm.setJingles(true)
        assertEquals(jingleOf(p.current), p.lastSound)
        p.vm.setJingles(false)
        assertEquals(uiSound(UiSound.Mute), p.lastSound)
        assertFalse(p.savedSettings.jingles)
    }

    @Test
    fun `with UI sounds off, the buttons are silent`() {
        val p = TestPicker()
        p.vm.setUiSounds(false)
        assertEquals(uiSound(UiSound.Mute), p.lastSound)
        val played = p.sounds.played.size
        p.vm.toggleCiv(idOf("Celts"))
        p.vm.selectTab(2)
        p.vm.undo()
        assertEquals(played, p.sounds.played.size)
    }

    @Test
    fun `switching legacy jingles plays the civ's jingle in the new style, or the view sound for a civ without a legacy one`() {
        val britons = TestPicker(makeState(off = CIVS.filter { it.name != "Britons" }.map { it.name }))
        britons.vm.draw()
        britons.vm.setLegacyJingles(false)
        assertEquals(jingleOf(idOf("Britons"), false), britons.lastSound)

        val wu = TestPicker(makeState(off = CIVS.filter { it.name != "Wu" }.map { it.name }))
        wu.vm.draw()
        assertFalse(CIVS[idOf("Wu")].legacy)
        wu.vm.setLegacyJingles(false)
        assertEquals(uiSound(UiSound.View), wu.lastSound)
        assertTrue(wu.savedSettings.jingles && !wu.savedSettings.legacyJingles)
    }

    @Test
    fun `every UI sound loads ahead, and the sounds are let go with the view model`() {
        val p = TestPicker()
        assertTrue(UiSound.entries.all { uiSound(it) in p.sounds.loaded })
        val owner = ViewModelStore()
        ViewModelProvider(owner, viewModelFactory { initializer { p.vm } })[PickerViewModel::class.java]
        assertFalse(p.sounds.released)
        owner.clear()
        assertTrue(p.sounds.released)
    }
}
