package net.frcp.app.picker

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SettingsTest {
    private val webDefaults = """{"v":1,"showName":true,"showIcon":true,"showCounter":true,"jingles":true,
        "legacyJingles":true,"uiSounds":true,"drawOnLoad":false,"newRoundOnVisit":false,"fresh":true,"freshN":1000,
        "showRemaining":true,"autoReset":false,"autoToggle":false}"""

    @Test
    fun t115_newSettingsSaveAsTheWebVersionsDefaults() {
        assertEquals(Json.parseToJsonElement(webDefaults), Json.parseToJsonElement(Settings().toJson()))
    }

    @Test
    fun t116_settingsReadBackAsTheyWereSaved() {
        val changed = Settings(
            showName = false, showIcon = false, showCounter = false, jingles = false, legacyJingles = false,
            uiSounds = false, drawOnLoad = true, newRoundOnVisit = true, fresh = false, freshN = 7,
            showRemaining = false, autoReset = true, autoToggle = true,
        )
        assertEquals(changed, Settings.parse(changed.toJson()))
    }

    @Test
    fun t117_webSettingsReadBackPassingOverWebOnlyOnes() {
        val web = """{"v":1,"showName":false,"tooltips":false,"jingles":true,"hotkeys":"desktop",""" +
            """"charKeys":false,"freshN":4}"""
        assertEquals(Settings(showName = false, freshN = 4), Settings.parse(web))
    }

    @Test
    fun t118_settingOfTheWrongTypeKeepsItsDefault() {
        val wrong = """{"showName":"false","jingles":0,"uiSounds":null,"fresh":[],"freshN":"5","autoReset":{}}"""
        assertEquals(Settings(), Settings.parse(wrong))
    }

    @Test
    fun t119_freshnessReadsWithinOneAndTheMost() {
        val saved = listOf("7", "1", "0", "-3", "2.5", "1000", "5000")
        val read = saved.map { Settings.parse("""{"freshN":$it}""").freshN }
        assertEquals(listOf(7, 1, 1, 1, 1, 1000, 1000), read)
    }

    @Test
    fun t120_whatIsNotSavedSettingsReadsAsTheDefaults() {
        val raws = listOf(null, "", "not json", "[]", "42", "null", "\"text\"")
        assertEquals(emptyList<String?>(), raws.filter { Settings.parse(it) != Settings() })
    }
}
