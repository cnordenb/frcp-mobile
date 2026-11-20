package net.frcp.app.picker.links

import net.frcp.app.logic.CIVS
import net.frcp.app.logic.Named
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import net.frcp.app.logic.namedLog
import net.frcp.app.picker.Dialog
import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.PickerViewModel
import net.frcp.app.picker.Settings
import net.frcp.app.picker.TestPicker
import net.frcp.app.picker.drawnPicker
import net.frcp.app.picker.sharedLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmLaunchTest {
    private fun visitedTwice(): TestPicker {
        val state = makeState(log = listOf(listOf(Named("Huns", 1, 56))))
        val p = TestPicker(state, Settings(newRoundOnVisit = true, drawOnLoad = true))
        repeat(2) { p.vm.onLaunch(null) }
        return p
    }

    private fun sharing() = TestPicker().also { p ->
        p.vm.setEdition("HD")
        for (c in CIVS.filter { logic.inEdition(it, "HD") && it.id % 4 == 0 }) p.vm.toggleCiv(c.id)
        repeat(3) { p.vm.draw() }
    }

    private fun opened(link: String) = TestPicker().also { it.vm.onLaunch(link) }

    private fun linkDialog() = TestPicker().also { it.vm.openLinkDialog() }

    private fun otherTextTried() = linkDialog().also { it.vm.openLinkText("hello") }

    private fun linkTextTaken() = linkDialog().also { it.vm.openLinkText("1B__________.KF") }

    @Test
    fun t213_openingTheAppTwiceStartsOneNewRound() {
        assertEquals(2, visitedTwice().vm.view.state.log.size)
    }

    @Test
    fun t214_openingTheAppTwiceDrawsOnceOnLoad() {
        assertEquals(1, visitedTwice().round.size)
    }

    @Test
    fun t215_drawOnLoadIsSilent() {
        assertEquals(emptyList<String>(), visitedTwice().sounds.played)
    }

    @Test
    fun t216_visitIsSaved() {
        val p = visitedTwice()
        assertEquals(p.vm.view.state, p.saved)
    }

    @Test
    fun t217_shareLinkStartsWithTheSitesAddress() {
        val link = sharedLink(sharing(), true)
        assertTrue(link, link.startsWith(PickerViewModel.SITE + "#p="))
    }

    @Test
    fun t218_shareLinkOpensAtTheSameEditionInAnotherPicker() {
        assertEquals("HD", opened(sharedLink(sharing(), true)).vm.view.state.edition)
    }

    @Test
    fun t219_shareLinkOpensAtTheSamePoolInAnotherPicker() {
        val from = sharing()
        assertEquals(from.vm.view.pool, opened(sharedLink(from, true)).vm.view.pool)
    }

    @Test
    fun t220_shareLinkOpensAtTheSameRoundInAnotherPicker() {
        val from = sharing()
        assertEquals(from.round, opened(sharedLink(from, true)).round)
    }

    @Test
    fun t221_poolLinkLeavesOutTheRoundsDraws() {
        assertEquals(emptyList<Int>(), opened(sharedLink(drawnPicker(3), false)).round)
    }

    @Test
    fun t222_undoneDrawIsNotShared() {
        val p = drawnPicker(4)
        p.vm.undo()
        assertEquals(p.round, logic.parseShare(sharedLink(p, true))!!.draws)
    }

    @Test
    fun t223_openLinkDialogOpens() {
        assertEquals(Dialog.OpenLink, linkDialog().vm.dialog)
    }

    @Test
    fun t224_openLinkDialogTurnsDownTextThatIsNotALink() {
        assertFalse(linkDialog().vm.openLinkText("hello"))
    }

    @Test
    fun t225_textThatIsNotALinkSoundsTheError() {
        assertEquals(ERROR_SOUND, otherTextTried().lastSound)
    }

    @Test
    fun t226_textThatIsNotALinkLeavesTheDialogOpen() {
        assertEquals(Dialog.OpenLink, otherTextTried().vm.dialog)
    }

    @Test
    fun t227_openLinkDialogTakesALinkWithoutItsAddress() {
        assertTrue(linkDialog().vm.openLinkText("1B__________.KF"))
    }

    @Test
    fun t228_linkTakenByTheDialogClosesIt() {
        assertNull(linkTextTaken().vm.dialog)
    }

    @Test
    fun t229_linkTakenByTheDialogLoads() {
        assertEquals(listOf(HD_ROUND), namedLog(linkTextTaken().vm.view.state))
    }
}
