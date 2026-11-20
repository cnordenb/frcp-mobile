package net.frcp.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class StateTest {
    private val emptyLog = listOf(emptyList<Draw>())
    private val webState = """{"v":1,"edition":"DE","disabled":[1,40],"log":[[[16,1,54]],[[10,1,54],[5,2,54]]]}"""
    private val fullRound = CIVS.map { Draw(it.id, it.id + 1, CIVS.size) }
    private val dropped = 6 * CIVS.size - 256

    private fun savedState() = makeState(
        off = listOf("Aztecs", "Wu"),
        log = listOf(listOf(Named("Huns", 1, 54), Named("Celts", 2, 54)), listOf(Named("Franks", 1, 54))),
    )

    private fun patchyState() = logic.parseState(
        """{"edition":"DE","disabled":[1,-1,999,2.5,"3",null],"log":[
          [[10,1,56],[999,2,56],["x",3,56],[11,"4",56],[12,5]],
          "not a round",
          [],
          [[13,1,56],[13,2,56],[14,3,56]]]}""",
    )!!

    private fun trimmedSixRounds() = logic.emptyState().also { s ->
        s.log = MutableList(6) { fullRound.toMutableList() }
        logic.trimLog(s)
    }

    @Test
    fun t026_newStateHasTheDefaultEdition() {
        assertEquals(EDITIONS[0], logic.emptyState().edition)
    }

    @Test
    fun t027_newStateHasEveryCivOn() {
        assertEquals(0, logic.emptyState().off.size)
    }

    @Test
    fun t028_newStateHasOneEmptyRound() {
        assertEquals(emptyLog, logic.emptyState().log)
    }

    @Test
    fun t029_newStateHasNoContent() {
        assertFalse(logic.hasContent(logic.emptyState()))
    }

    @Test
    fun t030_savedStateReadsBackAsItWas() {
        val s = savedState()
        assertEquals(s, logic.parseState(logic.serialiseState(s)))
    }

    @Test
    fun t031_stateWithDrawsHasContent() {
        assertTrue(logic.hasContent(savedState()))
    }

    @Test
    fun t032_whatIsNotASavedStateReadsAsNothing() {
        val texts = listOf(
            null, "", "not json", "[]", "{}", "42", "null", """{"edition":"XX"}""", """{"edition":null}""",
        )
        assertEquals(emptyList<String?>(), texts.filter { logic.parseState(it) != null })
    }

    @Test
    fun t033_savedStateKeepsOnlyKnownCivsOff() {
        assertEquals(setOf(1), patchyState().off)
    }

    @Test
    fun t034_savedStateDropsBrokenDrawsEmptyPastRoundsAndRepeats() {
        assertEquals(listOf(listOf(Draw(10, 1, 56)), listOf(Draw(13, 1, 56), Draw(14, 3, 56))), patchyState().log)
    }

    @Test
    fun t035_savedStateWithoutALogGetsAnEmptyRound() {
        assertEquals(emptyLog, logic.parseState("""{"edition":"DE"}""")!!.log)
    }

    @Test
    fun t036_savedStateOfEmptyRoundsGetsOneEmptyRound() {
        assertEquals(emptyLog, logic.parseState("""{"edition":"DE","log":[[],[]]}""")!!.log)
    }

    @Test
    fun t037_savedStateOfAnOlderEditionHasTheNewerCivsOff() {
        val s = logic.parseState("""{"edition":"AOK","disabled":[]}""")!!
        assertEquals(CIVS.filter { !logic.inEdition(it, "AOK") }.map { it.id }.toSet(), s.off)
    }

    @Test
    fun t038_webVersionsStateReadsBackItsCurrentRound() {
        assertEquals(listOf(Draw(10, 1, 54), Draw(5, 2, 54)), logic.round(logic.parseState(webState)!!))
    }

    @Test
    fun t039_webVersionsStateSavesTheSame() {
        assertEquals(webState, logic.serialiseState(logic.parseState(webState)!!))
    }

    @Test
    fun t040_drawsAreCountedOutOfThePoolAndAFullRoundStartsANewOne() {
        val s = makeState(edition = "AOK", off = allBut("Britons", "Celts"))
        for (name in listOf("Celts", "Britons", "Britons")) logic.recordDraw(s, idOf(name))
        assertEquals(
            listOf(listOf(Named("Celts", 1, 2), Named("Britons", 2, 2)), listOf(Named("Britons", 1, 2))),
            namedLog(s),
        )
    }

    @Test
    fun t041_closingARoundTwiceAddsOneEmptyRound() {
        val s = makeState(log = listOf(listOf(Named("Celts", 1, 56))))
        logic.closeRound(s)
        logic.closeRound(s)
        assertEquals(listOf(listOf(Named("Celts", 1, 56)), emptyList()), namedLog(s))
    }

    @Test
    fun t042_trimmedLogKeeps256Draws() {
        assertEquals(256, trimmedSixRounds().log.sumOf { it.size })
    }

    @Test
    fun t043_trimmedLogKeepsTheCurrentRound() {
        assertEquals(fullRound, trimmedSixRounds().log.last())
    }

    @Test
    fun t044_trimmedLogDropsWholeOldRounds() {
        assertEquals(6 - dropped / CIVS.size, trimmedSixRounds().log.size)
    }

    @Test
    fun t045_trimmedLogDropsTheOldestDrawsFirst() {
        assertEquals(dropped % CIVS.size, trimmedSixRounds().log[0][0].id)
    }

    @Test
    fun t046_currentRoundLongerThanTheCapStaysWhole() {
        val s = logic.emptyState()
        s.log = mutableListOf(MutableList(300) { Draw(it % CIVS.size, it + 1, 300) })
        logic.trimLog(s)
        assertEquals(300, s.log[0].size)
    }
}
