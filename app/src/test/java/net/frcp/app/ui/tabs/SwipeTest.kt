package net.frcp.app.ui.tabs

import net.frcp.app.picker.SwipeKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SwipeTest {
    private val travel = 120f
    private val otherKinds = listOf(SwipeKind.Undo, SwipeKind.Reroll, SwipeKind.Reset)

    @Test
    fun t306_swipeLeftDraws() {
        assertEquals(SwipeKind.Draw, swipeKind(true, -50f))
    }

    @Test
    fun t307_swipeRightUndoes() {
        assertEquals(SwipeKind.Undo, swipeKind(true, 50f))
    }

    @Test
    fun t308_swipeUpRerolls() {
        assertEquals(SwipeKind.Reroll, swipeKind(false, -50f))
    }

    @Test
    fun t309_swipeDownResets() {
        assertEquals(SwipeKind.Reset, swipeKind(false, 50f))
    }

    @Test
    fun t310_axisIsUnsettledWhileTheFingerIsWithinTheSlop() {
        assertNull(swipeAxis(6f, -8f, 10f))
    }

    @Test
    fun t311_axisSettlesSidewaysWhenTheFingerWentFurtherSideways() {
        assertEquals(true, swipeAxis(-12f, 5f, 10f))
    }

    @Test
    fun t312_axisSettlesUpAndDownWhenTheFingerWentFurtherThatWay() {
        assertEquals(false, swipeAxis(4f, 15f, 10f))
    }

    @Test
    fun t313_diagonalSwipeSettlesUpAndDown() {
        assertEquals(false, swipeAxis(11f, -11f, 10f))
    }

    @Test
    fun t314_drawSwipeTakesTheCurrentCivTowardsTheEdgeFadingItOut() {
        val moved = listOf(0f, 30f, 90f)
        val icons = moved.map { swipePlaces(SwipeKind.Draw, it, travel, false).first }
        assertEquals(moved.map { IconPlace(it, 1 - it / travel) }, icons)
    }

    @Test
    fun t315_drawSwipeShowsNoGhostBeforeItActs() {
        val ghosts = listOf(0f, 30f, 90f).map { swipePlaces(SwipeKind.Draw, it, travel, false).second }
        assertEquals(listOf<IconPlace?>(null, null, null), ghosts)
    }

    @Test
    fun t316_otherSwipesFadeTheCurrentCivInPlace() {
        val icons = otherKinds.map { swipePlaces(it, 30f, travel, false).first }
        assertEquals(otherKinds.map { IconPlace(0f, 0.75f) }, icons)
    }

    @Test
    fun t317_otherSwipesBringTheNextCivInFromTheEdgeWithTheFinger() {
        val ghosts = otherKinds.map { swipePlaces(it, 30f, travel, false).second }
        assertEquals(otherKinds.map { IconPlace(90f, 0.25f) }, ghosts)
    }

    @Test
    fun t318_actedDrawSwipeShowsTheNewCivInPlace() {
        assertEquals(IconPlace(0f, 1f), swipePlaces(SwipeKind.Draw, 60f, travel, true).first)
    }

    @Test
    fun t319_actedDrawSwipeLeavesTheOldCivAsAGhostWhereItWas() {
        assertEquals(IconPlace(60f, 0.5f), swipePlaces(SwipeKind.Draw, 60f, travel, true).second)
    }

    @Test
    fun t320_actedUndoSwipeShowsTheNewCivWhereTheGhostWas() {
        assertEquals(IconPlace(60f, 0.5f), swipePlaces(SwipeKind.Undo, 60f, travel, true).first)
    }

    @Test
    fun t321_actedUndoSwipeLeavesTheOldCivAsAGhostInPlace() {
        assertEquals(IconPlace(0f, 0.5f), swipePlaces(SwipeKind.Undo, 60f, travel, true).second)
    }

    @Test
    fun t322_atTheEndOfItsTravelTheNewCivIsInPlace() {
        val icons = SwipeKind.entries.map { swipePlaces(it, travel, travel, true).first }
        assertEquals(SwipeKind.entries.map { IconPlace(0f, 1f) }, icons)
    }

    @Test
    fun t323_atTheEndOfItsTravelTheOldCivIsGone() {
        val alphas = SwipeKind.entries.map { swipePlaces(it, travel, travel, true).second?.alpha }
        assertEquals(SwipeKind.entries.map { 0f }, alphas)
    }

    @Test
    fun t324_liftedSwipeGlidesOnFromTheFingersSpeed() {
        assertEquals(200f, glideMillis(100f, 1f), 0.001f)
    }

    @Test
    fun t325_slowFingerGlidesForAtMost350Milliseconds() {
        assertEquals(350f, glideMillis(100f, 0.05f), 0f)
    }

    @Test
    fun t326_stoppedFingerGlidesFor350Milliseconds() {
        assertEquals(350f, glideMillis(100f, 0f), 0f)
    }

    @Test
    fun t327_fastFingerGlidesForAtLeast80Milliseconds() {
        assertEquals(80f, glideMillis(100f, 10f), 0f)
    }
}
