package net.frcp.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ShareTest {
    private val address = "https://frcp.net/#p="
    private val bitfieldLength = (CIVS.size + 5) / 6
    private val allBits = "_".repeat(bitfieldLength)
    private val fullEditions = EDITIONS.indices.filter { logic.editionTotal(EDITIONS[it]) == CIVS.size }

    private fun link(s: PickerState, withDraws: Boolean = true) = address + logic.shareCode(s, withDraws)

    private fun samplePool(ed: String): PickerState {
        val off = CIVS.filter { !logic.inEdition(it, ed) || it.id % 3 == 1 }.mapTo(mutableSetOf()) { it.id }
        val s = PickerState(ed, off, mutableListOf(mutableListOf()))
        val draws = CIVS.filter { it.id !in s.off }.reversed().take(3)
        s.log = mutableListOf(draws.mapIndexedTo(mutableListOf()) { i, c -> Draw(c.id, i + 1, CIVS.size) })
        return s
    }

    private fun expected(s: PickerState) =
        SharedLink(s.edition, CIVS.map { it.id !in s.off }, s.log.last().map { it.id })

    private fun fullPool(edition: Int, draws: List<Int> = emptyList()) = PickerState(
        EDITIONS[edition],
        mutableSetOf(),
        mutableListOf(draws.mapIndexedTo(mutableListOf()) { i, id -> Draw(id, i + 1, CIVS.size) }),
    )

    private fun withOffDraw() = makeState(
        off = listOf("Wu"),
        log = listOf(listOf(Named("Celts", 1, 55), Named("Wu", 2, 56), Named("Franks", 3, 55))),
    )

    private fun afterHdLink(): PickerState {
        val past = listOf(listOf(Named("Huns", 1, 56)), listOf(Named("Celts", 1, 56), Named("Franks", 2, 56)))
        return makeState(log = past).also { logic.applyLink(it, logic.parseShare("1B__________.KF")!!) }
    }

    private fun notLinks(): List<String> {
        val ed = B64[0]
        val noEdition = B64[EDITIONS.size]
        val noCiv = B64[CIVS.size]
        return listOf(
            "", "   ", "hello", address, address + "1", "${address}1$ed", "${address}2${ed}_",
            "${address}1${ed}_.", "${address}1${ed}_..A", "${address}1${ed}_!",
            "${address}1${ed}_.A.B", "${address}1${noEdition}_",
            "${address}1${ed}_.$noCiv",
            "${address}1${ed}_.AA", "${address}1${ed}A.A", "$address%", "$address%3",
            "$address%G1${ed}_", "${address}1${ed}_%C3%A4", "${address}1${ed}_ä",
            "${address}1${ed}_ A",
        )
    }

    @Test
    fun t094_linkStartsWithTheAddressTheFormatVersionAndTheEdition() {
        assertTrue(link(samplePool(EDITIONS[0])).startsWith(address + "1" + B64[0]))
    }

    @Test
    fun t095_fullPoolIsAllSetBits() {
        val links = fullEditions.map { link(fullPool(it)) }
        assertEquals(fullEditions.map { address + "1" + B64[it] + allBits }, links)
    }

    @Test
    fun t096_drawsFollowADot() {
        val links = fullEditions.map { link(fullPool(it, listOf(0, CIVS.lastIndex))) }
        val draws = "." + B64[0] + B64[CIVS.lastIndex]
        assertEquals(fullEditions.map { address + "1" + B64[it] + allBits + draws }, links)
    }

    @Test
    fun t097_linkOpensAsWhatItWasWrittenFromForEveryEdition() {
        val pools = EDITIONS.map(::samplePool)
        assertEquals(pools.map(::expected), pools.map { logic.parseShare(link(it)) })
    }

    @Test
    fun t098_linkOpensWithOrWithoutItsAddress() {
        val s = samplePool(EDITIONS[0])
        val code = logic.shareCode(s, true)
        val texts = listOf(
            code, "p=$code", "#p=$code", "  \t$address$code \n",
            "https://example.org/index.html?x=1#p=$code", "%31" + code.substring(1),
        )
        assertEquals(emptyList<String>(), texts.filter { logic.parseShare(it) != expected(s) })
    }

    @Test
    fun t099_civsPastAShortBitfieldAreOn() {
        val opened = logic.parseShare(address + "1" + B64[0] + "A")!!
        assertEquals(CIVS.map { it.id >= 6 && logic.inEdition(it, EDITIONS[0]) }, opened.enabled)
    }

    @Test
    fun t100_linkWithoutDrawsOpensWithNone() {
        assertEquals(emptyList<Int>(), logic.parseShare(address + "1" + B64[0] + "A")!!.draws)
    }

    @Test
    fun t101_civsOfNewerEditionsAreLeftOut() {
        val opened = EDITIONS.indices.map { logic.parseShare(address + "1" + B64[it] + allBits)!!.enabled }
        assertEquals(EDITIONS.map { ed -> CIVS.map { logic.inEdition(it, ed) } }, opened)
    }

    @Test
    fun t102_textThatIsNotALinkOpensAsNothing() {
        assertEquals(emptyList<String>(), notLinks().filter { logic.parseShare(it) != null })
    }

    @Test
    fun t103_linkLeavesOutTheDrawsOfCivsThatAreOff() {
        assertEquals(listOf(idOf("Celts"), idOf("Franks")), logic.parseShare(link(withOffDraw()))!!.draws)
    }

    @Test
    fun t104_linkLeavesOutTheDrawsWhenAsked() {
        assertEquals(emptyList<Int>(), logic.parseShare(link(withOffDraw(), false))!!.draws)
    }

    @Test
    fun t105_openingALinkReplacesTheEdition() {
        assertEquals("HD", afterHdLink().edition)
    }

    @Test
    fun t106_openingALinkStartsARoundOfItsDrawsKeepingPastRounds() {
        val expected = listOf(
            listOf(Named("Huns", 1, 56)),
            listOf(Named("Celts", 1, 56), Named("Franks", 2, 56)),
            listOf(Named("Celts", 1, 31), Named("Britons", 2, 31)),
        )
        assertEquals(expected, namedLog(afterHdLink()))
    }

    @Test
    fun t107_openingALinkWithoutDrawsStartsAnEmptyRound() {
        val s = makeState(log = listOf(listOf(Named("Celts", 1, 56))))
        logic.applyLink(s, logic.parseShare("1A__________")!!)
        assertEquals(listOf(listOf(Named("Celts", 1, 56)), emptyList()), namedLog(s))
    }
}
