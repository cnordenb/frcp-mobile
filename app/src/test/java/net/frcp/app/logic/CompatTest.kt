package net.frcp.app.logic

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Compatibility: what share links, saved data and preset files from the web version and the desktop app rely on.
class CompatTest {
    @Test
    fun `civ IDs never change, and every civ has its ID frozen`() {
        val frozen = Json.parseToJsonElement(fixture("civ-ids.json")).jsonArray.map { it.jsonPrimitive.content }
        frozen.forEachIndexed { id, name ->
            assertTrue("civ $id ($name) is gone: share links and saved data still refer to it", id < CIVS.size)
            assertEquals("civ $id was $name: share links and saved data refer to civs by ID", name, CIVS[id].name)
        }
        for (c in CIVS.drop(frozen.size)) throw AssertionError("civ ${c.id} (${c.name}) isn't in the frozen civ IDs: add it in the web version")
    }

    @Test
    fun `share links written by earlier versions open as they did`() {
        for (element in Json.parseToJsonElement(fixture("share-links.json")).jsonArray) {
            val f = element.jsonObject
            val note = f.getValue("note").jsonPrimitive.content
            val edition = f.getValue("edition").jsonPrimitive.content
            val opened = logic.parseShare(f.getValue("link").jsonPrimitive.content)
            assertNotNull("$note no longer opens", opened)
            assertEquals(note, edition, opened!!.edition)
            val off = CIVS.filter { logic.inEdition(it, edition) && !opened.enabled[it.id] }.map { it.name }
            assertEquals(note, f.getValue("off").jsonArray.map { it.jsonPrimitive.content }, off)
            assertEquals(note, f.getValue("draws").jsonArray.map { idOf(it.jsonPrimitive.content) }, opened.draws)
        }
    }

    @Test
    fun `a share link can name every civ (format 1 spends one character per draw)`() {
        assertTrue("${CIVS.size} civs, but link format 1 can only name ${B64.length}", CIVS.size <= B64.length)
    }

    @Test
    fun `civ names and edition IDs are single words, as preset files split their lines on spaces`() {
        for (c in CIVS) assertTrue("civ ${c.id}", Regex("\\S+").matches(c.name) && !c.name.endsWith(":"))
        for (ed in EDITIONS) assertTrue(ed, Regex("\\S+").matches(ed))
    }
}
