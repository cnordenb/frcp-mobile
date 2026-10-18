package net.frcp.app.logic

import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt

// What the tests share, as the web version's tests/helpers.js: the app's data and rules, and a few helpers. Gradle
// runs unit tests from the module's folder, so the assets are at src/main/assets.

val ASSETS = File("src/main/assets")
val DATA_JSON: String = File(ASSETS, "data.json").readText()
val logic = Logic(GameData.parse(DATA_JSON))
val CIVS = logic.civs
val EDITIONS = logic.editions

fun fixture(name: String): String =
    checkNotNull(Logic::class.java.getResource("/fixtures/$name")) { "no fixture $name: run ./gradlew syncWebTestFixtures" }
        .readBytes().toString(Charsets.UTF_8)

fun idOf(name: String): Int = CIVS.firstOrNull { it.name == name }?.id ?: error("No civ named $name")
fun namesOf(ids: Iterable<Int>) = ids.map { CIVS[it].name }

/** A draw in a test's log, with the civ's name in place of its ID. */
data class Named(val name: String, val k: Int, val n: Int)

/** A state from civ names: the edition, the civs that are off, and the log's rounds, oldest first. */
fun makeState(edition: String = EDITIONS[0], off: List<String> = emptyList(), log: List<List<Named>> = listOf(emptyList())): PickerState {
    val s = PickerState(edition, off.mapTo(mutableSetOf(), ::idOf), log.mapTo(mutableListOf()) { r -> r.mapTo(mutableListOf()) { Draw(idOf(it.name), it.k, it.n) } })
    logic.applyEdition(s)
    return s
}

/** The log with names in place of IDs, for readable comparisons. */
fun namedLog(s: PickerState) = s.log.map { r -> r.map { Named(CIVS[it.id].name, it.k, it.n) } }

/** The same numbers on every run, so a statistical test either always passes or always fails. */
fun seeded(seed: Int = 20261016) = MersenneTwister(seed)

/** Pearson's chi-square statistic for counts that should all be equal. */
fun chiSquare(counts: Map<Int, Int>): Double {
    val expected = counts.values.sum().toDouble() / counts.size
    return counts.values.sumOf { (it - expected).pow(2) / expected }
}

/** The value chi-square stays under with probability 0.999 for df degrees of freedom (Wilson-Hilferty approximation). */
fun chiSquareLimit(df: Int): Double {
    val z = 3.09
    return df * (1 - 2.0 / (9 * df) + z * sqrt(2.0 / (9 * df))).pow(3)
}

/** A PNG file's pixels. */
fun image(rel: String): BufferedImage = checkNotNull(ImageIO.read(File(ASSETS, rel))) { "$rel isn't an image" }

/** Width and height from a PNG file's header. */
fun pngSize(rel: String): Pair<Int, Int> {
    val b = ByteBuffer.wrap(File(ASSETS, rel).readBytes())
    check(String(ByteArray(3) { b.get(1 + it) }, Charsets.US_ASCII) == "PNG") { "$rel isn't a PNG file" }
    return b.getInt(16) to b.getInt(20)
}

/** Width and height from a WebP file's header, for the lossy, lossless and extended formats. */
fun webpSize(rel: String): Pair<Int, Int> {
    val b = ByteBuffer.wrap(File(ASSETS, rel).readBytes()).order(ByteOrder.LITTLE_ENDIAN)
    fun ascii(at: Int) = String(ByteArray(4) { b.get(at + it) }, Charsets.US_ASCII)
    fun uint24(at: Int) = (b.get(at).toInt() and 0xff) or ((b.get(at + 1).toInt() and 0xff) shl 8) or ((b.get(at + 2).toInt() and 0xff) shl 16)
    check(ascii(0) == "RIFF" && ascii(8) == "WEBP") { "$rel isn't a WebP file" }
    return when (val chunk = ascii(12)) {
        "VP8X" -> 1 + uint24(24) to 1 + uint24(27)
        "VP8L" -> {
            val bits = b.getInt(21)
            1 + (bits and 0x3fff) to 1 + ((bits ushr 14) and 0x3fff)
        }
        "VP8 " -> (b.getShort(26).toInt() and 0x3fff) to (b.getShort(28).toInt() and 0x3fff)
        else -> error("$rel has an unknown WebP chunk $chunk")
    }
}
