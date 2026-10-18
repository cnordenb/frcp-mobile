package net.frcp.app.ui

import net.frcp.app.SwipeKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// The Draw tab's swipes, as the web version's swipe test has them: which way does what, how the first pixels settle
// the swipe's axis, where the civ icon and the incoming civ's ghost sit as the finger moves, and the glide after it
// lifts. The view model tests check what each swipe then does.
class SwipeTest {
    private val travel = 120f

    @Test
    fun `a swipe left draws, right undoes, up re-rolls and down resets`() {
        assertEquals(SwipeKind.Draw, swipeKind(true, -50f))
        assertEquals(SwipeKind.Undo, swipeKind(true, 50f))
        assertEquals(SwipeKind.Reroll, swipeKind(false, -50f))
        assertEquals(SwipeKind.Reset, swipeKind(false, 50f))
    }

    @Test
    fun `the axis settles once the finger is past the slop, the way it went further`() {
        assertNull(swipeAxis(6f, -8f, 10f))
        assertEquals(true, swipeAxis(-12f, 5f, 10f))
        assertEquals(false, swipeAxis(4f, 15f, 10f))
        assertEquals("a diagonal goes up or down", false, swipeAxis(11f, -11f, 10f))
    }

    @Test
    fun `a swipe left takes the current civ towards the edge with the finger, fading it out, with no ghost`() {
        for (t in listOf(0f, 30f, 90f)) {
            val (icon, ghost) = swipePlaces(SwipeKind.Draw, t, travel, false)
            assertEquals(IconPlace(t, 1 - t / travel), icon)
            assertNull(ghost)
        }
    }

    @Test
    fun `the other swipes bring the next civ in from the edge with the finger, as the current one fades in place`() {
        for (kind in listOf(SwipeKind.Undo, SwipeKind.Reroll, SwipeKind.Reset)) {
            val (icon, ghost) = swipePlaces(kind, 30f, travel, false)
            assertEquals(kind.name, IconPlace(0f, 0.75f), icon)
            assertEquals(kind.name, IconPlace(90f, 0.25f), ghost)
        }
    }

    @Test
    fun `once a swipe acts, the civ icon shows the new civ and the ghost the old one, each where it was`() {
        val (drawIcon, drawGhost) = swipePlaces(SwipeKind.Draw, 60f, travel, true)
        assertEquals(IconPlace(0f, 1f), drawIcon)
        assertEquals(IconPlace(60f, 0.5f), drawGhost)
        val (undoIcon, undoGhost) = swipePlaces(SwipeKind.Undo, 60f, travel, true)
        assertEquals(IconPlace(60f, 0.5f), undoIcon)
        assertEquals(IconPlace(0f, 0.5f), undoGhost)
    }

    @Test
    fun `at the end of its travel the new civ is in place and the old one gone`() {
        for (kind in SwipeKind.entries) {
            val (icon, ghost) = swipePlaces(kind, travel, travel, true)
            assertEquals(kind.name, IconPlace(0f, 1f), icon)
            assertEquals(kind.name, 0f, ghost!!.alpha, 0f)
        }
    }

    @Test
    fun `a lifted swipe glides on from the finger's speed, taking 80 to 350 ms`() {
        assertEquals(200f, glideMillis(100f, 1f), 0.001f)
        assertEquals("a slow finger", 350f, glideMillis(100f, 0.05f), 0f)
        assertEquals("a finger that stopped", 350f, glideMillis(100f, 0f), 0f)
        assertEquals("a fast finger", 80f, glideMillis(100f, 10f), 0f)
    }
}
