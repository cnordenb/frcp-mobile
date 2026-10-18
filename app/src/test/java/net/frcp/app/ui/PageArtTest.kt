package net.frcp.app.ui

import androidx.compose.ui.unit.dp
import net.frcp.app.Assets
import net.frcp.app.logic.image
import net.frcp.app.logic.pngSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.image.BufferedImage
import kotlin.math.abs

// The page art, background.png, and its tab strips, draw_tab.png and so on, against the places Page measures
// on them: their sizes, the seam where a strip meets the page, the tabs under their touch boxes and the parchment
// under the tabs' content. New art that fails these needs Page's places measured again.
class PageArtTest {
    private val page = image(Assets.page("background"))
    private val strips = (0..2).map { image(Page.strip(it)) }
    private val stripTop = Page.STRIP_TOP.toInt()

    private fun alpha(argb: Int) = argb ushr 24
    private fun channels(argb: Int) = intArrayOf(argb shr 16 and 0xff, argb shr 8 and 0xff, argb and 0xff)

    /** The art's opacity at (x, y) with the strip drawn over the page. */
    private fun opacity(strip: BufferedImage, x: Int, y: Int): Float {
        val p = alpha(page.getRGB(x, y)) / 255f
        if (y < stripTop) return p
        val s = alpha(strip.getRGB(x, y - stripTop)) / 255f
        return s + p * (1 - s)
    }

    @Test
    fun `the page and its strips are the sizes Page measures them in`() {
        assertEquals(Page.ART_WIDTH.toInt() to Page.ART_HEIGHT.toInt(), pngSize(Assets.page("background")))
        for (t in 0..2) assertEquals(Page.ART_WIDTH.toInt() to Page.STRIP_HEIGHT.toInt(), pngSize(Page.strip(t)))
        assertEquals(Page.ART_HEIGHT, Page.STRIP_TOP + Page.STRIP_HEIGHT, 0f)
    }

    @Test
    fun `each strip's top rows are the page's own, so it meets the page without a seam`() {
        for ((t, strip) in strips.withIndex()) {
            var worst = 0
            for (y in 0 until SEAM_ROWS) {
                for (x in 0 until strip.width) {
                    val s = strip.getRGB(x, y)
                    val p = page.getRGB(x, stripTop + y)
                    worst = maxOf(worst, abs(alpha(s) - alpha(p)))
                    if (alpha(s) == 255 && alpha(p) == 255) {
                        val a = channels(s)
                        val b = channels(p)
                        for (c in 0..2) worst = maxOf(worst, abs(a[c] - b[c]))
                    }
                }
            }
            assertTrue("strip $t differs from the page by up to $worst in its top $SEAM_ROWS rows", worst <= 2)
        }
    }

    @Test
    fun `each tab's touch box covers its drawn tab, and leaves out the gaps between tabs`() {
        // The middle of the tabs' labels
        val row = ((Page.TAB_TOP + Page.TAB_BOTTOM) / 2 - Page.STRIP_TOP).toInt()
        for ((t, strip) in strips.withIndex()) {
            for ((l, r) in Page.TABS) {
                val columns = l.toInt()..r.toInt()
                val opaque = columns.count { alpha(strip.getRGB(it, row)) == 255 }
                assertTrue("strip $t: the tab box $l to $r is over $opaque opaque pixels of ${columns.count()}", opaque >= 0.95 * columns.count())
            }
            for ((a, b) in Page.TABS.zipWithNext()) {
                assertTrue(a.second < b.first)
                val gap = ((a.second + b.first) / 2).toInt()
                assertTrue("strip $t: the gap between tabs at $gap px is see-through", alpha(strip.getRGB(gap, row)) < 255)
            }
        }
    }

    @Test
    fun `the tabs' content lies on the page, which is opaque all along the content's edges`() {
        val left = Page.INNER_LEFT.toInt()
        val right = Page.INNER_RIGHT.toInt() - 1
        val top = Page.INNER_TOP.toInt()
        val bottom = Page.PAGE_BOTTOM.toInt() - 1
        for ((t, strip) in strips.withIndex()) {
            val edges = (left..right).flatMap { listOf(it to top, it to bottom) } + (top..bottom).flatMap { listOf(left to it, right to it) }
            val (x, y) = edges.minBy { (x, y) -> opacity(strip, x, y) }
            assertTrue("with strip $t, the art at ($x, $y) is only ${opacity(strip, x, y)} opaque", opacity(strip, x, y) >= 250 / 255f)
        }
    }

    @Test
    fun `the crop takes in all of the art that shows`() {
        val crop = Page.CROP_LEFT.toInt() until Page.CROP_RIGHT.toInt()
        val cropRows = Page.CROP_TOP.toInt() until Page.CROP_BOTTOM.toInt()
        for ((t, strip) in strips.withIndex()) {
            for (y in 0 until page.height) {
                for (x in 0 until page.width) {
                    if (x in crop && y in cropRows) continue
                    assertTrue("with strip $t, ($x, $y) shows outside the crop", opacity(strip, x, y) < SHOWS)
                }
            }
        }
    }

    @Test
    fun `the crop leaves no margin of its own, as the art shows at each of its edges`() {
        val left = Page.CROP_LEFT.toInt()
        val right = Page.CROP_RIGHT.toInt() - 1
        val top = Page.CROP_TOP.toInt()
        val bottom = Page.CROP_BOTTOM.toInt() - 1
        val strip = strips[0]
        assertTrue((top..bottom).any { opacity(strip, left, it) >= SHOWS })
        assertTrue((top..bottom).any { opacity(strip, right, it) >= SHOWS })
        assertTrue((left..right).any { opacity(strip, it, top) >= SHOWS })
        assertTrue((left..right).any { opacity(strip, it, bottom) >= SHOWS })
    }

    @Test
    fun `Page scales the crop to the size it's drawn at`() {
        val cropWidth = Page.CROP_RIGHT - Page.CROP_LEFT
        val cropHeight = Page.CROP_BOTTOM - Page.CROP_TOP
        val half = Page((cropWidth / 2).dp, (cropHeight / 2).dp)
        assertEquals((Page.INNER_LEFT - Page.CROP_LEFT) / 2, half.left.value, 0.001f)
        assertEquals((Page.CROP_RIGHT - Page.INNER_RIGHT) / 2, half.right.value, 0.001f)
        assertEquals((Page.INNER_TOP - Page.CROP_TOP) / 2, half.top.value, 0.001f)
        assertEquals((Page.CROP_BOTTOM - Page.PAGE_BOTTOM) / 2, half.bottom.value, 0.001f)
        assertEquals((Page.TAB_TOP - Page.CROP_TOP) / 2, half.tabTop.value, 0.001f)
    }

    private companion object {
        /** How many of a strip's top rows to hold to the page's: all three strips keep to it for their top 45. */
        const val SEAM_ROWS = 40

        /** How opaque a pixel is when it shows, as Page's crop counts it: half opaque or more. */
        const val SHOWS = 128 / 255f
    }
}
