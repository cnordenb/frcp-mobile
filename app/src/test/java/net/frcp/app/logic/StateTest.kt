package net.frcp.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// The saved state: reading back what the app stored, whatever shape it's in, and the log's bookkeeping.
class StateTest {
    @Test
    fun `a new state is the default edition with every civ on and an empty round`() {
        val s = logic.emptyState()
        assertEquals(EDITIONS[0], s.edition)
        assertEquals(0, s.off.size)
        assertEquals(listOf(emptyList<Draw>()), s.log)
        assertFalse(logic.hasContent(s))
    }

    @Test
    fun `a saved state reads back as it was`() {
        val s = makeState(
            off = listOf("Aztecs", "Wu"),
            log = listOf(listOf(Named("Huns", 1, 54), Named("Celts", 2, 54)), listOf(Named("Franks", 1, 54))),
        )
        val back = logic.parseState(logic.serialiseState(s))
        assertEquals(s, back)
        assertTrue(logic.hasContent(back!!))
    }

    @Test
    fun `anything that isn't a saved state reads as nothing`() {
        for (raw in listOf(null, "", "not json", "[]", "{}", "42", "null", """{"edition":"XX"}""", """{"edition":null}""")) {
            assertNull(raw, logic.parseState(raw))
        }
    }

    @Test
    fun `a saved state keeps only the parts that make sense`() {
        val s = logic.parseState(
            """{"edition":"DE","disabled":[1,-1,999,2.5,"3",null],"log":[
              [[10,1,56],[999,2,56],["x",3,56],[11,"4",56],[12,5]],
              "not a round",
              [],
              [[13,1,56],[13,2,56],[14,3,56]]]}""",
        )!!
        assertEquals(setOf(1), s.off)
        // Unknown civs and broken entries go, as do empty past rounds; the current round loses its repeats.
        assertEquals(listOf(listOf(Draw(10, 1, 56)), listOf(Draw(13, 1, 56), Draw(14, 3, 56))), s.log)
    }

    @Test
    fun `a saved state with no log gets an empty round`() {
        assertEquals(listOf(emptyList<Draw>()), logic.parseState("""{"edition":"DE"}""")!!.log)
        assertEquals(listOf(emptyList<Draw>()), logic.parseState("""{"edition":"DE","log":[[],[]]}""")!!.log)
    }

    @Test
    fun `a saved state of an older edition has the newer civs off`() {
        val s = logic.parseState("""{"edition":"AOK","disabled":[]}""")!!
        for (c in CIVS) assertEquals(c.name, !logic.inEdition(c, "AOK"), c.id in s.off)
    }

    @Test
    fun `a state saved in the web version's format reads back and saves the same`() {
        val web = """{"v":1,"edition":"DE","disabled":[1,40],"log":[[[16,1,54]],[[10,1,54],[5,2,54]]]}"""
        val s = logic.parseState(web)!!
        assertEquals(listOf(Draw(10, 1, 54), Draw(5, 2, 54)), logic.round(s))
        assertEquals(web, logic.serialiseState(s))
    }

    @Test
    fun `a draw is counted out of the pool, and the draw after a full round starts a new one`() {
        val s = makeState(edition = "AOK", off = CIVS.filter { it.name !in listOf("Britons", "Celts") }.map { it.name })
        logic.recordDraw(s, idOf("Celts"))
        logic.recordDraw(s, idOf("Britons"))
        logic.recordDraw(s, idOf("Britons"))
        assertEquals(
            listOf(listOf(Named("Celts", 1, 2), Named("Britons", 2, 2)), listOf(Named("Britons", 1, 2))),
            namedLog(s),
        )
    }

    @Test
    fun `closing a round adds one empty round, and never two`() {
        val s = makeState(log = listOf(listOf(Named("Celts", 1, 56))))
        logic.closeRound(s)
        logic.closeRound(s)
        assertEquals(listOf(listOf(Named("Celts", 1, 56)), emptyList()), namedLog(s))
    }

    @Test
    fun `the log keeps its newest 256 draws, dropping the oldest first but never the current round`() {
        val full = CIVS.map { Draw(it.id, it.id + 1, CIVS.size) }
        val s = logic.emptyState()
        s.log = MutableList(6) { full.toMutableList() }
        logic.trimLog(s)
        assertEquals(256, s.log.sumOf { it.size })
        assertEquals(full, s.log.last())
        val dropped = 6 * CIVS.size - 256
        assertEquals(6 - dropped / CIVS.size, s.log.size)
        assertEquals(dropped % CIVS.size, s.log[0][0].id)

        val big = logic.emptyState()
        big.log = mutableListOf(MutableList(300) { Draw(it % CIVS.size, it + 1, 300) })
        logic.trimLog(big)
        assertEquals("a current round longer than the cap stays whole", 300, big.log[0].size)
    }
}
