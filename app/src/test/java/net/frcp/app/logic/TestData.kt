package net.frcp.app.logic

import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt

val ASSETS = File("src/main/assets")
val DATA_JSON: String = File(ASSETS, "data.json").readText()
val logic = Logic(GameData.parse(DATA_JSON))
val CIVS = logic.civs
val EDITIONS = logic.editions

fun fixture(name: String): String {
    val found = Logic::class.java.getResource("/fixtures/$name")
    return checkNotNull(found) { "no fixture $name: run ./gradlew syncWebTestFixtures" }
        .readBytes().toString(Charsets.UTF_8)
}

fun idOf(name: String): Int = CIVS.firstOrNull { it.name == name }?.id ?: error("No civ named $name")
fun namesOf(ids: Iterable<Int>) = ids.map { CIVS[it].name }

data class Named(val name: String, val k: Int, val n: Int)

fun makeState(
    edition: String = EDITIONS[0],
    off: List<String> = emptyList(),
    log: List<List<Named>> = listOf(emptyList()),
): PickerState {
    val rounds = log.mapTo(mutableListOf()) { r -> r.mapTo(mutableListOf()) { Draw(idOf(it.name), it.k, it.n) } }
    val s = PickerState(edition, off.mapTo(mutableSetOf(), ::idOf), rounds)
    logic.applyEdition(s)
    return s
}

fun namedLog(s: PickerState) = s.log.map { r -> r.map { Named(CIVS[it.id].name, it.k, it.n) } }

fun allBut(vararg names: String) = CIVS.filter { it.name !in names }.map { it.name }

fun seeded(seed: Int = 20261016) = MersenneTwister(seed)

fun chiSquare(counts: Map<Int, Int>): Double {
    val expected = counts.values.sum().toDouble() / counts.size
    return counts.values.sumOf { (it - expected).pow(2) / expected }
}

fun chiSquareLimit(df: Int): Double {
    val z = 3.09
    return df * (1 - 2.0 / (9 * df) + z * sqrt(2.0 / (9 * df))).pow(3)
}

fun image(rel: String): BufferedImage = checkNotNull(ImageIO.read(File(ASSETS, rel))) { "$rel isn't an image" }

fun pngSize(rel: String): Pair<Int, Int> {
    val b = ByteBuffer.wrap(File(ASSETS, rel).readBytes())
    check(String(ByteArray(3) { b.get(1 + it) }, Charsets.US_ASCII) == "PNG") { "$rel isn't a PNG file" }
    return b.getInt(16) to b.getInt(20)
}

fun webpSize(rel: String): Pair<Int, Int> {
    val b = ByteBuffer.wrap(File(ASSETS, rel).readBytes()).order(ByteOrder.LITTLE_ENDIAN)
    fun ascii(at: Int) = String(ByteArray(4) { b.get(at + it) }, Charsets.US_ASCII)
    fun byte(at: Int) = b.get(at).toInt() and 0xff
    fun uint24(at: Int) = byte(at) or (byte(at + 1) shl 8) or (byte(at + 2) shl 16)
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
