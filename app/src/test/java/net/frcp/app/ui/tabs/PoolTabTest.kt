package net.frcp.app.ui

import net.frcp.app.logic.EDITIONS
import net.frcp.app.logic.logic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// The Pool tab's civs and DLC toggles. The civs never scroll and never move: an older edition leaves the places of
// the newer civs empty, and only as many DLC toggles show as leave the civs their room.
class PoolTabTest {
    private val newest = EDITIONS.maxBy { logic.editionInfo.getValue(it).year }

    @Test
    fun `the newest edition's grid holds every civ in alphabetical order, three to a row`() {
        val grid = civGrid(logic, newest)
        assertEquals(logic.displayOrder, grid.flatten())
        assertTrue(grid.dropLast(1).all { it.size == 3 })
    }

    @Test
    fun `every civ keeps its place in every edition, and newer civs leave theirs empty`() {
        val rows = civGrid(logic, newest).size
        for (ed in EDITIONS) {
            val grid = civGrid(logic, ed)
            assertEquals(ed, rows, grid.size)
            grid.flatten().forEachIndexed { i, c ->
                val civ = logic.displayOrder[i]
                assertEquals("$ed, place $i", if (logic.inEdition(civ, ed)) civ else null, c)
            }
        }
    }

    @Test
    fun `the DLC toggles left out are toggles data json has, so leaving them out still works`() {
        val toggles = EDITIONS.flatMap { logic.editionInfo.getValue(it).dlcToggles }
        for (dlc in HIDDEN_DLC_TOGGLES) assertTrue(dlc, dlc in toggles)
    }

    @Test
    fun `each edition's DLC toggles fit in three rows of two, which leaves the civs their room`() {
        for (ed in EDITIONS) {
            val shown = shownDlcToggles(logic.editionInfo.getValue(ed))
            assertTrue("$ed shows ${shown.size} DLC toggles", (shown.size + 1) / 2 <= 3)
        }
    }
}
