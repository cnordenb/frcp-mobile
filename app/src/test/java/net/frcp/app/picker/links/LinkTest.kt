package net.frcp.app

import net.frcp.app.logic.CIVS
import net.frcp.app.logic.Named
import net.frcp.app.logic.fixture
import net.frcp.app.logic.idOf
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import net.frcp.app.logic.namedLog
import net.frcp.app.logic.namesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Opening the app, share links, preset files and the links out of the app, as the desktop app's share tests and the
// web version's script.js have them.
class LinkTest {
    private val error = uiSound(UiSound.Error)

    // HD with every civ on, and Celts then Britons drawn, as in ShareTest
    private val hdLink = "https://frcp.net/#p=1B__________.KF"
    private val hdRound = listOf(Named("Celts", 1, 31), Named("Britons", 2, 31))

    /** The link a picker hands to the share sheet. */
    private fun shared(p: TestPicker, withDraws: Boolean): String {
        p.vm.shareLink(withDraws)
        return (p.effect() as Effect.Share).text
    }

    @Test
    fun `a share link opens the app at the shared pool and its draws`() {
        val p = TestPicker()
        p.vm.onLaunch(hdLink)
        assertEquals("HD", p.vm.view.state.edition)
        assertEquals(listOf(hdRound), namedLog(p.vm.view.state))
        assertEquals("Loaded the shared link", p.vm.toast?.text)
        assertEquals(p.vm.view.state, p.saved)
    }

    @Test
    fun `a link over a picker with draws shows what it loads first, and loads once that's confirmed`() {
        val past = listOf(Named("Huns", 1, 56))
        val p = TestPicker(makeState(log = listOf(past)))
        p.vm.onLaunch(null)
        p.vm.onLink(hdLink)
        val preview = p.vm.dialog as Dialog.Preview
        assertEquals("HD", preview.link.edition)
        assertEquals("nothing changes before it's confirmed", listOf(past), namedLog(p.vm.view.state))
        p.vm.loadPreviewedLink(preview.link)
        assertNull(p.vm.dialog)
        assertEquals(listOf(past, hdRound), namedLog(p.vm.view.state))
        assertEquals("Loaded the shared link", p.vm.toast?.text)
    }

    @Test
    fun `a link that isn't one says so and changes nothing`() {
        val p = TestPicker(makeState())
        p.vm.onLaunch("https://frcp.net/#p=nonsense")
        assertEquals(Dialog.Message("Link not recognised", "This link isn't a valid Fresh Random Civ Picker link, so nothing has changed."), p.vm.dialog)
        assertEquals(error, p.lastSound)
        assertEquals(makeState(), p.vm.view.state)
    }

    @Test
    fun `an address without a link opens the app as the launcher does`() {
        for (uri in listOf(null, "https://frcp.net/", "https://frcp.net/#about")) {
            val p = TestPicker(makeState())
            p.vm.onLaunch(uri)
            assertNull(uri, p.vm.dialog)
            assertNull(uri, p.vm.toast)
            assertEquals(uri, makeState(), p.vm.view.state)
        }
    }

    @Test
    fun `opening the app is one visit, which starts a new round with that setting and draws silently with draw on load`() {
        val p = TestPicker(makeState(log = listOf(listOf(Named("Huns", 1, 56)))), Settings(newRoundOnVisit = true, drawOnLoad = true))
        p.vm.onLaunch(null)
        p.vm.onLaunch(null)
        assertEquals(2, p.vm.view.state.log.size)
        assertEquals(1, p.round.size)
        assertEquals(emptyList<String>(), p.sounds.played)
        assertEquals(p.vm.view.state, p.saved)
    }

    @Test
    fun `a picker's share link opens as the same pool and round in another`() {
        val sharing = TestPicker()
        sharing.vm.setEdition("HD")
        for (c in CIVS.filter { logic.inEdition(it, "HD") && it.id % 4 == 0 }) sharing.vm.toggleCiv(c.id)
        repeat(3) { sharing.vm.draw() }
        val link = shared(sharing, true)
        assertTrue(link, link.startsWith(PickerViewModel.SITE + "#p="))

        val loading = TestPicker()
        loading.vm.onLaunch(link)
        assertEquals("HD", loading.vm.view.state.edition)
        assertEquals(sharing.vm.view.pool, loading.vm.view.pool)
        assertEquals(sharing.round, loading.round)
    }

    @Test
    fun `a pool link leaves out the round's draws`() {
        val sharing = TestPicker().also { p -> repeat(3) { p.vm.draw() } }
        val loading = TestPicker()
        loading.vm.onLaunch(shared(sharing, false))
        assertEquals(sharing.vm.view.pool, loading.vm.view.pool)
        assertEquals(emptyList<Int>(), loading.round)
    }

    @Test
    fun `an undone draw isn't shared`() {
        val p = TestPicker().also { p -> repeat(4) { p.vm.draw() } }
        p.vm.undo()
        val draws = logic.parseShare(shared(p, true))!!.draws
        assertEquals(p.round, draws)
        assertEquals(p.current, draws.last())
    }

    @Test
    fun `the Open Link dialog takes a link, and turns down other text`() {
        val p = TestPicker()
        p.vm.openLinkDialog()
        assertEquals(Dialog.OpenLink, p.vm.dialog)
        assertFalse(p.vm.openLinkText("hello"))
        assertEquals(error, p.lastSound)
        assertEquals(Dialog.OpenLink, p.vm.dialog)
        assertTrue(p.vm.openLinkText("1B__________.KF"))
        assertNull(p.vm.dialog)
        assertEquals(listOf(hdRound), namedLog(p.vm.view.state))
    }

    @Test
    fun `a preset file loads its pool and draws in place of the session, and clears undo`() {
        val p = TestPicker().also { it.vm.draw() }
        p.vm.loadPresetText("round-start.txt", fixture("presets/round-start.txt"))
        val first = p.vm.toast
        assertEquals("Preset loaded", first?.text)
        assertEquals(listOf("Aztecs", "Wu"), namesOf(p.vm.view.state.off.sorted()))
        assertEquals(listOf(idOf("Celts"), idOf("Mongols")), p.round)
        assertEquals(p.vm.view.state, p.saved)
        p.vm.undo()
        assertEquals(error, p.lastSound)
        p.vm.loadPresetText("round-start.txt", fixture("presets/round-start.txt"))
        assertNotEquals("the same message again shows again", first, p.vm.toast)
    }

    @Test
    fun `a file that isn't a preset, or couldn't be read, says so by name and changes nothing`() {
        for (text in listOf("hello", null)) {
            val p = TestPicker(makeState())
            p.vm.loadPresetText("notes.txt", text)
            assertEquals(Dialog.Message("Preset not recognised", "notes.txt isn't a Fresh Random Civ Picker preset, so nothing has changed."), p.vm.dialog)
            assertEquals(error, p.lastSound)
            assertEquals(makeState(), p.vm.view.state)
        }
    }

    @Test
    fun `saving a preset asks for a file named with the app's version and the time`() {
        val p = TestPicker()
        p.vm.savePreset()
        val name = (p.effect() as Effect.SavePreset).fileName
        assertTrue(name, Regex("""FRCP Preset v${Regex.escape(BuildConfig.VERSION_NAME)}a @\d{4}\.\d{2}\.\d{2} \d{6}\.txt""").matches(name))
        p.vm.loadPreset()
        assertEquals(Effect.LoadPreset, p.effect())
    }

    @Test
    fun `the edition's banner opens its store page, or says why it has none`() {
        val p = TestPicker()
        p.vm.openEditionPage()
        assertEquals(Effect.OpenUrl(logic.editionInfo.getValue("DE").url!!), p.effect())
        p.vm.setEdition("AOK")
        p.vm.openEditionPage()
        val aok = logic.editionInfo.getValue("AOK")
        assertEquals(Dialog.Message(aok.short, aok.storeNote!!), p.vm.dialog)
    }

    @Test
    fun `the tech tree opens at the current civ, or at its start before a draw`() {
        val p = TestPicker()
        p.vm.openTechTree()
        assertEquals(Effect.OpenUrl("https://aoe2techtree.net/"), p.effect())
        p.vm.draw()
        p.vm.openTechTree()
        assertEquals(Effect.OpenUrl("https://aoe2techtree.net/#" + CIVS[p.current!!].techTree), p.effect())
    }

    @Test
    fun `a link no app can open says so`() {
        val p = TestPicker()
        p.vm.toggleMenu()
        p.vm.openExternal(PickerViewModel.GITHUB)
        assertFalse(p.vm.menuOpen)
        assertEquals(Effect.OpenUrl(PickerViewModel.GITHUB), p.effect())
        p.vm.linkFailed()
        assertEquals("No app here can open that", p.vm.toast?.text)
        assertEquals(error, p.lastSound)
    }
}
