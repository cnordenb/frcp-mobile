package net.frcp.app.ui.tabs

import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UiScrollBarTest {
    private val range = 600f
    private val free = 354f

    @Test
    fun t334_knobStartsAtTheTopOfItsTrack() {
        assertEquals(0f, scrollKnobTop(0f, range, false, free), 0f)
    }

    @Test
    fun t335_knobSitsHalfwayWhenTheLogIsScrolledHalfway() {
        assertEquals(177f, scrollKnobTop(300f, range, false, free), 0.001f)
    }

    @Test
    fun t336_knobEndsAtTheBottomOfItsTrack() {
        assertEquals(free, scrollKnobTop(range, range, true, free), 0f)
    }

    @Test
    fun t337_knobReachesTheBottomOnceTheLogCannotScrollFurther() {
        assertEquals(free, scrollKnobTop(590f, range, true, free), 0f)
    }

    @Test
    fun t338_knobStaysAtTheTopOfALogThatDoesNotScroll() {
        assertEquals(0f, scrollKnobTop(0f, 0f, false, free), 0f)
    }

    @Test
    fun t339_draggingTheKnobDownFromTheTopScrollsInProportion() {
        assertEquals(300f, scrollForKnobDrag(0f, 177f, range, free), 0.001f)
    }

    @Test
    fun t340_draggingTheKnobDownFromTheMiddleScrollsToTheEnd() {
        assertEquals(range, scrollForKnobDrag(300f, 177f, range, free), 0.001f)
    }

    @Test
    fun t341_draggingTheKnobUpFromTheMiddleScrollsToTheStart() {
        assertEquals(0f, scrollForKnobDrag(300f, -177f, range, free), 0.001f)
    }

    @Test
    fun t342_pressOnTheTrackPagesByALineShortOfAPage() {
        assertEquals(380f, scrollPage(400f, 20f), 0f)
    }

    @Test
    fun t343_pressOnTheTrackPagesByAtLeastALine() {
        assertEquals(20f, scrollPage(30f, 20f), 0f)
    }
}
