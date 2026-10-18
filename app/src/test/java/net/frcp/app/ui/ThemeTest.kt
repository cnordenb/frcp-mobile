package net.frcp.app.ui

import androidx.compose.ui.graphics.Color
import net.frcp.app.Assets
import net.frcp.app.logic.image
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

// The text colours against what they're written on, at WCAG 2.2's 1.4.3 Contrast (Minimum): 4.5 to 1 for text. The
// page's colour is the page art's average under the tabs' content. The dialogs' text is left out, as their panel is a
// WebP image, which the tests can't read.
class ThemeTest {
    private val parchment: Color by lazy {
        val art = image(Assets.page("background"))
        val sums = DoubleArray(3)
        var n = 0
        for (y in Page.INNER_TOP.toInt() until Page.PAGE_BOTTOM.toInt() step 4) {
            for (x in Page.INNER_LEFT.toInt() until Page.INNER_RIGHT.toInt() step 4) {
                val argb = art.getRGB(x, y)
                sums[0] += argb shr 16 and 0xff
                sums[1] += argb shr 8 and 0xff
                sums[2] += argb and 0xff
                n++
            }
        }
        Color((sums[0] / n / 255).toFloat(), (sums[1] / n / 255).toFloat(), (sums[2] / n / 255).toFloat())
    }

    /** WCAG's relative luminance of an sRGB colour. */
    private fun luminance(c: Color): Double {
        fun linear(v: Float) = if (v <= 0.04045f) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        return 0.2126 * linear(c.red) + 0.7152 * linear(c.green) + 0.0722 * linear(c.blue)
    }

    private fun contrast(a: Color, b: Color): Double {
        val (light, dark) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (light + 0.05) / (dark + 0.05)
    }

    private fun assertReadable(pairs: List<Triple<String, Color, Color>>) {
        for ((what, text, back) in pairs) {
            val ratio = contrast(text, back)
            assertTrue("$what is only %.2f to 1".format(ratio), ratio >= 4.5)
        }
    }

    @Test
    fun `text on the page has WCAG's minimum contrast`() {
        assertReadable(
            listOf(
                Triple("the page's text", Ink.Text, parchment),
                Triple("the swipe hints", Ink.Brown, parchment),
            ),
        )
    }

    @Test
    fun `text on the menus, logs, fields and status messages has WCAG's minimum contrast`() {
        assertReadable(
            listOf(
                Triple("a menu's items", Ink.Cream, Ink.Menu),
                Triple("a pressed menu item", Ink.Text, Ink.Gold),
                Triple("the logs' lines", Ink.LogText, Ink.LogBack),
                Triple("a text field's text", Color.White, Ink.Field),
                Triple("a status message", Color.White, Ink.Toast),
            ),
        )
    }
}
