package net.frcp.app.picker.links

import net.frcp.app.logic.CIVS
import net.frcp.app.logic.logic
import net.frcp.app.picker.Dialog
import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.Effect
import net.frcp.app.picker.PickerViewModel
import net.frcp.app.picker.TestPicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmOutLinkTest {
    private fun outsideLinkOpened() = TestPicker().also { p ->
        p.vm.toggleMenu()
        p.vm.openExternal(PickerViewModel.GITHUB)
    }

    private fun linkFailed() = TestPicker().also { it.vm.linkFailed() }

    @Test
    fun t241_editionsBannerOpensItsStorePage() {
        val p = TestPicker()
        p.vm.openEditionPage()
        assertEquals(Effect.OpenUrl(logic.editionInfo.getValue("DE").url!!), p.effect())
    }

    @Test
    fun t242_editionWithoutAStorePageSaysWhy() {
        val p = TestPicker()
        p.vm.setEdition("AOK")
        p.vm.openEditionPage()
        val aok = logic.editionInfo.getValue("AOK")
        assertEquals(Dialog.Message(aok.short, aok.storeNote!!), p.vm.dialog)
    }

    @Test
    fun t243_techTreeOpensAtItsStartBeforeADraw() {
        val p = TestPicker()
        p.vm.openTechTree()
        assertEquals(Effect.OpenUrl("https://aoe2techtree.net/"), p.effect())
    }

    @Test
    fun t244_techTreeOpensAtTheCurrentCiv() {
        val p = TestPicker()
        p.vm.draw()
        p.vm.openTechTree()
        assertEquals(Effect.OpenUrl("https://aoe2techtree.net/#" + CIVS[p.current!!].techTree), p.effect())
    }

    @Test
    fun t245_openingAnOutsideLinkClosesTheMenu() {
        assertFalse(outsideLinkOpened().vm.menuOpen)
    }

    @Test
    fun t246_outsideLinkIsHandedToTheScreen() {
        assertEquals(Effect.OpenUrl(PickerViewModel.GITHUB), outsideLinkOpened().effect())
    }

    @Test
    fun t247_linkNoAppCanOpenSaysSo() {
        assertEquals("No app here can open that", linkFailed().vm.toast?.text)
    }

    @Test
    fun t248_linkNoAppCanOpenSoundsTheError() {
        assertEquals(ERROR_SOUND, linkFailed().lastSound)
    }
}
