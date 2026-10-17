package net.frcp.app

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put
import net.frcp.app.logic.Freshness

/**
 * The user's settings, with the web version's names and defaults: continuous freshness on at the most, legacy jingles,
 * and neither a draw nor a new round when the app opens. The web version's tooltip and hotkey settings have no use on a
 * phone, so the app leaves them out.
 */
data class Settings(
    val showName: Boolean = true,
    val showIcon: Boolean = true,
    val showCounter: Boolean = true,
    val jingles: Boolean = true,
    val legacyJingles: Boolean = true,
    val uiSounds: Boolean = true,
    val drawOnLoad: Boolean = false,
    val newRoundOnVisit: Boolean = false,
    val fresh: Boolean = true,
    val freshN: Int = FRESH_MOST,
    val showRemaining: Boolean = true,
    val autoReset: Boolean = false,
    val autoToggle: Boolean = false,
) {
    val freshness get() = Freshness(fresh, freshN)

    fun toJson(): String = buildJsonObject {
        put("v", 1)
        put("showName", showName)
        put("showIcon", showIcon)
        put("showCounter", showCounter)
        put("jingles", jingles)
        put("legacyJingles", legacyJingles)
        put("uiSounds", uiSounds)
        put("drawOnLoad", drawOnLoad)
        put("newRoundOnVisit", newRoundOnVisit)
        put("fresh", fresh)
        put("freshN", freshN)
        put("showRemaining", showRemaining)
        put("autoReset", autoReset)
        put("autoToggle", autoToggle)
    }.toString()

    companion object {
        /** A freshness setting at or above half the pool means "the most", so it follows the pool's size. */
        const val FRESH_MOST = 1000

        /** Reads saved settings, keeping the default of each one that's missing or of the wrong type. */
        fun parse(raw: String?): Settings {
            val d = Settings()
            val data = raw?.let { runCatching { Json.parseToJsonElement(it) }.getOrNull() } as? JsonObject ?: return d
            fun bool(key: String, default: Boolean) =
                (data[key] as? JsonPrimitive)?.takeIf { !it.isString }?.booleanOrNull ?: default
            val n = (data["freshN"] as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull
            return Settings(
                showName = bool("showName", d.showName),
                showIcon = bool("showIcon", d.showIcon),
                showCounter = bool("showCounter", d.showCounter),
                jingles = bool("jingles", d.jingles),
                legacyJingles = bool("legacyJingles", d.legacyJingles),
                uiSounds = bool("uiSounds", d.uiSounds),
                drawOnLoad = bool("drawOnLoad", d.drawOnLoad),
                newRoundOnVisit = bool("newRoundOnVisit", d.newRoundOnVisit),
                fresh = bool("fresh", d.fresh),
                freshN = when {
                    n == null -> d.freshN
                    n % 1.0 != 0.0 || n < 1 -> 1
                    else -> n.coerceAtMost(FRESH_MOST.toDouble()).toInt()
                },
                showRemaining = bool("showRemaining", d.showRemaining),
                autoReset = bool("autoReset", d.autoReset),
                autoToggle = bool("autoToggle", d.autoToggle),
            )
        }
    }
}
