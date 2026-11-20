package net.frcp.app.picker.draws

import net.frcp.app.device.UiSound
import net.frcp.app.logic.idOf
import net.frcp.app.picker.Settings
import net.frcp.app.picker.SwipeKind
import net.frcp.app.picker.TestPicker
import net.frcp.app.picker.drawnPicker
import net.frcp.app.picker.jingleOf
import net.frcp.app.picker.onlyCiv
import net.frcp.app.picker.uiSound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmDrawSoundTest {
    private fun britonsDrawn(legacy: Boolean) =
        TestPicker(onlyCiv("Britons"), Settings(legacyJingles = legacy)).also { it.vm.draw() }

    @Test
    fun t150_randomsJingleLoadsAhead() {
        assertTrue(jingleOf(null) in TestPicker().sounds.loaded)
    }

    @Test
    fun t151_nextDrawsJingleLoadsAhead() {
        val p = TestPicker()
        val notLoaded = mutableListOf<String>()
        repeat(10) {
            val jingle = jingleOf(p.vm.swipeCell(SwipeKind.Draw))
            if (jingle !in p.sounds.loaded) notLoaded += jingle
            p.vm.draw()
        }
        assertEquals(emptyList<String>(), notLoaded)
    }

    @Test
    fun t152_drawPlaysTheJingleOfTheCivShownAhead() {
        val p = TestPicker()
        val ahead = mutableListOf<String>()
        val played = mutableListOf<String?>()
        repeat(10) {
            ahead += jingleOf(p.vm.swipeCell(SwipeKind.Draw))
            p.vm.draw()
            played += p.lastSound
        }
        assertEquals(ahead, played)
    }

    @Test
    fun t153_withJinglesOffADrawSoundsTheButton() {
        assertEquals(uiSound(UiSound.Button), drawnPicker(1, Settings(jingles = false)).lastSound)
    }

    @Test
    fun t154_withJinglesOffNoJingleLoads() {
        val loaded = drawnPicker(1, Settings(jingles = false)).sounds.loaded
        assertEquals(emptyList<String>(), loaded.filter { it.startsWith("sounds/civ/") })
    }

    @Test
    fun t155_civPlaysItsLegacyJingleWhileLegacyJinglesAreOn() {
        assertEquals("sounds/civ/legacy/britons.webm", britonsDrawn(legacy = true).lastSound)
    }

    @Test
    fun t156_civPlaysItsNewJingleWhileLegacyJinglesAreOff() {
        assertEquals(jingleOf(idOf("Britons"), false), britonsDrawn(legacy = false).lastSound)
    }
}
