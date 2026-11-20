package net.frcp.app.logic

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CompatTest {
    private val frozen = Json.parseToJsonElement(fixture("civ-ids.json")).jsonArray.map { it.jsonPrimitive.content }
    private val links = Json.parseToJsonElement(fixture("share-links.json")).jsonArray.map { it.jsonObject }
    private val word = Regex("\\S+")

    private fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.content
    private fun JsonObject.texts(key: String) = getValue(key).jsonArray.map { it.jsonPrimitive.content }
    private fun opened(f: JsonObject) = logic.parseShare(f.text("link"))

    private fun notesWhere(differs: (JsonObject, SharedLink) -> Boolean) =
        links.filter { f -> opened(f)?.let { differs(f, it) } == true }.map { it.text("note") }

    @Test
    fun t001_frozenCivsAreStillThere() {
        val gone = frozen.filterIndexed { id, _ -> id >= CIVS.size }
        assertEquals("share links and saved data still refer to these civs", emptyList<String>(), gone)
    }

    @Test
    fun t002_frozenCivsKeepTheirIds() {
        val names = CIVS.take(frozen.size).map { it.name }
        assertEquals("share links and saved data refer to civs by ID", frozen.take(CIVS.size), names)
    }

    @Test
    fun t003_everyCivHasItsIdFrozen() {
        val unfrozen = CIVS.drop(frozen.size).map { it.name }
        assertEquals("not in the frozen civ IDs: add them in the web version", emptyList<String>(), unfrozen)
    }

    @Test
    fun t004_earlierLinksStillOpen() {
        val unopened = links.filter { opened(it) == null }.map { it.text("note") }
        assertEquals(emptyList<String>(), unopened)
    }

    @Test
    fun t005_earlierLinksOpenWithTheirEdition() {
        assertEquals(emptyList<String>(), notesWhere { f, link -> link.edition != f.text("edition") })
    }

    @Test
    fun t006_earlierLinksOpenWithTheirCivsOff() {
        val wrong = notesWhere { f, link ->
            val edition = f.text("edition")
            val off = CIVS.filter { logic.inEdition(it, edition) && !link.enabled[it.id] }.map { it.name }
            off != f.texts("off")
        }
        assertEquals(emptyList<String>(), wrong)
    }

    @Test
    fun t007_earlierLinksOpenWithTheirDraws() {
        assertEquals(emptyList<String>(), notesWhere { f, link -> link.draws != f.texts("draws").map(::idOf) })
    }

    @Test
    fun t008_aLinkCanNameEveryCiv() {
        assertTrue("${CIVS.size} civs, but link format 1 can only name ${B64.length}", CIVS.size <= B64.length)
    }

    @Test
    fun t009_civNamesAreSingleWords() {
        val broken = CIVS.filter { !word.matches(it.name) || it.name.endsWith(":") }.map { it.name }
        assertEquals(emptyList<String>(), broken)
    }

    @Test
    fun t010_editionIdsAreSingleWords() {
        assertEquals(emptyList<String>(), EDITIONS.filter { !word.matches(it) })
    }
}
