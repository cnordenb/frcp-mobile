package net.frcp.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PresetTest {
    private val emptyLog = listOf(emptyList<Draw>())
    private val anyOrder = "EditionState:\nHD\nDrawnCivs:\nBritons\nWu\nCivStates:\nCelts 0\n\nDrawnCivs:\nFranks\n"
    private val unknownParts = "EditionState:\nXX\nDrawnCivs:\nCelts\nNonsense\nCelts\nFranks\n"

    private fun preset(name: String) = fixture("presets/$name")
    private fun loaded(text: String) = logic.emptyState().also { logic.applyPreset(it, logic.parsePreset(text)!!) }

    private fun section(text: String, header: String): List<String> {
        val lines = text.split("\r\n")
        val at = lines.indexOf(header)
        return if (at < 0) emptyList() else lines.drop(at + 1).takeWhile { it.isNotEmpty() && !it.endsWith(":") }
    }

    private fun mixedState() = makeState(
        edition = "HD",
        off = listOf("Celts"),
        log = listOf(listOf(Named("Huns", 1, 30), Named("Celts", 2, 31), Named("Franks", 2, 30))),
    )

    private fun savedWithoutHistory() = logic.presetText(mixedState(), false)

    @Test
    fun t072_desktopPresetLoadsItsEdition() {
        assertEquals("DE", loaded(preset("desktop-log.txt")).edition)
    }

    @Test
    fun t073_desktopPresetLeavesTheCivsItDoesNotListOn() {
        assertEquals(0, loaded(preset("desktop-log.txt")).off.size)
    }

    @Test
    fun t074_desktopPresetLoadsItsDrawsAsOneRound() {
        val text = preset("desktop-log.txt")
        val draws = section(text, "DrawnCivs:")
        assertEquals(listOf(draws.mapIndexed { i, name -> Named(name, i + 1, CIVS.size) }), namedLog(loaded(text)))
    }

    @Test
    fun t075_olderDesktopPresetStillHasItsOldCivNames() {
        val text = preset("desktop-v1.3.2.txt")
        assertTrue("Incas 1" in text && "Mayans 1" in text)
    }

    @Test
    fun t076_olderDesktopPresetLoadsItsPoolPassingOverOldNames() {
        val s = loaded(preset("desktop-v1.3.2.txt"))
        assertEquals(listOf("Bulgarians", "Goths", "Persians", "Sicilians"), namesOf(s.off.sorted()))
    }

    @Test
    fun t077_olderDesktopPresetWithoutDrawsLoadsAnEmptyRound() {
        assertEquals(emptyLog, loaded(preset("desktop-v1.3.2.txt")).log)
    }

    @Test
    fun t078_roundStartPresetLoadsItsCivsOff() {
        assertEquals(listOf("Aztecs", "Wu"), namesOf(loaded(preset("round-start.txt")).off.sorted()))
    }

    @Test
    fun t079_roundStartPresetsEarlierHistoryMakesARoundWithoutCounts() {
        val expected = listOf(
            listOf(Named("Huns", 0, 0), Named("Franks", 0, 0), Named("Teutons", 0, 0)),
            listOf(Named("Celts", 1, CIVS.size - 2), Named("Mongols", 2, CIVS.size - 2)),
        )
        assertEquals(expected, namedLog(loaded(preset("round-start.txt"))))
    }

    @Test
    fun t080_desktopPresetSavesBackByteForByte() {
        val text = preset("round-start.txt")
        assertEquals(text, logic.presetText(loaded(text), true))
    }

    @Test
    fun t081_savedPresetEndsEveryLineInCrLf() {
        val text = savedWithoutHistory()
        assertTrue(text.endsWith("\r\n") && !Regex("[^\r]\n").containsMatchIn(text))
    }

    @Test
    fun t082_savedPresetListsEveryCivInIdOrderWithItsState() {
        val off = mixedState().off
        val expected = CIVS.map { "${it.name} ${if (it.id in off) 0 else 1}" }
        assertEquals(expected, section(savedWithoutHistory(), "CivStates:"))
    }

    @Test
    fun t083_savedPresetListsThisRoundsDrawsOfCivsThatAreOn() {
        assertEquals(listOf("Huns", "Franks"), section(savedWithoutHistory(), "DrawnCivs:"))
    }

    @Test
    fun t084_savedPresetNamesItsEdition() {
        assertEquals(listOf("HD"), section(savedWithoutHistory(), "EditionState:"))
    }

    @Test
    fun t085_savedPresetHasNoHistoryWithFreshnessOff() {
        assertFalse("ContfreshCivArray:" in savedWithoutHistory())
    }

    @Test
    fun t086_savedPresetsHistoryHoldsTheNewestCivsThatCouldSitOut() {
        val past = CIVS.mapIndexed { i, c -> Named(c.name, i + 1, CIVS.size) }
        val round = listOf(Named("Celts", 1, CIVS.size), Named("Franks", 2, CIVS.size))
        val s = makeState(log = listOf(past, round))
        val earlier = past.map { it.name }.filter { it != "Celts" && it != "Franks" }.takeLast(CIVS.size / 2 - 2)
        assertEquals(earlier + listOf("Celts", "Franks"), section(logic.presetText(s, true), "ContfreshCivArray:"))
    }

    @Test
    fun t087_presetsEditionIsReadFromAnySectionOrder() {
        assertEquals("HD", loaded(anyOrder).edition)
    }

    @Test
    fun t088_presetIsReadToTheFirstEmptyLineWithoutCivsOfNewerEditions() {
        assertEquals(listOf(listOf(Named("Britons", 1, 30))), namedLog(loaded(anyOrder)))
    }

    @Test
    fun t089_presetsCivStatesAreReadAfterOtherSections() {
        assertTrue(idOf("Celts") in loaded(anyOrder).off)
    }

    @Test
    fun t090_presetWithAnUnknownEditionKeepsTheDefaultOne() {
        assertEquals(EDITIONS[0], loaded(unknownParts).edition)
    }

    @Test
    fun t091_presetsRepeatedAndUnknownDrawsArePassedOver() {
        val expected = listOf(listOf(Named("Celts", 1, CIVS.size), Named("Franks", 2, CIVS.size)))
        assertEquals(expected, namedLog(loaded(unknownParts)))
    }

    @Test
    fun t092_textWithNoneOfTheSectionsIsNotAPreset() {
        val texts = listOf("", "\r\n", "hello\r\nworld\r\n", "Celts 1\r\nFranks 0\r\n", "\r\nCivStates:\r\nCelts 0\r\n")
        assertEquals(emptyList<String>(), texts.filter { logic.parsePreset(it) != null })
    }

    @Test
    fun t093_loadedPresetsEarlierCivsKeepSittingOut() {
        val s = loaded(preset("round-start.txt"))
        val candidates = logic.drawCandidates(s, Freshness(true, 1000))
        val sittingOut = listOf("Huns", "Franks", "Teutons", "Celts", "Mongols")
        assertEquals(emptyList<String>(), sittingOut.filter { idOf(it) in candidates })
    }
}
