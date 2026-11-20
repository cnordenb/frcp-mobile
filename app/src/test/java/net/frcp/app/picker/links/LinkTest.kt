package net.frcp.app.picker.links

import net.frcp.app.logic.Named
import net.frcp.app.logic.makeState
import net.frcp.app.logic.namedLog
import net.frcp.app.picker.Dialog
import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.TestPicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

const val HD_LINK = "https://frcp.net/#p=1B__________.KF"
val HD_ROUND = listOf(Named("Celts", 1, 31), Named("Britons", 2, 31))

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LinkTest {
    private val past = listOf(Named("Huns", 1, 56))
    private val addresses = listOf(null, "https://frcp.net/", "https://frcp.net/#about")

    private fun openedAt(uri: String?) = TestPicker(makeState()).also { it.vm.onLaunch(uri) }

    private fun linkOverDraws() = TestPicker(makeState(log = listOf(past))).also { p ->
        p.vm.onLaunch(null)
        p.vm.onLink(HD_LINK)
    }

    private fun previewConfirmed() = linkOverDraws().also { p ->
        p.vm.loadPreviewedLink((p.vm.dialog as Dialog.Preview).link)
    }

    private fun badLinkOpened() = openedAt("https://frcp.net/#p=nonsense")

    @Test
    fun t198_linkOpensTheAppAtItsEdition() {
        assertEquals("HD", openedAt(HD_LINK).vm.view.state.edition)
    }

    @Test
    fun t199_linkOpensTheAppAtItsDraws() {
        assertEquals(listOf(HD_ROUND), namedLog(openedAt(HD_LINK).vm.view.state))
    }

    @Test
    fun t200_openingALinkSaysItLoaded() {
        assertEquals("Loaded the shared link", openedAt(HD_LINK).vm.toast?.text)
    }

    @Test
    fun t201_openedLinkIsSaved() {
        val p = openedAt(HD_LINK)
        assertEquals(p.vm.view.state, p.saved)
    }

    @Test
    fun t202_linkOverAPickerWithDrawsIsPreviewedFirst() {
        assertEquals("HD", (linkOverDraws().vm.dialog as? Dialog.Preview)?.link?.edition)
    }

    @Test
    fun t203_previewedLinkChangesNothingBeforeItIsConfirmed() {
        assertEquals(listOf(past), namedLog(linkOverDraws().vm.view.state))
    }

    @Test
    fun t204_confirmingThePreviewClosesIt() {
        assertNull(previewConfirmed().vm.dialog)
    }

    @Test
    fun t205_confirmingThePreviewLoadsTheLinkAfterThePastRounds() {
        assertEquals(listOf(past, HD_ROUND), namedLog(previewConfirmed().vm.view.state))
    }

    @Test
    fun t206_confirmingThePreviewSaysTheLinkLoaded() {
        assertEquals("Loaded the shared link", previewConfirmed().vm.toast?.text)
    }

    @Test
    fun t207_linkThatIsNotOneSaysSo() {
        val text = "This link isn't a valid Fresh Random Civ Picker link, so nothing has changed."
        assertEquals(Dialog.Message("Link not recognised", text), badLinkOpened().vm.dialog)
    }

    @Test
    fun t208_linkThatIsNotOneSoundsTheError() {
        assertEquals(ERROR_SOUND, badLinkOpened().lastSound)
    }

    @Test
    fun t209_linkThatIsNotOneChangesNothing() {
        assertEquals(makeState(), badLinkOpened().vm.view.state)
    }

    @Test
    fun t210_addressWithoutALinkOpensNoDialog() {
        assertEquals(emptyList<String?>(), addresses.filter { openedAt(it).vm.dialog != null })
    }

    @Test
    fun t211_addressWithoutALinkShowsNoMessage() {
        assertEquals(emptyList<String?>(), addresses.filter { openedAt(it).vm.toast != null })
    }

    @Test
    fun t212_addressWithoutALinkChangesNothing() {
        assertEquals(emptyList<String?>(), addresses.filter { openedAt(it).vm.view.state != makeState() })
    }
}
