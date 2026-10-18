package net.frcp.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

// The controls' geometry, as the desktop app's ParchmentGeometryTest has it: the Options dialog's slider, the logs'
// scrollbar and the tiles along a nine-slice frame's edges.
class ControlsTest {
    // The slider image is 340 px wide, with its groove from 32 to 308 px between the arrowheads.

    @Test
    fun `the slider's knob runs the groove, from the minimum at its start to the maximum at its end`() {
        assertEquals(32f / 340f, sliderKnobAt(1, 1, 28), 1e-6f)
        assertEquals(308f / 340f, sliderKnobAt(28, 1, 28), 1e-6f)
        assertEquals((32f + 276f / 3) / 340f, sliderKnobAt(10, 1, 28), 1e-6f)
    }

    @Test
    fun `a slider with a single value keeps its knob at the start`() {
        assertEquals(32f / 340f, sliderKnobAt(5, 5, 5), 1e-6f)
        assertEquals(5, sliderValueAt(200f, 340f, 5, 5))
    }

    @Test
    fun `a finger on the knob reads its value, for every value and width`() {
        for (width in listOf(240f, 340f, 1000f)) {
            for (v in 1..28) assertEquals("$v at $width px", v, sliderValueAt(width * sliderKnobAt(v, 1, 28), width, 1, 28))
        }
    }

    @Test
    fun `a finger past the groove's ends reads as the minimum or the maximum`() {
        assertEquals(1, sliderValueAt(0f, 340f, 1, 28))
        assertEquals(1, sliderValueAt(-20f, 340f, 1, 28))
        assertEquals(28, sliderValueAt(340f, 340f, 1, 28))
        assertEquals(28, sliderValueAt(1000f, 340f, 1, 28))
    }

    // A log with 600 px out of sight, and a scrollbar track that leaves the knob 354 px to move in

    @Test
    fun `the scrollbar's knob starts at the top of its track and ends at the bottom`() {
        assertEquals(0f, scrollKnobTop(0f, 600f, false, 354f), 0f)
        assertEquals(177f, scrollKnobTop(300f, 600f, false, 354f), 0.001f)
        assertEquals(354f, scrollKnobTop(600f, 600f, true, 354f), 0f)
    }

    @Test
    fun `the knob reaches the bottom once the log can't scroll further, and stays at the top of a log that doesn't scroll`() {
        assertEquals(354f, scrollKnobTop(590f, 600f, true, 354f), 0f)
        assertEquals(0f, scrollKnobTop(0f, 0f, false, 354f), 0f)
    }

    @Test
    fun `dragging the knob scrolls the log in proportion`() {
        assertEquals(300f, scrollForKnobDrag(0f, 177f, 600f, 354f), 0.001f)
        assertEquals(600f, scrollForKnobDrag(300f, 177f, 600f, 354f), 0.001f)
        assertEquals(0f, scrollForKnobDrag(300f, -177f, 600f, 354f), 0.001f)
    }

    @Test
    fun `a press on the track pages by a line short of a page, and at least a line`() {
        assertEquals(380f, scrollPage(400f, 20f), 0f)
        assertEquals(20f, scrollPage(30f, 20f), 0f)
    }

    // The edges of a nine-slice frame, as CSS border-image-repeat lays them out

    @Test
    fun `round fits a whole number of tiles along an edge, at least one`() {
        assertEquals(100f / 3f, tileLength(100f, 38f, Tile.Round), 0.001f)
        assertEquals(10f, tileLength(10f, 38f, Tile.Round), 0f)
    }

    @Test
    fun `repeat keeps the tiles at their own size`() {
        assertEquals(38f, tileLength(100f, 38f, Tile.Repeat), 0f)
    }
}
