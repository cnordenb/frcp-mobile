package net.frcp.app.picker.pool

import net.frcp.app.logic.EDITIONS
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import net.frcp.app.picker.TestPicker
import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmDlcToggleTest {
    private class Toggle(val edition: String, val dlc: String) {
        val ids = civsOf(listOf(dlc)).filter { it in civsInEdition(edition) }
        val name = "$dlc in $edition"

        fun toggled(times: Int) =
            TestPicker(makeState(edition = edition)).also { p -> repeat(times) { p.vm.toggleDlc(dlc) } }
    }

    private val toggles = EDITIONS.flatMap { ed -> logic.editionInfo.getValue(ed).dlcToggles.map { Toggle(ed, it) } }

    private fun wrong(holds: (Toggle) -> Boolean) = toggles.filterNot(holds).map { it.name }

    private fun oneOffThenToggled(t: Toggle): List<Int> {
        val p = t.toggled(0)
        p.vm.toggleCiv(t.ids[0])
        p.vm.toggleDlc(t.dlc)
        return t.ids.filter { it in p.vm.view.pool }
    }

    @Test
    fun t260_everyDlcToggleHasCivsInItsEdition() {
        assertEquals(emptyList<String>(), wrong { it.ids.isNotEmpty() })
    }

    @Test
    fun t261_dlcToggleTurnsOnlyItsOwnCivsOff() {
        val wrong = wrong { it.toggled(1).vm.view.pool == civsInEdition(it.edition) - it.ids.toSet() }
        assertEquals(emptyList<String>(), wrong)
    }

    @Test
    fun t262_dlcToggleTurnsItsCivsOnOnceTheyAreAllOff() {
        assertEquals(emptyList<String>(), wrong { it.toggled(2).vm.view.pool == civsInEdition(it.edition) })
    }

    @Test
    fun t263_dlcToggleTurnsItsCivsOffWhileAnyIsOn() {
        assertEquals(emptyList<String>(), wrong { it.ids.size < 2 || oneOffThenToggled(it).none() })
    }
}
