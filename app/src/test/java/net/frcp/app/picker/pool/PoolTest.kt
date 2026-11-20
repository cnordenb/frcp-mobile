package net.frcp.app

import net.frcp.app.logic.CIVS
import net.frcp.app.logic.EDITIONS
import net.frcp.app.logic.Named
import net.frcp.app.logic.idOf
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import net.frcp.app.logic.namedLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// The Pool tab's actions, as the desktop app's model tests have them: editions, DLC toggles, single civs, Enable All
// and Disable All, and what a change of the pool does to the round, to undo and to the saved state.
class PoolTest {
    private fun civsOf(dlcs: Collection<String>) = CIVS.filter { it.dlc in dlcs }.map { it.id }
    private fun inEdition(ed: String) = CIVS.filter { logic.inEdition(it, ed) }.map { it.id }

    @Test
    fun `switching edition leaves its civs on and the newer ones off`() {
        for (ed in EDITIONS) {
            val p = TestPicker(makeState())
            p.vm.setEdition(ed)
            assertEquals(ed, p.vm.view.state.edition)
            assertEquals(ed, inEdition(ed), p.vm.view.pool)
            assertEquals(ed, logic.editionTotal(ed), p.vm.view.editionTotal)
            assertEquals(ed, p.vm.view.state, p.saved)
        }
    }

    @Test
    fun `switching to the edition already on changes nothing`() {
        val p = TestPicker()
        p.vm.draw()
        p.vm.setEdition(EDITIONS[0])
        p.vm.undo()
        assertEquals("the draw can still be undone", emptyList<Int>(), p.round)
    }

    @Test
    fun `a civ toggles off and on again`() {
        val p = TestPicker()
        val celts = idOf("Celts")
        p.vm.toggleCiv(celts)
        assertFalse(celts in p.vm.view.pool)
        assertEquals(setOf(celts), p.saved!!.off)
        p.vm.toggleCiv(celts)
        assertTrue(celts in p.vm.view.pool)
        assertEquals(uiSound(UiSound.Click), p.lastSound)
    }

    @Test
    fun `a DLC toggle turns its civs off while any is on, and on once they're all off`() {
        for (ed in EDITIONS) {
            for (dlc in logic.editionInfo.getValue(ed).dlcToggles) {
                val ids = civsOf(listOf(dlc)).filter { it in inEdition(ed) }
                assertTrue("$dlc has civs in $ed", ids.isNotEmpty())
                val p = TestPicker(makeState(edition = ed))
                p.vm.toggleDlc(dlc)
                assertEquals("$dlc in $ed: only its own civs go", inEdition(ed) - ids.toSet(), p.vm.view.pool)
                p.vm.toggleDlc(dlc)
                assertEquals("$dlc in $ed", inEdition(ed), p.vm.view.pool)
                if (ids.size > 1) {
                    p.vm.toggleCiv(ids[0])
                    p.vm.toggleDlc(dlc)
                    assertTrue("$dlc in $ed, with one civ off", ids.none { it in p.vm.view.pool })
                }
            }
        }
    }

    @Test
    fun `Enable All turns on the edition's civs, and Disable All turns them all off`() {
        val p = TestPicker(makeState(edition = "HD"))
        p.vm.disableAll()
        assertEquals(emptyList<Int>(), p.vm.view.pool)
        p.vm.enableAll()
        assertEquals(inEdition("HD"), p.vm.view.pool)
    }

    @Test
    fun `changing the pool clears undo and redo`() {
        val p = TestPicker()
        repeat(2) { p.vm.draw() }
        p.vm.undo()
        p.vm.toggleCiv(idOf("Celts"))
        p.vm.undo()
        p.vm.redo()
        assertEquals(listOf(uiSound(UiSound.Error), uiSound(UiSound.Error)), p.sounds.played.takeLast(2))
        assertEquals(1, p.round.size)
    }

    @Test
    fun `a change that leaves the pool as it was keeps undo`() {
        val p = TestPicker()
        p.vm.draw()
        p.vm.enableAll()
        p.vm.undo()
        assertEquals(emptyList<Int>(), p.round)
    }

    @Test
    fun `with auto-reset, a change of the pool starts a new round`() {
        val state = makeState(log = listOf(listOf(Named("Huns", 1, 56), Named("Celts", 2, 56))))
        val kept = TestPicker(state)
        kept.vm.toggleCiv(idOf("Wu"))
        assertEquals(1, kept.vm.view.state.log.size)

        val reset = TestPicker(state, Settings(autoReset = true))
        reset.vm.toggleCiv(idOf("Wu"))
        assertEquals(listOf(listOf(Named("Huns", 1, 56), Named("Celts", 2, 56)), emptyList()), namedLog(reset.vm.view.state))
    }

    @Test
    fun `turning auto-toggle on turns on the edition's older civs, and turning it off turns them off`() {
        val older = civsOf(logic.editionInfo.getValue("DE").olderDlcs)
        val p = TestPicker(makeState(off = CIVS.map { it.name }))
        p.vm.toggleAutoToggle()
        assertTrue(p.savedSettings.autoToggle)
        assertEquals(older, p.vm.view.pool)
        p.vm.toggleAutoToggle()
        assertFalse(p.savedSettings.autoToggle)
        assertEquals(emptyList<Int>(), p.vm.view.pool)
    }

    @Test
    fun `with auto-toggle, switching edition turns on its older civs`() {
        val p = TestPicker(makeState(off = CIVS.map { it.name }), Settings(autoToggle = true))
        p.vm.setEdition("HD")
        assertEquals(civsOf(logic.editionInfo.getValue("HD").olderDlcs), p.vm.view.pool)
    }

    @Test
    fun `auto-reset is saved`() {
        val p = TestPicker()
        p.vm.toggleAutoReset()
        assertTrue(p.savedSettings.autoReset)
    }
}
