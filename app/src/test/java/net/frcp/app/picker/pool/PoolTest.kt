package net.frcp.app.picker.pool

import net.frcp.app.device.UiSound
import net.frcp.app.logic.CIVS
import net.frcp.app.logic.EDITIONS
import net.frcp.app.logic.idOf
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import net.frcp.app.picker.TestPicker
import net.frcp.app.picker.uiSound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

fun civsOf(dlcs: Collection<String>) = CIVS.filter { it.dlc in dlcs }.map { it.id }

fun civsInEdition(ed: String) = CIVS.filter { logic.inEdition(it, ed) }.map { it.id }

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PoolTest {
    private val celts = idOf("Celts")

    private fun switchedTo(ed: String) = TestPicker(makeState()).also { it.vm.setEdition(ed) }

    private fun celtsToggled(times: Int) = TestPicker().also { p -> repeat(times) { p.vm.toggleCiv(celts) } }

    @Test
    fun t249_switchingEditionSetsTheEdition() {
        assertEquals(EDITIONS, EDITIONS.map { switchedTo(it).vm.view.state.edition })
    }

    @Test
    fun t250_switchingEditionLeavesItsCivsOnAndTheNewerOnesOff() {
        assertEquals(EDITIONS.map(::civsInEdition), EDITIONS.map { switchedTo(it).vm.view.pool })
    }

    @Test
    fun t251_switchingEditionShowsTheEditionsTotal() {
        assertEquals(EDITIONS.map(logic::editionTotal), EDITIONS.map { switchedTo(it).vm.view.editionTotal })
    }

    @Test
    fun t252_switchedEditionIsSaved() {
        val unsaved = EDITIONS.filter { ed -> switchedTo(ed).let { it.vm.view.state != it.saved } }
        assertEquals(emptyList<String>(), unsaved)
    }

    @Test
    fun t253_switchingToTheEditionAlreadyOnKeepsUndo() {
        val p = TestPicker()
        p.vm.draw()
        p.vm.setEdition(EDITIONS[0])
        p.vm.undo()
        assertEquals(emptyList<Int>(), p.round)
    }

    @Test
    fun t254_civTogglesOff() {
        assertFalse(celts in celtsToggled(1).vm.view.pool)
    }

    @Test
    fun t255_civToggledOffIsSaved() {
        assertEquals(setOf(celts), celtsToggled(1).saved!!.off)
    }

    @Test
    fun t256_civTogglesOnAgain() {
        assertTrue(celts in celtsToggled(2).vm.view.pool)
    }

    @Test
    fun t257_togglingACivSoundsTheClick() {
        assertEquals(uiSound(UiSound.Click), celtsToggled(1).lastSound)
    }

    @Test
    fun t258_disableAllTurnsEveryCivOff() {
        val p = TestPicker(makeState(edition = "HD"))
        p.vm.disableAll()
        assertEquals(emptyList<Int>(), p.vm.view.pool)
    }

    @Test
    fun t259_enableAllTurnsOnTheEditionsCivs() {
        val p = TestPicker(makeState(edition = "HD"))
        p.vm.disableAll()
        p.vm.enableAll()
        assertEquals(civsInEdition("HD"), p.vm.view.pool)
    }
}
