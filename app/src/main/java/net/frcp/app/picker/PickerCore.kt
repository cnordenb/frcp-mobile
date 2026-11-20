package net.frcp.app.picker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import net.frcp.app.device.Assets
import net.frcp.app.device.SoundPlayer
import net.frcp.app.device.Store
import net.frcp.app.device.UiSound
import net.frcp.app.logic.Draw
import net.frcp.app.logic.Logic
import net.frcp.app.logic.MersenneTwister
import net.frcp.app.logic.Picker
import net.frcp.app.logic.PickerState

abstract class PickerCore(
    val logic: Logic,
    private val store: Store,
    protected val sounds: SoundPlayer,
    private val rng: MersenneTwister?,
) : ViewModel() {
    val randomCell = logic.civs.size

    private val picker = Picker(rng)
    protected var state = logic.parseState(store.state) ?: logic.emptyState()
    private val undoStack = ArrayDeque<PickerState>()
    private val redoStack = ArrayDeque<PickerState>()
    private var flash: String? = null

    var settings by mutableStateOf(Settings.parse(store.settings))
        private set
    var view by mutableStateOf(PickerView(logic, state.deepCopy(), null))
        private set
    var tab by mutableIntStateOf(0)
        protected set

    init {
        for (s in UiSound.entries) sounds.load(Assets.uiSound(s.file))
        loadAhead()
    }

    override fun onCleared() {
        sounds.release()
    }

    protected fun saveState() {
        store.state = logic.serialiseState(state)
    }

    protected fun publish() {
        view = PickerView(logic, state.deepCopy(), flash)
        loadAhead()
    }

    protected fun commit() {
        flash = null
        saveState()
        publish()
    }

    protected fun keepSettings(changed: Settings) {
        settings = changed
        store.settings = settings.toJson()
    }

    protected fun updateSettings(change: Settings.() -> Settings) {
        keepSettings(settings.change())
        loadAhead()
    }

    protected fun sfx(s: UiSound) {
        if (settings.uiSounds) sounds.play(Assets.uiSound(s.file))
    }

    private fun jinglePath(id: Int?): String {
        val legacy = settings.legacyJingles
        if (id == null) return Assets.jingle(Assets.RANDOM, legacy)
        val c = logic.civs[id]
        return Assets.jingle(c.file, legacy && c.legacy)
    }

    protected fun jingle(id: Int?) {
        if (settings.jingles) sounds.play(jinglePath(id))
    }

    protected fun jingleOrButton(id: Int?) {
        if (settings.jingles) jingle(id) else sfx(UiSound.Button)
    }

    protected fun loadAhead() {
        if (!settings.jingles) return
        sounds.load(jinglePath(null))
        nextDraw()?.let { sounds.load(jinglePath(it)) }
    }

    fun playJingle() = jingle(currentId())

    protected fun currentId() = logic.round(state).lastOrNull()?.id
    fun iconCell(id: Int?) = id ?: randomCell
    private fun candidates() = logic.drawCandidates(state, settings.freshness)
    private fun nextDraw(from: List<Int> = candidates()) = picker.next(from)

    fun draw(silent: Boolean = false) {
        val id = if (logic.poolIds(state).isEmpty()) null else nextDraw()
        if (id == null) {
            if (!silent) sfx(UiSound.Error)
            flash = if (rng != null) "Empty pool!" else "No random source"
            publish()
            return
        }
        recordUndo()
        logic.recordDraw(state, id)
        commit()
        if (!silent) jingleOrButton(id)
    }

    fun reset() {
        if (logic.round(state).isEmpty()) return
        recordUndo()
        logic.closeRound(state)
        commit()
        if (settings.jingles && tab != 2) jingle(null) else sfx(UiSound.Button)
    }

    private fun rerollCivs(): List<Int>? {
        val entries = logic.round(state)
        val current = entries.lastOrNull() ?: return null
        if (rng == null) return null
        entries.removeAt(entries.lastIndex)
        val others = candidates().filter { it != current.id }
        entries.add(current)
        return others.ifEmpty { null }
    }

    fun reroll() {
        val others = rerollCivs()
        val id = others?.let { nextDraw(it) }
        if (id == null) {
            sfx(UiSound.Error)
            return
        }
        redoStack.clear()
        val entries = logic.round(state)
        entries.removeAt(entries.lastIndex)
        entries.add(Draw(id, logic.drawnCount(state) + 1, logic.poolIds(state).size))
        commit()
        jingleOrButton(id)
    }

    private fun recordUndo() {
        undoStack.addLast(state.deepCopy())
        if (undoStack.size > UNDO_CAP) undoStack.removeFirst()
        redoStack.clear()
    }

    protected fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }

    private fun step(from: ArrayDeque<PickerState>, to: ArrayDeque<PickerState>) {
        if (from.isEmpty()) {
            sfx(UiSound.Error)
            return
        }
        to.addLast(state.deepCopy())
        state = from.removeLast()
        commit()
        jingleOrButton(currentId())
    }

    fun undo() = step(undoStack, redoStack)
    fun redo() = step(redoStack, undoStack)

    fun clearLog() {
        sfx(UiSound.Button)
        clearHistory()
        state.log = mutableListOf(logic.round(state))
        commit()
    }

    fun swipeCell(kind: SwipeKind): Int? = when (kind) {
        SwipeKind.Draw -> if (logic.poolIds(state).isNotEmpty()) nextDraw() else null
        SwipeKind.Reset -> if (logic.round(state).isNotEmpty()) randomCell else null
        SwipeKind.Reroll -> rerollCivs()?.let { nextDraw(it) }
        SwipeKind.Undo -> undoStack.lastOrNull()?.let { iconCell(logic.round(it).lastOrNull()?.id) }
    }

    fun swipe(kind: SwipeKind) = when (kind) {
        SwipeKind.Draw -> draw()
        SwipeKind.Undo -> undo()
        SwipeKind.Reroll -> reroll()
        SwipeKind.Reset -> reset()
    }

    private fun poolKey() = state.edition + "|" + state.off.sorted().joinToString(",")

    private fun changePool(mutate: () -> Unit) {
        val before = poolKey()
        mutate()
        logic.applyEdition(state)
        if (poolKey() == before) {
            publish()
            return
        }
        clearHistory()
        if (settings.autoReset) logic.closeRound(state)
        commit()
    }

    private fun setOn(ids: List<Int>, on: Boolean) {
        if (on) state.off -= ids.toSet() else state.off += ids
    }

    private fun editionIds() = logic.civs.filter { logic.inEdition(it, state.edition) }.map { it.id }
    private fun dlcIds(dlc: String) =
        logic.civs.filter { it.dlc == dlc && logic.inEdition(it, state.edition) }.map { it.id }
    private fun olderIds(ed: String) =
        logic.civs.filter { it.dlc in logic.editionInfo.getValue(ed).olderDlcs }.map { it.id }

    fun toggleCiv(id: Int) {
        sfx(UiSound.Click)
        changePool { setOn(listOf(id), !logic.isOn(state, id)) }
    }

    fun toggleDlc(dlc: String) {
        val ids = dlcIds(dlc)
        val on = ids.none { logic.isOn(state, it) }
        sfx(UiSound.Button)
        changePool { setOn(ids, on) }
    }

    fun enableAll() {
        sfx(UiSound.Button)
        changePool { setOn(editionIds(), true) }
    }

    fun disableAll() {
        sfx(UiSound.Button)
        changePool { setOn(logic.civs.map { it.id }, false) }
    }

    fun setEdition(ed: String) {
        sfx(UiSound.View)
        if (ed == state.edition) {
            publish()
            return
        }
        changePool {
            state.edition = ed
            logic.applyEdition(state)
            if (settings.autoToggle) setOn(olderIds(ed), true)
        }
    }

    fun toggleAutoToggle() {
        updateSettings { copy(autoToggle = !autoToggle) }
        sfx(UiSound.Button)
        changePool { setOn(olderIds(state.edition), settings.autoToggle) }
    }

    fun toggleAutoReset() {
        updateSettings { copy(autoReset = !autoReset) }
        sfx(UiSound.Button)
    }

    private companion object {
        const val UNDO_CAP = 200
    }
}
