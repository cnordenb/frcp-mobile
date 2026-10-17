package net.frcp.app.logic

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.text.Collator
import java.util.Locale
import kotlin.math.abs

// The picker's rules without the screen: the saved state, drawing, share links and preset files. A port of the web
// version's logic.js, rule for rule, so the two read and write the same links, saved data and presets. Nothing here
// touches the screen, storage or sound, so PickerViewModel runs the app on top of it and the unit tests check it.

const val LOG_CAP = 256
const val B64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
const val LINK_VERSION = "1"

/** A civ with what follows from its DLC: its edition, whether it has a legacy jingle, its files' name and its tech tree name. */
class Civ(val id: Int, val name: String, val dlc: String, val edition: String, val legacy: Boolean, val file: String, val techTree: String)

/** One draw in the log, shown as "Civ (k/n)", or as just "Civ" where k is 0. */
data class Draw(val id: Int, val k: Int, val n: Int)

/** The edition, the civs that are off, and the log: a list of rounds, oldest first. The last round is the current one. */
data class PickerState(var edition: String, val off: MutableSet<Int>, var log: MutableList<MutableList<Draw>>) {
    fun deepCopy() = PickerState(edition, off.toMutableSet(), log.mapTo(mutableListOf()) { it.toMutableList() })
}

/** The Options dialog's continuous freshness settings. */
data class Freshness(val fresh: Boolean, val freshN: Int)

data class SharedLink(val edition: String, val enabled: List<Boolean>, val draws: List<Int>)

data class Preset(val edition: String, val off: Set<Int>, val draws: List<Int>, val history: List<Int>)

/**
 * The rules for one data.json.
 *
 * A share link stores an edition by its place in the list, so new editions go at the end, and the first one is the
 * default. An edition has its own civs and those of every older edition. Civ IDs are permanent: share links and saved
 * data refer to them. A share link spends one base64url character per drawn civ, so past 64 civs the link format
 * version has to change. A civ's techTree name is the one aoe2techtree.net uses, where it differs.
 */
class Logic(data: GameData) {
    val editions: List<String> = data.editions.map { it.id }
    val editionInfo: Map<String, Edition> = data.editions.associateBy { it.id }
    val dlcs: Map<String, Dlc> = data.dlcs
    val civs: List<Civ> = data.civs.sortedBy { it.id }.mapIndexed { i, c ->
        require(c.id == i) { "Civ IDs must run from 0 with no gaps or repeats" }
        val edition = dlcs.getValue(c.dlc).edition
        Civ(c.id, c.name, c.dlc, edition, editionInfo.getValue(edition).legacyJingles, c.name.lowercase(Locale.ROOT), c.techTree ?: c.name)
    }
    val displayOrder: List<Civ> = civs.sortedWith(compareBy(Collator.getInstance(Locale.ENGLISH)) { it.name })

    fun inEdition(civ: Civ, ed: String) = editionInfo.getValue(civ.edition).year <= editionInfo.getValue(ed).year
    fun editionTotal(ed: String) = civs.count { inEdition(it, ed) }

    // ---- State ----

    fun emptyState() = PickerState(editions[0], mutableSetOf(), mutableListOf(mutableListOf()))

    fun applyEdition(s: PickerState) {
        for (c in civs) if (!inEdition(c, s.edition)) s.off += c.id
    }

    /** Reads back a saved state, keeping only the parts that make sense, or null where it isn't one. */
    fun parseState(raw: String?): PickerState? {
        val data = raw?.let { runCatching { Json.parseToJsonElement(it) }.getOrNull() } as? JsonObject ?: return null
        val edition = (data["edition"] as? JsonPrimitive)?.takeIf { it.isString }?.content
        if (edition == null || edition !in editions) return null
        val off = (data["disabled"] as? JsonArray)?.mapNotNullTo(mutableSetOf()) { civId(it) } ?: mutableSetOf()
        val log = mutableListOf<MutableList<Draw>>()
        for (r in data["log"] as? JsonArray ?: JsonArray(emptyList())) {
            if (r !is JsonArray) continue
            log += r.mapNotNullTo(mutableListOf()) { e ->
                if (e !is JsonArray) return@mapNotNullTo null
                val id = civId(e.getOrNull(0))
                val k = e.getOrNull(1).integer()
                val n = e.getOrNull(2).integer()
                if (id != null && k != null && n != null) Draw(id, k, n) else null
            }
        }
        val kept = log.filterIndexedTo(mutableListOf()) { i, r -> r.isNotEmpty() || i == log.lastIndex }
        if (kept.isEmpty()) kept += mutableListOf<Draw>()
        val seen = HashSet<Int>()
        kept[kept.lastIndex] = kept.last().filterTo(mutableListOf()) { seen.add(it.id) }
        return PickerState(edition, off, kept).also(::applyEdition)
    }

    fun serialiseState(s: PickerState): String = buildJsonObject {
        put("v", 1)
        put("edition", s.edition)
        putJsonArray("disabled") { s.off.sorted().forEach { add(it) } }
        putJsonArray("log") {
            for (r in s.log) addJsonArray { for (d in r) addJsonArray { add(d.id); add(d.k); add(d.n) } }
        }
    }.toString()

    fun hasContent(s: PickerState) = s.edition != editions[0] || s.off.isNotEmpty() || s.log.any { it.isNotEmpty() }

    fun isOn(s: PickerState, id: Int) = id !in s.off
    fun poolIds(s: PickerState) = civs.filter { isOn(s, it.id) }.map { it.id }
    fun round(s: PickerState) = s.log.last()
    fun drawnIds(s: PickerState) = round(s).map { it.id }
    fun drawnCount(s: PickerState) = drawnIds(s).count { isOn(s, it) }
    fun remainingIds(s: PickerState): List<Int> {
        val drawn = drawnIds(s).toSet()
        return poolIds(s).filter { it !in drawn }
    }

    fun closeRound(s: PickerState) {
        if (round(s).isNotEmpty()) s.log.add(mutableListOf())
    }

    /** Keeps the log's newest LOG_CAP draws, dropping the oldest first but never the current round. */
    fun trimLog(s: PickerState) {
        var total = s.log.sumOf { it.size }
        while (total > LOG_CAP && s.log.size > 1) {
            val oldest = s.log[0]
            if (oldest.isNotEmpty()) {
                oldest.removeAt(0)
                total--
            }
            if (oldest.isEmpty()) s.log.removeAt(0)
        }
    }

    // ---- Drawing ----

    fun freshMax(poolSize: Int) = maxOf(1, poolSize / 2)
    fun freshLimit(poolSize: Int, freshN: Int) = minOf(freshN, freshMax(poolSize))

    /** The most recently drawn civs, newest first, across round boundaries. */
    fun recentCivs(s: PickerState, n: Int): List<Int> {
        val out = mutableListOf<Int>()
        val seen = HashSet<Int>()
        for (r in s.log.indices.reversed()) {
            val entries = s.log[r]
            for (i in entries.indices.reversed()) {
                if (out.size >= n) return out
                if (seen.add(entries[i].id)) out += entries[i].id
            }
        }
        return out
    }

    /**
     * The civs the next draw picks from: those left this round, or the whole pool once the round is over. With
     * continuous freshness on, the last N civs drawn sit out. If that leaves nothing, fewer sit out, keeping the most
     * recent ones out.
     */
    fun drawCandidates(s: PickerState, f: Freshness): List<Int> {
        val pool = poolIds(s)
        val left = remainingIds(s).ifEmpty { pool }
        if (!f.fresh || pool.size < 2) return left
        val recent = recentCivs(s, freshLimit(pool.size, f.freshN))
        for (n in recent.size downTo 1) {
            val skip = recent.subList(0, n).toSet()
            val fresh = left.filter { it !in skip }
            if (fresh.isNotEmpty()) return fresh
        }
        return left
    }

    /** Adds a draw to the log, starting a new round first if this one is over. */
    fun recordDraw(s: PickerState, id: Int) {
        if (remainingIds(s).isEmpty()) closeRound(s)
        round(s).add(Draw(id, drawnCount(s) + 1, poolIds(s).size))
        trimLog(s)
    }

    // ---- Share links ----

    /**
     * #p= + format version + edition + pool bitfield (6 civs per character) + optional "." and one character per civ
     * drawn this round, in draw order. Civs past the end of a link's bitfield count as enabled, so new civs are
     * enabled when an old link is opened.
     */
    fun shareCode(s: PickerState, withDraws: Boolean): String {
        val code = StringBuilder(LINK_VERSION).append(B64[editions.indexOf(s.edition)])
        for (ch in 0 until (civs.size + 5) / 6) {
            var v = 0
            for (b in 0 until 6) {
                val id = ch * 6 + b
                if (id >= civs.size || isOn(s, id)) v = v or (1 shl b)
            }
            code.append(B64[v])
        }
        val draws = drawnIds(s).filter { isOn(s, it) }
        if (withDraws && draws.isNotEmpty()) code.append('.').append(draws.joinToString("") { B64[it].toString() })
        return code.toString()
    }

    /** Opens a link, with or without its address, or returns null for text that isn't one. */
    fun parseShare(text: String): SharedLink? {
        var s = text.trim()
        val hash = s.indexOf('#')
        if (hash >= 0) s = s.substring(hash + 1)
        s = s.removePrefix("p=")
        s = decodeUriComponent(s) ?: return null
        val m = LINK.matchEntire(s) ?: return null
        val ed = B64.indexOf(m.groupValues[1])
        if (ed >= editions.size) return null
        val edition = editions[ed]
        val bits = m.groupValues[2]
        val enabled = civs.map { c ->
            val ch = c.id / 6
            inEdition(c, edition) && (ch >= bits.length || ((B64.indexOf(bits[ch]) shr (c.id % 6)) and 1) == 1)
        }
        val draws = mutableListOf<Int>()
        for (ch in m.groupValues[3]) {
            val id = B64.indexOf(ch)
            if (id >= civs.size || !enabled[id] || id in draws) return null
            draws += id
        }
        return SharedLink(edition, enabled, draws)
    }

    /** A link's pool replaces this one, and its draws, if any, make a new round. Past rounds stay. */
    fun applyLink(s: PickerState, link: SharedLink) {
        s.edition = link.edition
        s.off.clear()
        for (c in civs) if (!link.enabled[c.id]) s.off += c.id
        applyEdition(s)
        closeRound(s)
        if (link.draws.isNotEmpty()) {
            val n = poolIds(s).size
            s.log[s.log.lastIndex] = link.draws.mapIndexedTo(mutableListOf()) { i, id -> Draw(id, i + 1, n) }
        }
        trimLog(s)
    }

    // ---- Presets ----

    /**
     * Preset files are the desktop app's .txt files: a section of every civ's name and on (1) or off (0), one of the
     * civs drawn this round in draw order, one of the edition's ID and, with continuous freshness on, one of its
     * history, oldest first and as many civs as could sit out of the pool. Lines end in CRLF, as the desktop app's do.
     */
    fun presetText(s: PickerState, fresh: Boolean): String {
        fun names(ids: List<Int>) = ids.map { civs[it].name }
        val lines = mutableListOf("CivStates:")
        civs.mapTo(lines) { "${it.name} ${if (isOn(s, it.id)) 1 else 0}" }
        lines += "DrawnCivs:"
        lines += names(drawnIds(s).filter { isOn(s, it) })
        lines += listOf("EditionState:", s.edition)
        if (fresh) {
            lines += "ContfreshCivArray:"
            lines += names(recentCivs(s, freshMax(poolIds(s).size)).reversed())
        }
        return lines.joinToString("") { "$it\r\n" }
    }

    /**
     * Reads a preset as the desktop app does: from every civ of the default edition on, through the sections in any
     * order, to the first empty line, passing over names it doesn't know. Text with none of the sections isn't a preset.
     */
    fun parsePreset(text: String): Preset? {
        var edition = editions[0]
        val off = mutableSetOf<Int>()
        val draws = mutableListOf<Int>()
        val history = mutableListOf<Int>()
        val ids = civs.associate { it.name to it.id }
        var section: String? = null
        for (raw in text.split(LINE_END)) {
            val line = raw.trim()
            if (line.isEmpty()) break
            PRESET_SECTIONS[line]?.let {
                section = it
                continue
            }
            val id = ids[line]
            when {
                section == "states" -> {
                    val parts = line.split(SPACES)
                    if (parts[0] in ids && isZero(parts.getOrNull(1))) off += ids.getValue(parts[0])
                }
                section == "draws" && id != null -> draws += id
                section == "history" && id != null -> history += id
                section == "edition" && line in editions -> edition = line
            }
        }
        return if (section != null) Preset(edition, off, draws, history) else null
    }

    /**
     * A loaded preset replaces the whole session, as on the desktop app: the pool and edition become the preset's, and
     * its draws this round. The civs of its continuous freshness history from before this round go in a round of their
     * own before it, without counts, as the file has none, so they keep sitting out of the next draws.
     */
    fun applyPreset(s: PickerState, preset: Preset) {
        s.edition = preset.edition
        s.off.clear()
        s.off += preset.off
        applyEdition(s)
        val n = poolIds(s).size
        val draws = preset.draws.filterIndexed { i, id -> isOn(s, id) && preset.draws.indexOf(id) == i }
        val earlier = preset.history.filterIndexed { i, id -> id !in draws && preset.history.lastIndexOf(id) == i }
        s.log = mutableListOf(draws.mapIndexedTo(mutableListOf()) { i, id -> Draw(id, i + 1, n) })
        if (earlier.isNotEmpty()) s.log.add(0, earlier.mapTo(mutableListOf()) { Draw(it, 0, 0) })
    }

    private fun civId(e: JsonElement?): Int? = e.integer()?.takeIf { it in civs.indices }

    private companion object {
        val LINK = Regex("1([A-Za-z0-9_-])([A-Za-z0-9_-]+)(?:\\.([A-Za-z0-9_-]+))?")
        val LINE_END = Regex("\r?\n")
        val SPACES = Regex("\\s+")
        val LEADING_INT = Regex("^[+-]?(\\d+)")
        val PRESET_SECTIONS = mapOf(
            "CivStates:" to "states", "DrawnCivs:" to "draws", "EditionState:" to "edition", "ContfreshCivArray:" to "history",
        )

        /** Whether JavaScript's parseInt(text, 10) reads text as 0, as the web version tests a civ's state. */
        fun isZero(text: String?) = text?.let { LEADING_INT.find(it) }?.groupValues?.get(1)?.all { it == '0' } == true

        /** A JSON number that is a whole number in Int's range, as JavaScript's Number.isInteger allows. */
        fun JsonElement?.integer(): Int? {
            val p = this as? JsonPrimitive ?: return null
            if (p.isString) return null
            val d = p.doubleOrNull ?: return null
            return if (d % 1.0 == 0.0 && abs(d) <= Int.MAX_VALUE) d.toInt() else null
        }

        /** JavaScript's decodeURIComponent: %XX escapes of UTF-8 bytes, or null where they're broken. */
        fun decodeUriComponent(s: String): String? {
            if ('%' !in s) return s
            val out = StringBuilder()
            var i = 0
            while (i < s.length) {
                if (s[i] != '%') {
                    out.append(s[i++])
                    continue
                }
                val bytes = ByteArrayOutputStream()
                while (i < s.length && s[i] == '%') {
                    if (i + 2 >= s.length) return null
                    val hi = Character.digit(s[i + 1], 16)
                    val lo = Character.digit(s[i + 2], 16)
                    if (hi < 0 || lo < 0) return null
                    bytes.write(hi * 16 + lo)
                    i += 3
                }
                val decoder = Charsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                out.append(runCatching { decoder.decode(ByteBuffer.wrap(bytes.toByteArray())) }.getOrNull() ?: return null)
            }
            return out.toString()
        }
    }
}

/**
 * The next draw is picked ahead of time, so its jingle can load before the draw is revealed. Each time it's asked for,
 * it's checked against the civs it can now be picked from and kept a fair pick from them, changing as little as it
 * can, so editing the pool doesn't load jingle after jingle: when civs drop out, it stays unless it's one of them;
 * when civs join, it moves to one of them with the chance a new pick would have had of landing on one; after any other
 * change, such as a draw, it's picked again. A re-roll asks with its own civs to pick from. Without a random source,
 * there's no pick.
 */
class Picker(private val rng: MersenneTwister?) {
    private var aheadFrom: List<Int> = emptyList()
    private var aheadId: Int? = null

    fun next(from: List<Int>): Int? {
        val rng = rng
        val before = aheadFrom.toSet()
        val joined = from.filter { it !in before }
        val dropped = from.size - joined.size < aheadFrom.size
        var id = aheadId
        if (from.isEmpty() || rng == null) id = null
        else if (id == null || (joined.isNotEmpty() && dropped)) id = from[rng.int(0, from.lastIndex)]
        else if (joined.isNotEmpty() && rng.int(0, from.lastIndex) >= aheadFrom.size) id = joined[rng.int(0, joined.lastIndex)]
        else if (joined.isEmpty() && id !in from) id = from[rng.int(0, from.lastIndex)]
        aheadFrom = from
        aheadId = id
        return id
    }
}
