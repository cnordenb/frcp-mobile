package net.frcp.app.ui

import androidx.compose.ui.graphics.Color
import net.frcp.app.device.Assets
import net.frcp.app.logic.image
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import kotlin.math.pow

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UiContrastTest {
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

    private fun luminance(c: Color): Double {
        fun linear(v: Float) = if (v <= 0.04045f) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        return 0.2126 * linear(c.red) + 0.7152 * linear(c.green) + 0.0722 * linear(c.blue)
    }

    private fun contrast(a: Color, b: Color): Double {
        val (light, dark) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (light + 0.05) / (dark + 0.05)
    }

    private fun tooFaint(text: Color, back: Color) = "only %.2f to 1".format(contrast(text, back))

    @Test
    fun t344_pageTextHasMinimumContrastOnTheParchment() {
        assertTrue(tooFaint(Ink.Text, parchment), contrast(Ink.Text, parchment) >= MINIMUM)
    }

    @Test
    fun t345_swipeHintsHaveMinimumContrastOnTheParchment() {
        assertTrue(tooFaint(Ink.Brown, parchment), contrast(Ink.Brown, parchment) >= MINIMUM)
    }

    @Test
    fun t346_menuItemsHaveMinimumContrast() {
        assertTrue(tooFaint(Ink.Cream, Ink.Menu), contrast(Ink.Cream, Ink.Menu) >= MINIMUM)
    }

    @Test
    fun t347_pressedMenuItemHasMinimumContrast() {
        assertTrue(tooFaint(Ink.Text, Ink.Gold), contrast(Ink.Text, Ink.Gold) >= MINIMUM)
    }

    @Test
    fun t348_logLinesHaveMinimumContrast() {
        assertTrue(tooFaint(Ink.LogText, Ink.LogBack), contrast(Ink.LogText, Ink.LogBack) >= MINIMUM)
    }

    @Test
    fun t349_textFieldTextHasMinimumContrast() {
        assertTrue(tooFaint(Color.White, Ink.Field), contrast(Color.White, Ink.Field) >= MINIMUM)
    }

    @Test
    fun t350_statusMessageHasMinimumContrast() {
        assertTrue(tooFaint(Color.White, Ink.Toast), contrast(Color.White, Ink.Toast) >= MINIMUM)
    }

    private companion object {
        const val MINIMUM = 4.5
    }
}
