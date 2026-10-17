package net.frcp.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Preset files, in the desktop app's .txt format. The fixtures hold files the desktop app wrote (desktop-log.txt,
// desktop-v1.3.2.txt) and one written to its WriteLogFile rules (round-start.txt).
class PresetTest {
    private fun preset(name: String) = fixture("presets/$name")
    private fun loaded(text: String) = logic.emptyState().also { logic.applyPreset(it, logic.parsePreset(text)!!) }

    /** The lines of one section of a preset file. */
    private fun section(text: String, header: String): List<String> {
        val lines = text.split("\r\n")
        val at = lines.indexOf(header)
        return if (at < 0) emptyList() else lines.drop(at + 1).takeWhile { it.isNotEmpty() && !it.endsWith(":") }
    }

    @Test
    fun `a preset the desktop app wrote mid-round loads its pool, edition and draws`() {
        val text = preset("desktop-log.txt")
        val s = loaded(text)
        val draws = section(text, "DrawnCivs:")
        assertEquals("DE", s.edition)
        assertEquals("its civs are all on, and the civs it doesn't list stay on", 0, s.off.size)
        // Its freshness history is all from this round, so there's no earlier round.
        assertEquals(listOf(draws.mapIndexed { i, name -> Named(name, i + 1, CIVS.size) }), namedLog(s))
    }

    @Test
    fun `a preset from an older desktop app loads, passing over civ names it no longer has`() {
        val text = preset("desktop-v1.3.2.txt")
        assertTrue("the file still has the old names", "Incas 1" in text && "Mayans 1" in text)
        val s = loaded(text)
        assertEquals(listOf("Bulgarians", "Goths", "Persians", "Sicilians"), namesOf(s.off.sorted()))
        assertEquals(listOf(emptyList<Draw>()), s.log)
    }

    @Test
    fun `a desktop preset loads and saves back byte for byte`() {
        val text = preset("round-start.txt")
        val s = loaded(text)
        assertEquals(listOf("Aztecs", "Wu"), namesOf(s.off.sorted()))
        // The civs of the freshness history from before this round make an earlier round, without counts.
        assertEquals(
            listOf(
                listOf(Named("Huns", 0, 0), Named("Franks", 0, 0), Named("Teutons", 0, 0)),
                listOf(Named("Celts", 1, CIVS.size - 2), Named("Mongols", 2, CIVS.size - 2)),
            ),
            namedLog(s),
        )
        assertEquals(text, logic.presetText(s, true))
    }

    @Test
    fun `a saved preset lists every civ in ID order, this round's draws of civs that are on, and the edition`() {
        val s = makeState(edition = "HD", off = listOf("Celts"), log = listOf(listOf(Named("Huns", 1, 30), Named("Celts", 2, 31), Named("Franks", 2, 30))))
        val text = logic.presetText(s, false)
        assertTrue("every line ends in CRLF", text.endsWith("\r\n") && !Regex("[^\r]\n").containsMatchIn(text))
        assertEquals(CIVS.map { "${it.name} ${if (it.id in s.off) 0 else 1}" }, section(text, "CivStates:"))
        assertEquals(listOf("Huns", "Franks"), section(text, "DrawnCivs:"))
        assertEquals(listOf("HD"), section(text, "EditionState:"))
        assertFalse("no freshness history with continuous freshness off", "ContfreshCivArray:" in text)
    }

    @Test
    fun `a saved preset's freshness history holds as many of the newest civs as could sit out, oldest first`() {
        val past = CIVS.mapIndexed { i, c -> Named(c.name, i + 1, CIVS.size) }
        val s = makeState(log = listOf(past, listOf(Named("Celts", 1, CIVS.size), Named("Franks", 2, CIVS.size))))
        val half = CIVS.size / 2
        val newest = past.map { it.name }.filter { it != "Celts" && it != "Franks" }.takeLast(half - 2) + listOf("Celts", "Franks")
        assertEquals(newest, section(logic.presetText(s, true), "ContfreshCivArray:"))
    }

    @Test
    fun `a preset is read from the default edition with every civ on, in any section order, to the first empty line`() {
        val s = loaded("EditionState:\nHD\nDrawnCivs:\nBritons\nWu\nCivStates:\nCelts 0\n\nDrawnCivs:\nFranks\n")
        assertEquals("HD", s.edition)
        assertEquals("Wu isn't in HD, and the second DrawnCivs is past the empty line", listOf(listOf(Named("Britons", 1, 30))), namedLog(s))
        assertTrue(idOf("Celts") in s.off)
    }

    @Test
    fun `a preset with an unknown edition keeps the default one, and repeated or unknown draws are passed over`() {
        val s = loaded("EditionState:\nXX\nDrawnCivs:\nCelts\nNonsense\nCelts\nFranks\n")
        assertEquals(EDITIONS[0], s.edition)
        assertEquals(listOf(listOf(Named("Celts", 1, CIVS.size), Named("Franks", 2, CIVS.size))), namedLog(s))
    }

    @Test
    fun `text with none of the sections isn't a preset`() {
        for (text in listOf("", "\r\n", "hello\r\nworld\r\n", "Celts 1\r\nFranks 0\r\n", "\r\nCivStates:\r\nCelts 0\r\n")) {
            assertNull(text, logic.parsePreset(text))
        }
    }

    @Test
    fun `after loading a preset, its earlier civs keep sitting out of the next draws`() {
        val s = loaded(preset("round-start.txt"))
        val candidates = logic.drawCandidates(s, Freshness(true, 1000))
        for (name in listOf("Huns", "Franks", "Teutons", "Celts", "Mongols")) assertFalse(name, idOf(name) in candidates)
    }
}
