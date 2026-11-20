package net.frcp.app.picker.draws

import net.frcp.app.picker.ERROR_SOUND
import net.frcp.app.picker.SwipeKind
import net.frcp.app.picker.TestPicker
import net.frcp.app.picker.drawnPicker
import net.frcp.app.picker.jingleOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VmUndoTest {
    private fun undoneAndRedone(): Pair<TestPicker, List<Int>> {
        val p = drawnPicker(3)
        val three = p.round
        p.vm.reset()
        repeat(2) { p.vm.undo() }
        repeat(2) { p.vm.redo() }
        return p to three
    }

    private fun drawnAfterUndo() = drawnPicker(2).also { p ->
        p.vm.undo()
        p.vm.draw()
    }

    private fun undoneAndRedoneOnNothing() = TestPicker().also { p ->
        p.vm.undo()
        p.vm.redo()
    }

    private fun undone(draws: Int, undos: Int) = drawnPicker(draws).also { p -> repeat(undos) { p.vm.undo() } }

    @Test
    fun t157_undoTakesBackAReset() {
        val p = drawnPicker(3)
        val three = p.round
        p.vm.reset()
        p.vm.undo()
        assertEquals(three, p.round)
    }

    @Test
    fun t158_undoTakesBackADraw() {
        val p = drawnPicker(3)
        val two = p.round.take(2)
        p.vm.undo()
        assertEquals(two, p.round)
    }

    @Test
    fun t159_undoPlaysTheJingleOfTheCivItGoesBackTo() {
        val p = drawnPicker(3)
        val second = p.round[1]
        p.vm.undo()
        assertEquals(jingleOf(second), p.lastSound)
    }

    @Test
    fun t160_redoBringsBackADrawAndAReset() {
        assertEquals(emptyList<Int>(), undoneAndRedone().first.round)
    }

    @Test
    fun t161_redoneStepsAreSaved() {
        val (p, three) = undoneAndRedone()
        assertEquals(listOf(three, emptyList()), p.saved!!.log.map { r -> r.map { it.id } })
    }

    @Test
    fun t162_redoAfterANewDrawSoundsTheError() {
        val p = drawnAfterUndo()
        p.vm.redo()
        assertEquals(ERROR_SOUND, p.lastSound)
    }

    @Test
    fun t163_redoAfterANewDrawChangesNothing() {
        val p = drawnAfterUndo()
        val round = p.round
        p.vm.redo()
        assertEquals(round, p.round)
    }

    @Test
    fun t164_undoAndRedoWithNothingToStepSoundTheError() {
        assertEquals(listOf(ERROR_SOUND, ERROR_SOUND), undoneAndRedoneOnNothing().sounds.played)
    }

    @Test
    fun t165_undoAndRedoWithNothingToStepDrawNothing() {
        assertNull(undoneAndRedoneOnNothing().current)
    }

    @Test
    fun t166_undoGoesBackTwoHundredSteps() {
        assertEquals(10, undone(draws = 210, undos = 200).totalDraws)
    }

    @Test
    fun t167_undoPastTwoHundredStepsSoundsTheError() {
        assertEquals(ERROR_SOUND, undone(draws = 210, undos = 201).lastSound)
    }

    @Test
    fun t168_undoPastTwoHundredStepsChangesNothing() {
        assertEquals(10, undone(draws = 210, undos = 201).totalDraws)
    }

    @Test
    fun t169_swipeRightShowsTheCivUndoGoesBackTo() {
        val p = drawnPicker(2)
        assertEquals(p.round[0], p.vm.swipeCell(SwipeKind.Undo))
    }

    @Test
    fun t170_swipeRightUndoes() {
        val p = drawnPicker(2)
        val first = p.round[0]
        p.vm.swipe(SwipeKind.Undo)
        assertEquals(first, p.current)
    }

    @Test
    fun t171_swipeRightShowsRandomBeforeTheFirstDraw() {
        val p = drawnPicker(1)
        assertEquals(p.vm.randomCell, p.vm.swipeCell(SwipeKind.Undo))
    }

    @Test
    fun t172_swipeRightShowsNothingWithNothingToUndo() {
        assertNull(TestPicker().vm.swipeCell(SwipeKind.Undo))
    }
}
