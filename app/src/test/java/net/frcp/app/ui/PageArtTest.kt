package net.frcp.app.ui

import androidx.compose.ui.unit.dp
import net.frcp.app.device.Assets
import net.frcp.app.logic.image
import net.frcp.app.logic.pngSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.awt.image.BufferedImage
import kotlin.math.abs

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PageArtTest {
    private val page = image(Assets.page("background"))
    private val strips = (0..2).map { image(Page.strip(it)) }
    private val stripTop = Page.STRIP_TOP.toInt()
    private val labelRow = ((Page.TAB_TOP + Page.TAB_BOTTOM) / 2 - Page.STRIP_TOP).toInt()
    private val cropLeft = Page.CROP_LEFT.toInt()
    private val cropRight = Page.CROP_RIGHT.toInt() - 1
    private val cropTop = Page.CROP_TOP.toInt()
    private val cropBottom = Page.CROP_BOTTOM.toInt() - 1
    private val halfWidth = (Page.CROP_RIGHT - Page.CROP_LEFT) / 2
    private val halfHeight = (Page.CROP_BOTTOM - Page.CROP_TOP) / 2
    private val halfPage = Page(halfWidth.dp, halfHeight.dp)

    private fun alpha(argb: Int) = argb ushr 24
    private fun channels(argb: Int) = listOf(argb shr 16 and 0xff, argb shr 8 and 0xff, argb and 0xff)

    private fun opacity(strip: BufferedImage, x: Int, y: Int): Float {
        val p = alpha(page.getRGB(x, y)) / 255f
        if (y < stripTop) return p
        val s = alpha(strip.getRGB(x, y - stripTop)) / 255f
        return s + p * (1 - s)
    }

    private fun difference(s: Int, p: Int): Int {
        val alphas = abs(alpha(s) - alpha(p))
        if (alpha(s) != 255 || alpha(p) != 255) return alphas
        return maxOf(alphas, channels(s).zip(channels(p)).maxOf { (a, b) -> abs(a - b) })
    }

    private fun seamDifference(strip: BufferedImage) = (0 until SEAM_ROWS).maxOf { y ->
        (0 until strip.width).maxOf { x -> difference(strip.getRGB(x, y), page.getRGB(x, stripTop + y)) }
    }

    private fun firstShowingOutsideTheCrop(strip: BufferedImage): String? {
        for (y in 0 until page.height) {
            for (x in 0 until page.width) {
                val inside = x in cropLeft..cropRight && y in cropTop..cropBottom
                if (!inside && opacity(strip, x, y) >= SHOWS) return "($x, $y)"
            }
        }
        return null
    }

    @Test
    fun t278_pageIsTheSizePageMeasuresItIn() {
        assertEquals(Page.ART_WIDTH.toInt() to Page.ART_HEIGHT.toInt(), pngSize(Assets.page("background")))
    }

    @Test
    fun t279_stripsAreTheSizePageMeasuresThemIn() {
        val size = Page.ART_WIDTH.toInt() to Page.STRIP_HEIGHT.toInt()
        assertEquals(listOf(size, size, size), (0..2).map { pngSize(Page.strip(it)) })
    }

    @Test
    fun t280_stripsReachFromTheirTopToThePagesBottom() {
        assertEquals(Page.ART_HEIGHT, Page.STRIP_TOP + Page.STRIP_HEIGHT, 0f)
    }

    @Test
    fun t281_eachStripsTopRowsAreThePagesOwnSoItMeetsThePageWithoutASeam() {
        val seams = strips.withIndex().filter { (_, strip) -> seamDifference(strip) > 2 }
            .map { (t, strip) -> "strip $t differs from the page by up to ${seamDifference(strip)}" }
        assertEquals(emptyList<String>(), seams)
    }

    @Test
    fun t282_eachTabsTouchBoxCoversItsDrawnTab() {
        val uncovered = strips.withIndex().flatMap { (t, strip) ->
            Page.TABS.filter { (l, r) ->
                val columns = l.toInt()..r.toInt()
                columns.count { alpha(strip.getRGB(it, labelRow)) == 255 } < 0.95 * columns.count()
            }.map { (l, r) -> "strip $t: the tab box $l to $r" }
        }
        assertEquals(emptyList<String>(), uncovered)
    }

    @Test
    fun t283_tabsTouchBoxesRunLeftToRightWithoutOverlap() {
        val overlaps = Page.TABS.zipWithNext().filter { (a, b) -> a.second >= b.first }.map { (a, b) -> "$a and $b" }
        assertEquals(emptyList<String>(), overlaps)
    }

    @Test
    fun t284_gapsBetweenTheTabsAreSeeThrough() {
        val opaque = strips.withIndex().flatMap { (t, strip) ->
            Page.TABS.zipWithNext().map { (a, b) -> ((a.second + b.first) / 2).toInt() }
                .filter { alpha(strip.getRGB(it, labelRow)) == 255 }
                .map { "strip $t: the gap at $it px" }
        }
        assertEquals(emptyList<String>(), opaque)
    }

    @Test
    fun t285_pageIsOpaqueAllAlongTheEdgesOfTheTabsContent() {
        val left = Page.INNER_LEFT.toInt()
        val right = Page.INNER_RIGHT.toInt() - 1
        val top = Page.INNER_TOP.toInt()
        val bottom = Page.PAGE_BOTTOM.toInt() - 1
        val rows = (left..right).flatMap { listOf(it to top, it to bottom) }
        val edges = rows + (top..bottom).flatMap { listOf(left to it, right to it) }
        val faint = strips.withIndex().mapNotNull { (t, strip) ->
            val (x, y) = edges.minBy { (x, y) -> opacity(strip, x, y) }
            val least = opacity(strip, x, y)
            "with strip $t, ($x, $y) is only $least opaque".takeIf { least < 250 / 255f }
        }
        assertEquals(emptyList<String>(), faint)
    }

    @Test
    fun t286_cropTakesInAllOfTheArtThatShows() {
        val outside = strips.withIndex().mapNotNull { (t, strip) ->
            firstShowingOutsideTheCrop(strip)?.let { "with strip $t, $it shows outside the crop" }
        }
        assertEquals(emptyList<String>(), outside)
    }

    @Test
    fun t287_artShowsAtTheCropsLeftEdge() {
        assertTrue((cropTop..cropBottom).any { opacity(strips[0], cropLeft, it) >= SHOWS })
    }

    @Test
    fun t288_artShowsAtTheCropsRightEdge() {
        assertTrue((cropTop..cropBottom).any { opacity(strips[0], cropRight, it) >= SHOWS })
    }

    @Test
    fun t289_artShowsAtTheCropsTopEdge() {
        assertTrue((cropLeft..cropRight).any { opacity(strips[0], it, cropTop) >= SHOWS })
    }

    @Test
    fun t290_artShowsAtTheCropsBottomEdge() {
        assertTrue((cropLeft..cropRight).any { opacity(strips[0], it, cropBottom) >= SHOWS })
    }

    @Test
    fun t291_pageScalesItsLeftMarginToTheSizeItIsDrawnAt() {
        assertEquals((Page.INNER_LEFT - Page.CROP_LEFT) / 2, halfPage.left.value, 0.001f)
    }

    @Test
    fun t292_pageScalesItsRightMarginToTheSizeItIsDrawnAt() {
        assertEquals((Page.CROP_RIGHT - Page.INNER_RIGHT) / 2, halfPage.right.value, 0.001f)
    }

    @Test
    fun t293_pageScalesItsTopMarginToTheSizeItIsDrawnAt() {
        assertEquals((Page.INNER_TOP - Page.CROP_TOP) / 2, halfPage.top.value, 0.001f)
    }

    @Test
    fun t294_pageScalesItsBottomMarginToTheSizeItIsDrawnAt() {
        assertEquals((Page.CROP_BOTTOM - Page.PAGE_BOTTOM) / 2, halfPage.bottom.value, 0.001f)
    }

    @Test
    fun t295_pageScalesTheTabsTopToTheSizeItIsDrawnAt() {
        assertEquals((Page.TAB_TOP - Page.CROP_TOP) / 2, halfPage.tabTop.value, 0.001f)
    }

    private companion object {
        const val SEAM_ROWS = 40
        const val SHOWS = 128 / 255f
    }
}
