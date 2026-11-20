package net.frcp.app.logic

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class Edition(
    val id: String,
    val name: String,
    val short: String,
    val year: Int,
    val series: String,
    val url: String?,
    val storeNote: String?,
    val legacyJingles: Boolean,
    val dlcToggles: List<String>,
    val olderDlcs: List<String>,
    val toggleSize: Int,
    val toggleGap: Int,
)

class Dlc(val key: String, val name: String, val short: String, val edition: String, val hasIcon: Boolean)

class CivEntry(val id: Int, val name: String, val dlc: String, val techTree: String?)

class GameData(val editions: List<Edition>, val dlcs: Map<String, Dlc>, val civs: List<CivEntry>) {
    companion object {
        fun parse(json: String): GameData {
            val root = Json.parseToJsonElement(json).jsonObject
            val editions = root.getValue("editions").jsonArray.map { element ->
                val e = element.jsonObject
                val layout = e.getValue("dlcToggleLayout").jsonObject
                Edition(
                    id = e.string("id"),
                    name = e.string("name"),
                    short = e.string("short"),
                    year = e.getValue("year").jsonPrimitive.int,
                    series = e.string("series"),
                    url = e.stringOrNull("url"),
                    storeNote = e.stringOrNull("storeNote"),
                    legacyJingles = e["legacyJingles"]?.jsonPrimitive?.booleanOrNull == true,
                    dlcToggles = e.strings("dlcToggles"),
                    olderDlcs = e.strings("olderDlcs"),
                    toggleSize = layout.getValue("size").jsonPrimitive.int,
                    toggleGap = layout.getValue("gap").jsonPrimitive.int,
                )
            }
            val dlcs = root.getValue("dlcs").jsonObject.mapValues { (key, element) ->
                val d = element.jsonObject
                val name = d.string("name")
                Dlc(key, name, d.stringOrNull("short") ?: name, d.string("edition"), d["icon"] != null)
            }
            val civs = root.getValue("civs").jsonArray.map { element ->
                val c = element.jsonObject
                val id = c.getValue("id").jsonPrimitive.int
                CivEntry(id, c.string("name"), c.string("dlc"), c.stringOrNull("techTree"))
            }
            return GameData(editions, dlcs, civs)
        }

        private fun JsonObject.string(key: String) = getValue(key).jsonPrimitive.content
        private fun JsonObject.stringOrNull(key: String) = get(key)?.jsonPrimitive?.contentOrNull
        private fun JsonObject.strings(key: String) = getValue(key).jsonArray.map { it.jsonPrimitive.content }
    }
}
