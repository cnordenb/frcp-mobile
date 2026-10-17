package net.frcp.app.ui

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The images from the assets, each decoded once, on first use or ahead of it from another thread. They have mipmaps,
 * so the civ icons stay smooth at the small sizes of the logs and the Pool tab.
 */
class Images(private val assets: AssetManager) {
    private val bitmaps = ConcurrentHashMap<String, ImageBitmap>()
    private val slices = HashMap<String, NineSlice>()

    operator fun get(path: String): ImageBitmap = bitmaps.computeIfAbsent(path) {
        assets.open(path).use { BitmapFactory.decodeStream(it) }.apply { setHasMipMap(true) }.asImageBitmap()
    }

    fun nineSlice(path: String, slice: Int): NineSlice = slices.getOrPut(path) { NineSlice(get(path).asAndroidBitmap(), slice) }
}

val LocalImages = staticCompositionLocalOf<Images> { error("No images provided") }

/** How a nine-slice's edges fill their length, as CSS border-image-repeat: stretched, tiled, or tiled a whole number of times. */
enum class Tile { Stretch, Repeat, Round }

/**
 * An image cut 3×3 as CSS border-image cuts it: corners [slice] px square, the edges between them and the middle. It
 * draws the web version's wooden buttons, sunken logs, text fields and dialog panels at any size.
 */
class NineSlice(image: Bitmap, slice: Int) {
    private val slice = slice.toFloat()
    private val pieces: List<Bitmap>
    private val shaders: List<BitmapShader>
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val matrix = Matrix()
    private val rect = RectF()

    init {
        val xs = intArrayOf(0, slice, image.width - slice, image.width)
        val ys = intArrayOf(0, slice, image.height - slice, image.height)
        pieces = List(9) { i ->
            val c = i % 3
            val r = i / 3
            Bitmap.createBitmap(image, xs[c], ys[r], xs[c + 1] - xs[c], ys[r + 1] - ys[r])
        }
        shaders = pieces.map { BitmapShader(it, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT) }
    }

    /**
     * Draws the image over a [width]×[height] box with a [border] px frame. The corners stretch to the frame's size, the
     * edges run along it as [edges] says, and the middle, when [fill] is set, fills the rest as the edges do.
     */
    fun draw(canvas: Canvas, width: Float, height: Float, border: Float, edges: Tile, fill: Boolean) {
        val xs = floatArrayOf(0f, border, width - border, width)
        val ys = floatArrayOf(0f, border, height - border, height)
        val scale = border / slice
        for (i in 0 until 9) {
            if (i == 4 && !fill) continue
            val c = i % 3
            val r = i / 3
            rect.set(xs[c], ys[r], xs[c + 1], ys[r + 1])
            if (rect.width() <= 0f || rect.height() <= 0f) continue
            val piece = pieces[i]
            if ((c != 1 && r != 1) || edges == Tile.Stretch) {
                canvas.drawBitmap(piece, null, rect, paint)
                continue
            }
            // An edge tiles along its length and stretches across the frame. The middle tiles both ways.
            val tileW = if (c == 1) tile(rect.width(), piece.width * scale, edges) else rect.width()
            val tileH = if (r == 1) tile(rect.height(), piece.height * scale, edges) else rect.height()
            matrix.setScale(tileW / piece.width, tileH / piece.height)
            matrix.postTranslate(rect.left, rect.top)
            shaders[i].setLocalMatrix(matrix)
            paint.shader = shaders[i]
            canvas.drawRect(rect, paint)
            paint.shader = null
        }
    }

    /** The middle stretched over the box inset by [inset] px: the dialog panel's inlay, which reaches under its frame. */
    fun drawInlay(canvas: Canvas, width: Float, height: Float, inset: Float) {
        rect.set(inset, inset, width - inset, height - inset)
        canvas.drawBitmap(pieces[4], null, rect, paint)
    }

    private fun tile(length: Float, size: Float, edges: Tile) =
        if (edges == Tile.Round) length / max(1, (length / size).roundToInt()) else size
}

fun Modifier.nineSlice(slices: NineSlice, border: Dp, edges: Tile = Tile.Repeat, fill: Boolean = true) = drawBehind {
    val b = max(1, border.toPx().roundToInt()).toFloat()
    drawIntoCanvas { slices.draw(it.nativeCanvas, size.width, size.height, b, edges, fill) }
}

/** Draws the [w]×[h] px part of [image] at ([x], [y]) over the whole of this drawing's size, or over [dst]. */
fun DrawScope.drawPart(
    image: ImageBitmap, x: Int, y: Int, w: Int, h: Int,
    dstOffset: IntOffset = IntOffset.Zero,
    dstSize: IntSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null,
) = drawImage(image, IntOffset(x, y), IntSize(w, h), dstOffset, dstSize, alpha = alpha, colorFilter = colorFilter)

/** Draws cell [index] of an atlas of [cols] square cells per row over the whole of this drawing's size. */
fun DrawScope.drawCell(atlas: ImageBitmap, index: Int, cols: Int, colorFilter: ColorFilter? = null) {
    val cell = atlas.width / cols
    drawPart(atlas, index % cols * cell, index / cols * cell, cell, cell, colorFilter = colorFilter)
}
