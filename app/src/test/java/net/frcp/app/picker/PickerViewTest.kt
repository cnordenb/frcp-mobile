package net.frcp.app

import net.frcp.app.logic.CIVS
import net.frcp.app.logic.Named
import net.frcp.app.logic.idOf
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import org.junit.Assert.assertEquals
import org.junit.Test

// What the screen shows of a state: the Log tab's two logs and the Draw tab's counter.
class PickerViewTest {
    private fun texts(rounds: List<List<LogRow>>) = rounds.map { r -> r.map { it.text } }

    @Test
    fun `the drawn log lists the rounds newest first, and each round's draws newest first with their counts`() {
        val s = makeState(log = listOf(listOf(Named("Huns", 1, 56), Named("Celts", 2, 56)), listOf(Named("Franks", 1, 56))))
        val view = PickerView(logic, s, null)
        assertEquals(listOf(listOf("Franks (1/56)"), listOf("Celts (2/56)", "Huns (1/56)")), texts(view.drawnRounds))
        assertEquals(Assets.civIcon(CIVS[idOf("Franks")].file), view.drawnRounds[0][0].icon)
    }

    @Test
    fun `a round with no draws yet leaves no gap, and a preset's earlier civs show without counts`() {
        val s = makeState(log = listOf(listOf(Named("Huns", 0, 0), Named("Celts", 0, 0)), emptyList()))
        assertEquals(listOf(listOf("Celts", "Huns")), texts(PickerView(logic, s, null).drawnRounds))
    }

    @Test
    fun `the remaining log lists the civs left this round in alphabetical order, leaving out civs that are off`() {
        val s = makeState(edition = "AOK", off = listOf("Celts"), log = listOf(listOf(Named("Britons", 1, 12))))
        val rows = PickerView(logic, s, null).remainingRows.map { it.text }
        val left = CIVS.filter { logic.inEdition(it, "AOK") && it.name != "Celts" && it.name != "Britons" }.map { it.name }
        assertEquals(left.sorted(), rows)
    }

    @Test
    fun `the counter counts this round's draws of civs still on, out of the pool`() {
        val s = makeState(off = listOf("Celts"), log = listOf(listOf(Named("Huns", 1, 56), Named("Celts", 2, 56))))
        val view = PickerView(logic, s, null)
        assertEquals(1, view.drawn)
        assertEquals(CIVS.size - 1, view.pool.size)
        assertEquals(CIVS.size, view.editionTotal)
    }
}
