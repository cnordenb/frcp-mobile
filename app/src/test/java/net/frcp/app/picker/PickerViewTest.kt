package net.frcp.app.picker

import net.frcp.app.device.Assets
import net.frcp.app.logic.CIVS
import net.frcp.app.logic.Named
import net.frcp.app.logic.idOf
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PickerViewTest {
    private fun texts(rounds: List<List<LogRow>>) = rounds.map { r -> r.map { it.text } }

    private fun twoRounds() = makeState(
        log = listOf(listOf(Named("Huns", 1, 56), Named("Celts", 2, 56)), listOf(Named("Franks", 1, 56))),
    )

    private fun celtsOffAfterDraw() = makeState(
        off = listOf("Celts"),
        log = listOf(listOf(Named("Huns", 1, 56), Named("Celts", 2, 56))),
    )

    @Test
    fun t108_drawnLogListsRoundsAndDrawsNewestFirstWithCounts() {
        val view = PickerView(logic, twoRounds(), null)
        assertEquals(listOf(listOf("Franks (1/56)"), listOf("Celts (2/56)", "Huns (1/56)")), texts(view.drawnRounds))
    }

    @Test
    fun t109_drawnLogRowHasItsCivsIcon() {
        val view = PickerView(logic, twoRounds(), null)
        assertEquals(Assets.civIcon(CIVS[idOf("Franks")].file), view.drawnRounds[0][0].icon)
    }

    @Test
    fun t110_emptyRoundLeavesNoGapAndPresetCivsShowWithoutCounts() {
        val s = makeState(log = listOf(listOf(Named("Huns", 0, 0), Named("Celts", 0, 0)), emptyList()))
        assertEquals(listOf(listOf("Celts", "Huns")), texts(PickerView(logic, s, null).drawnRounds))
    }

    @Test
    fun t111_remainingLogListsCivsLeftAlphabeticallyWithoutCivsThatAreOff() {
        val s = makeState(edition = "AOK", off = listOf("Celts"), log = listOf(listOf(Named("Britons", 1, 12))))
        val rows = PickerView(logic, s, null).remainingRows.map { it.text }
        val left = CIVS.filter { logic.inEdition(it, "AOK") && it.name != "Celts" && it.name != "Britons" }
        assertEquals(left.map { it.name }.sorted(), rows)
    }

    @Test
    fun t112_counterCountsThisRoundsDrawsOfCivsStillOn() {
        assertEquals(1, PickerView(logic, celtsOffAfterDraw(), null).drawn)
    }

    @Test
    fun t113_counterIsOutOfThePoolWithoutCivsThatAreOff() {
        assertEquals(CIVS.size - 1, PickerView(logic, celtsOffAfterDraw(), null).pool.size)
    }

    @Test
    fun t114_editionTotalCountsCivsThatAreOffToo() {
        assertEquals(CIVS.size, PickerView(logic, celtsOffAfterDraw(), null).editionTotal)
    }
}
