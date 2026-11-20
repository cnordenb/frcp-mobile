package net.frcp.app.picker.links

import net.frcp.app.BuildConfig
import net.frcp.app.logic.fixture
import net.frcp.app.logic.idOf
import net.frcp.app.logic.makeState
import net.frcp.app.logic.namesOf
import net.frcp.app.picker.Dialog
import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.Effect
import net.frcp.app.picker.TestPicker
import net.frcp.app.picker.drawnPicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmPresetTest {
    private val notPresets = listOf("hello", null)

    private fun load(p: TestPicker) = p.vm.loadPresetText("round-start.txt", fixture("presets/round-start.txt"))

    private fun presetLoaded() = drawnPicker(1).also(::load)

    private fun notPresetLoaded(text: String?) =
        TestPicker(makeState()).also { it.vm.loadPresetText("notes.txt", text) }

    @Test
    fun t230_loadingAPresetSaysSo() {
        assertEquals("Preset loaded", presetLoaded().vm.toast?.text)
    }

    @Test
    fun t231_presetLoadsItsPoolInPlaceOfTheSessions() {
        assertEquals(listOf("Aztecs", "Wu"), namesOf(presetLoaded().vm.view.state.off.sorted()))
    }

    @Test
    fun t232_presetLoadsItsDrawsInPlaceOfTheSessions() {
        assertEquals(listOf(idOf("Celts"), idOf("Mongols")), presetLoaded().round)
    }

    @Test
    fun t233_loadedPresetIsSaved() {
        val p = presetLoaded()
        assertEquals(p.vm.view.state, p.saved)
    }

    @Test
    fun t234_loadingAPresetClearsUndo() {
        val p = presetLoaded()
        p.vm.undo()
        assertEquals(ERROR_SOUND, p.lastSound)
    }

    @Test
    fun t235_sameMessageAgainIsANewMessage() {
        val p = presetLoaded()
        val first = p.vm.toast
        load(p)
        assertNotEquals(first, p.vm.toast)
    }

    @Test
    fun t236_fileThatIsNotAPresetSaysSoByName() {
        val text = "notes.txt isn't a Fresh Random Civ Picker preset, so nothing has changed."
        val message = Dialog.Message("Preset not recognised", text)
        assertEquals(notPresets.map { message }, notPresets.map { notPresetLoaded(it).vm.dialog })
    }

    @Test
    fun t237_fileThatIsNotAPresetSoundsTheError() {
        assertEquals(notPresets.map { ERROR_SOUND }, notPresets.map { notPresetLoaded(it).lastSound })
    }

    @Test
    fun t238_fileThatIsNotAPresetChangesNothing() {
        assertEquals(notPresets.map { makeState() }, notPresets.map { notPresetLoaded(it).vm.view.state })
    }

    @Test
    fun t239_savingAPresetAsksForAFileNamedWithTheVersionAndTheTime() {
        val p = TestPicker()
        p.vm.savePreset()
        val name = (p.effect() as Effect.SavePreset).fileName
        val version = Regex.escape(BuildConfig.VERSION_NAME)
        val pattern = Regex("""FRCP Preset v${version}a @\d{4}\.\d{2}\.\d{2} \d{6}\.txt""")
        assertTrue(name, pattern.matches(name))
    }

    @Test
    fun t240_loadingAPresetAsksTheScreenForAFile() {
        val p = TestPicker()
        p.vm.loadPreset()
        assertEquals(Effect.LoadPreset, p.effect())
    }
}
