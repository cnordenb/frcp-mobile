package net.frcp.app.ui.tabs

import net.frcp.app.logic.EDITIONS
import net.frcp.app.logic.logic
import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PoolTabTest {
    private val newest = EDITIONS.maxBy { logic.editionInfo.getValue(it).year }

    @Test
    fun t328_newestEditionsGridHoldsEveryCivInAlphabeticalOrder() {
        assertEquals(logic.displayOrder, civGrid(logic, newest).flatten())
    }

    @Test
    fun t329_gridHasThreeCivsToARow() {
        val sizes = civGrid(logic, newest).dropLast(1).map { it.size }.distinct()
        assertEquals(listOf(3), sizes)
    }

    @Test
    fun t330_gridHasTheSameNumberOfRowsInEveryEdition() {
        val rows = civGrid(logic, newest).size
        assertEquals(EDITIONS.map { rows }, EDITIONS.map { civGrid(logic, it).size })
    }

    @Test
    fun t331_everyCivKeepsItsPlaceInEveryEditionAndNewerCivsLeaveTheirsEmpty() {
        val misplaced = EDITIONS.flatMap { ed ->
            civGrid(logic, ed).flatten().withIndex()
                .filter { (i, c) -> c != logic.displayOrder[i].takeIf { logic.inEdition(it, ed) } }
                .map { (i, _) -> "$ed, place $i" }
        }
        assertEquals(emptyList<String>(), misplaced)
    }

    @Test
    fun t332_hiddenDlcTogglesAreTogglesTheDataHas() {
        val toggles = EDITIONS.flatMap { logic.editionInfo.getValue(it).dlcToggles }
        assertEquals(emptyList<String>(), HIDDEN_DLC_TOGGLES.filter { it !in toggles })
    }

    @Test
    fun t333_eachEditionsShownDlcTogglesFitInThreeRowsOfTwo() {
        val crowded = EDITIONS.filter { (shownDlcToggles(logic.editionInfo.getValue(it)).size + 1) / 2 > 3 }
        assertEquals(emptyList<String>(), crowded)
    }
}
