package net.frcp.app.ui.widgets

import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UiSliderTest {
    private val imageWidth = 340f
    private val grooveStart = 32f
    private val grooveEnd = 308f

    @Test
    fun t296_knobAtTheMinimumSitsAtTheGroovesStart() {
        assertEquals(grooveStart / imageWidth, sliderKnobAt(1, 1, 28), 1e-6f)
    }

    @Test
    fun t297_knobAtTheMaximumSitsAtTheGroovesEnd() {
        assertEquals(grooveEnd / imageWidth, sliderKnobAt(28, 1, 28), 1e-6f)
    }

    @Test
    fun t298_knobBetweenTheEndsSitsInProportionAlongTheGroove() {
        assertEquals((grooveStart + 276f / 3) / imageWidth, sliderKnobAt(10, 1, 28), 1e-6f)
    }

    @Test
    fun t299_sliderWithASingleValueKeepsItsKnobAtTheStart() {
        assertEquals(grooveStart / imageWidth, sliderKnobAt(5, 5, 5), 1e-6f)
    }

    @Test
    fun t300_sliderWithASingleValueReadsThatValue() {
        assertEquals(5, sliderValueAt(200f, imageWidth, 5, 5))
    }

    @Test
    fun t301_fingerOnTheKnobReadsItsValueForEveryValueAndWidth() {
        val misread = listOf(240f, 340f, 1000f).flatMap { width ->
            (1..28).filter { v -> sliderValueAt(width * sliderKnobAt(v, 1, 28), width, 1, 28) != v }
                .map { v -> "$v at $width px" }
        }
        assertEquals(emptyList<String>(), misread)
    }

    @Test
    fun t302_fingerAtTheImagesLeftEdgeReadsTheMinimum() {
        assertEquals(1, sliderValueAt(0f, imageWidth, 1, 28))
    }

    @Test
    fun t303_fingerLeftOfTheSliderReadsTheMinimum() {
        assertEquals(1, sliderValueAt(-20f, imageWidth, 1, 28))
    }

    @Test
    fun t304_fingerAtTheImagesRightEdgeReadsTheMaximum() {
        assertEquals(28, sliderValueAt(imageWidth, imageWidth, 1, 28))
    }

    @Test
    fun t305_fingerRightOfTheSliderReadsTheMaximum() {
        assertEquals(28, sliderValueAt(1000f, imageWidth, 1, 28))
    }
}
