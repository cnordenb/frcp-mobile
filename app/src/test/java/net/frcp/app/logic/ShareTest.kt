package net.frcp.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Share links. The first tests mirror the desktop app's ShareLinkTest.cpp and the web version's share.test.js one for
// one, so all three apps write and open links by the same rules.
class ShareTest {
    private val address = "https://frcp.net/#p="
    private fun link(s: PickerState, withDraws: Boolean = true) = address + logic.shareCode(s, withDraws)
    private val bitfieldLength = (CIVS.size + 5) / 6

    /** The edition's civs, every third one left out, and up to three draws from the rest in reverse ID order. */
    private fun samplePool(ed: String): PickerState {
        val s = PickerState(ed, CIVS.filter { !logic.inEdition(it, ed) || it.id % 3 == 1 }.mapTo(mutableSetOf()) { it.id }, mutableListOf(mutableListOf()))
        val draws = CIVS.filter { it.id !in s.off }.reversed().take(3)
        s.log = mutableListOf(draws.mapIndexedTo(mutableListOf()) { i, c -> Draw(c.id, i + 1, CIVS.size) })
        return s
    }

    /** What a link should open as, for the pool of state s. */
    private fun expected(s: PickerState) = SharedLink(s.edition, CIVS.map { it.id !in s.off }, s.log.last().map { it.id })

    @Test
    fun `a link starts with the address, the format version and the edition`() {
        assertTrue(link(samplePool(EDITIONS[0])).startsWith(address + "1" + B64[0]))
    }

    @Test
    fun `a full pool is all set bits, and draws follow a dot`() {
        EDITIONS.forEachIndexed { e, ed ->
            if (logic.editionTotal(ed) != CIVS.size) return@forEachIndexed
            val s = PickerState(ed, mutableSetOf(), mutableListOf(mutableListOf()))
            val bits = "_".repeat(bitfieldLength)
            assertEquals(address + "1" + B64[e] + bits, link(s))
            s.log = mutableListOf(mutableListOf(Draw(0, 1, CIVS.size), Draw(CIVS.lastIndex, 2, CIVS.size)))
            assertEquals(address + "1" + B64[e] + bits + "." + B64[0] + B64[CIVS.lastIndex], link(s))
        }
    }

    @Test
    fun `a link opens as what it was written from, for every edition`() {
        for (ed in EDITIONS) {
            val s = samplePool(ed)
            assertEquals(ed, expected(s), logic.parseShare(link(s)))
        }
    }

    @Test
    fun `a link opens with or without its address`() {
        val s = samplePool(EDITIONS[0])
        val code = logic.shareCode(s, true)
        val texts = listOf(
            code, "p=$code", "#p=$code", "  \t$address$code \n", "https://example.org/index.html?x=1#p=$code", "%31" + code.substring(1),
        )
        for (text in texts) assertEquals(text, expected(s), logic.parseShare(text))
    }

    @Test
    fun `the civs past a short bitfield are on`() {
        val opened = logic.parseShare(address + "1" + B64[0] + "A")!!
        assertEquals(CIVS.map { it.id >= 6 && logic.inEdition(it, EDITIONS[0]) }, opened.enabled)
        assertEquals(emptyList<Int>(), opened.draws)
    }

    @Test
    fun `the civs of newer editions are left out`() {
        EDITIONS.forEachIndexed { e, ed ->
            val opened = logic.parseShare(address + "1" + B64[e] + "_".repeat(bitfieldLength))!!
            assertEquals(ed, CIVS.map { logic.inEdition(it, ed) }, opened.enabled)
        }
    }

    @Test
    fun `text that isn't a link opens as nothing`() {
        val ed = B64[0]
        val noEdition = B64[EDITIONS.size]
        val noCiv = B64[CIVS.size]
        val texts = listOf(
            "", "   ", "hello", address, address + "1", "${address}1$ed", "${address}2${ed}_",
            "${address}1${ed}_.", "${address}1${ed}_..A", "${address}1${ed}_!",
            "${address}1${ed}_.A.B", "${address}1${noEdition}_",
            "${address}1${ed}_.$noCiv",
            "${address}1${ed}_.AA", "${address}1${ed}A.A", "$address%", "$address%3",
            "$address%G1${ed}_", "${address}1${ed}_%C3%A4", "${address}1${ed}_ä",
            "${address}1${ed}_ A",
        )
        for (text in texts) assertNull(text, logic.parseShare(text))
    }

    // The web version's own behaviour, which the app shares

    @Test
    fun `a link leaves out the draws of civs that are off, and the draws altogether when asked`() {
        val s = makeState(off = listOf("Wu"), log = listOf(listOf(Named("Celts", 1, 55), Named("Wu", 2, 56), Named("Franks", 3, 55))))
        assertEquals(listOf(idOf("Celts"), idOf("Franks")), logic.parseShare(link(s))!!.draws)
        assertEquals(emptyList<Int>(), logic.parseShare(link(s, false))!!.draws)
    }

    @Test
    fun `opening a link replaces the pool and starts a round of its draws, keeping past rounds`() {
        val s = makeState(log = listOf(listOf(Named("Huns", 1, 56)), listOf(Named("Celts", 1, 56), Named("Franks", 2, 56))))
        logic.applyLink(s, logic.parseShare("1B__________.KF")!!)
        assertEquals("HD", s.edition)
        assertEquals(
            listOf(
                listOf(Named("Huns", 1, 56)),
                listOf(Named("Celts", 1, 56), Named("Franks", 2, 56)),
                listOf(Named("Celts", 1, 31), Named("Britons", 2, 31)),
            ),
            namedLog(s),
        )
    }

    @Test
    fun `opening a link without draws starts an empty round`() {
        val s = makeState(log = listOf(listOf(Named("Celts", 1, 56))))
        logic.applyLink(s, logic.parseShare("1A__________")!!)
        assertEquals(listOf(listOf(Named("Celts", 1, 56)), emptyList()), namedLog(s))
    }
}
