package net.frcp.app

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.frcp.app.logic.Draw
import net.frcp.app.logic.GameData
import net.frcp.app.logic.Logic
import net.frcp.app.logic.MersenneTwister
import net.frcp.app.logic.Picker
import net.frcp.app.logic.PickerState
import net.frcp.app.logic.SharedLink
import net.frcp.app.ui.Images
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.Locale

/**
 * The picker on top of the rules in [Logic]: the saved state and settings, undo and redo, sound, share links and
 * presets, as the web version's script.js runs them. The screen reads [view], [settings], [tab], [dialog] and [toast],
 * and calls the actions.
 */
class PickerViewModel(app: Application) : AndroidViewModel(app) {
    val logic = Logic(GameData.parse(app.assets.open(Assets.DATA).bufferedReader().use { it.readText() }))
    val images = Images(app.assets)

    /** The icon cell that stands for random: after every civ's, as in the web version's civ atlas. */
    val randomCell = logic.civs.size

    private val store = Store(app)
    private val sounds = Sounds(app.assets)
    private val rng = runCatching { MersenneTwister() }.getOrNull()
    private val picker = Picker(rng)
    private var state = logic.parseState(store.state) ?: logic.emptyState()
    private val undoStack = ArrayDeque<PickerState>()
    private val redoStack = ArrayDeque<PickerState>()
    private var flash: String? = null
    private var launched = false
    private var toastSerial = 0L
    private val effectChannel = Channel<Effect>(Channel.BUFFERED)

    var settings by mutableStateOf(Settings.parse(store.settings))
        private set
    var view by mutableStateOf(PickerView(logic, state.deepCopy(), null))
        private set
    var tab by mutableIntStateOf(0)
        private set
    var menuOpen by mutableStateOf(false)
        private set
    var dialog by mutableStateOf<Dialog?>(null)
        private set
    var toast by mutableStateOf<Toast?>(null)
        private set
    val effects = effectChannel.receiveAsFlow()

    init {
        for (s in UiSound.entries) sounds.load(Assets.uiSound(s.file))
        loadAhead()
        // The civ icons decode away from the screen's thread, ready for the logs and the Pool tab.
        viewModelScope.launch(Dispatchers.Default) {
            for (c in logic.civs) images[Assets.civIcon(c.file)]
        }
    }

    override fun onCleared() {
        sounds.release()
    }

    // ---- Opening the app ----

    /** The app opens, from the launcher or from a share link: a visit, as when the web page opens. */
    fun onLaunch(uri: String?) {
        if (launched) return
        launched = true
        if (settings.newRoundOnVisit) logic.closeRound(state)
        val code = linkCode(uri)
        if (code != null) openLinkCode(code) else if (settings.drawOnLoad) draw(silent = true)
        saveState()
        publish()
    }

    /** A share link opens in the app while it's running. */
    fun onLink(uri: String?) {
        linkCode(uri)?.let(::openLinkCode)
    }

    private fun linkCode(uri: String?) = uri?.substringAfter('#', "")?.takeIf { it.startsWith("p=") }?.substring(2)

    // ---- Saving and showing ----

    private fun saveState() {
        store.state = logic.serialiseState(state)
    }

    private fun publish() {
        view = PickerView(logic, state.deepCopy(), flash)
        loadAhead()
    }

    private fun commit() {
        flash = null
        saveState()
        publish()
    }

    private fun updateSettings(change: Settings.() -> Settings) {
        settings = settings.change()
        store.settings = settings.toJson()
        loadAhead()
    }

    private fun showToast(text: String) {
        toast = Toast(text, ++toastSerial)
    }

    private fun message(title: String, text: String) {
        menuOpen = false
        dialog = Dialog.Message(title, text)
    }

    private fun effect(e: Effect) {
        effectChannel.trySend(e)
    }

    // ---- Sound ----

    private fun sfx(s: UiSound) {
        if (settings.uiSounds) sounds.play(Assets.uiSound(s.file))
    }

    private fun jinglePath(id: Int?): String {
        val legacy = settings.legacyJingles
        if (id == null) return Assets.jingle(Assets.RANDOM, legacy)
        val c = logic.civs[id]
        return Assets.jingle(c.file, legacy && c.legacy)
    }

    private fun jingle(id: Int?) {
        if (settings.jingles) sounds.play(jinglePath(id))
    }

    /** Of the jingles, only the ones that a draw or a reset plays next load ahead: the next draw's and random's. */
    private fun loadAhead() {
        if (!settings.jingles) return
        sounds.load(jinglePath(null))
        nextDraw()?.let { sounds.load(jinglePath(it)) }
    }

    /** The civ icon's tap plays the current civ's jingle. */
    fun playJingle() = jingle(currentId())

    // ---- Drawing ----

    private fun currentId() = logic.round(state).lastOrNull()?.id
    fun iconCell(id: Int?) = id ?: randomCell
    private fun candidates() = logic.drawCandidates(state, settings.freshness)
    private fun nextDraw(from: List<Int> = candidates()) = picker.next(from)

    /** The draw was picked ahead and its jingle loaded, so it's revealed at once: name, icon and jingle together. */
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
        if (silent) return
        if (settings.jingles) jingle(id) else sfx(UiSound.Button)
    }

    fun reset() {
        if (logic.round(state).isEmpty()) return
        recordUndo()
        logic.closeRound(state)
        commit()
        if (settings.jingles && tab != 2) jingle(null) else sfx(UiSound.Button)
    }

    /**
     * Re-rolling swaps the current civ for another of the civs it was drawn from. Those are nearly always among the
     * next draw's civs, so the next draw ahead usually stands, and the swap is revealed as quickly as a draw. The
     * swapped-out civ goes back into the round, so it can still come up later. The swap stands in for the draw, so
     * neither undo nor continuous freshness records it, but it still clears redo, which would otherwise undo it.
     */
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
        if (settings.jingles) jingle(id) else sfx(UiSound.Button)
    }

    // Undo and redo cover draws and resets for this visit, but not re-rolls. Changing the pool clears them, and so do
    // clearing the log and loading a preset or link.

    private fun pushUndo(snap: PickerState) {
        undoStack.addLast(snap)
        if (undoStack.size > UNDO_CAP) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun recordUndo() = pushUndo(state.deepCopy())

    private fun clearHistory() {
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
        if (settings.jingles) jingle(currentId()) else sfx(UiSound.Button)
    }

    fun undo() = step(undoStack, redoStack)
    fun redo() = step(redoStack, undoStack)

    fun clearLog() {
        sfx(UiSound.Button)
        clearHistory()
        state.log = mutableListOf(logic.round(state))
        commit()
    }

    /** The icon cell of the civ a swipe leads to, or null when the swipe has nothing to do. */
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

    // ---- Civ pool ----

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

    private fun dlcIds(dlc: String) = logic.civs.filter { it.dlc == dlc && logic.inEdition(it, state.edition) }.map { it.id }
    private fun olderIds(ed: String) = logic.civs.filter { it.dlc in logic.editionInfo.getValue(ed).olderDlcs }.map { it.id }

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
        changePool { setOn(logic.civs.filter { logic.inEdition(it, state.edition) }.map { it.id }, true) }
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

    /** As on the desktop app, this also switches the edition's older civs on or off. */
    fun toggleAutoToggle() {
        updateSettings { copy(autoToggle = !autoToggle) }
        sfx(UiSound.Button)
        changePool { setOn(olderIds(state.edition), settings.autoToggle) }
    }

    fun toggleAutoReset() {
        updateSettings { copy(autoReset = !autoReset) }
        sfx(UiSound.Button)
    }

    // ---- Tabs, menu and settings ----

    fun selectTab(i: Int) {
        if (i == tab) return
        tab = i
        sfx(UiSound.Tab)
    }

    fun toggleMenu() {
        menuOpen = !menuOpen
    }

    fun closeMenu() {
        menuOpen = false
    }

    /** A checkbox setting, from the Options dialog or the Log tab. */
    fun toggleSetting(change: Settings.() -> Settings) {
        sfx(UiSound.Button)
        updateSettings(change)
    }

    fun setJingles(on: Boolean) {
        updateSettings { copy(jingles = on) }
        if (on) jingle(currentId()) else sounds.play(Assets.uiSound(UiSound.Mute.file))
    }

    fun setUiSounds(on: Boolean) {
        updateSettings { copy(uiSounds = on) }
        if (on) sfx(UiSound.Button) else sounds.play(Assets.uiSound(UiSound.Mute.file))
    }

    fun setLegacyJingles(legacy: Boolean) {
        updateSettings { copy(legacyJingles = legacy) }
        val c = currentId()?.let { logic.civs[it] }
        if (c == null || c.legacy) jingle(currentId()) else sfx(UiSound.View)
    }

    /** The freshness slider: at its right end, it means "the most", which follows the pool's size. */
    fun setFreshN(value: Int, max: Int) {
        val n = if (value >= max) Settings.FRESH_MOST else value
        if (n == settings.freshN) return
        settings = settings.copy(freshN = n)
        store.settings = settings.toJson()
    }

    /** The slider changes the civs the next draw can be once it's let go. */
    fun freshNSettled() = loadAhead()

    // ---- Dialogs ----

    private fun showDialog(d: Dialog) {
        menuOpen = false
        sfx(UiSound.Button)
        dialog = d
    }

    fun openOptions() = showDialog(Dialog.Options)
    fun openAbout() = showDialog(Dialog.About)
    fun openLinkDialog() = showDialog(Dialog.OpenLink)

    fun closeDialog() {
        sfx(UiSound.Button)
        dialog = null
    }

    fun openEditionPage() {
        sfx(UiSound.Button)
        val info = logic.editionInfo.getValue(state.edition)
        if (info.url != null) effect(Effect.OpenUrl(info.url)) else message(info.short, info.storeNote.orEmpty())
    }

    // ---- Links ----

    fun shareLink(withDraws: Boolean) {
        sfx(UiSound.Button)
        effect(Effect.Share(SITE + "#p=" + logic.shareCode(state, withDraws)))
    }

    /** The Open Link dialog's text: false where it isn't a link, so the dialog says so. */
    fun openLinkText(text: String): Boolean {
        val link = logic.parseShare(text)
        if (link == null) {
            sfx(UiSound.Error)
            return false
        }
        closeDialog()
        offerLink(link)
        return true
    }

    private fun openLinkCode(code: String) {
        val link = logic.parseShare(code)
        if (link == null) {
            sfx(UiSound.Error)
            message("Link not recognised", "This link isn't a valid Fresh Random Civ Picker link, so nothing has changed.")
            return
        }
        offerLink(link)
    }

    /** A link loads at once over a picker with nothing in it, and otherwise after a preview of what it replaces. */
    private fun offerLink(link: SharedLink) {
        if (!logic.hasContent(state)) {
            applyLink(link)
            showToast("Loaded the shared link")
            return
        }
        menuOpen = false
        dialog = Dialog.Preview(link)
    }

    fun loadPreviewedLink(link: SharedLink) {
        dialog = null
        applyLink(link)
        sfx(UiSound.Button)
        showToast("Loaded the shared link")
    }

    private fun applyLink(link: SharedLink) {
        clearHistory()
        logic.applyLink(state, link)
        commit()
    }

    fun openTechTree() {
        sfx(UiSound.Button)
        val c = currentId()?.let { logic.civs[it] }
        effect(Effect.OpenUrl("https://aoe2techtree.net/" + if (c != null) "#" + c.techTree else ""))
    }

    fun openExternal(url: String) {
        menuOpen = false
        sfx(UiSound.Button)
        effect(Effect.OpenUrl(url))
    }

    /** No app could open a link: the phone has no browser, say. */
    fun linkFailed() {
        sfx(UiSound.Error)
        showToast("No app here can open that")
    }

    // ---- Presets ----

    fun savePreset() {
        menuOpen = false
        sfx(UiSound.Button)
        val d = LocalDateTime.now()
        val name = String.format(
            Locale.ROOT, "FRCP Preset v%sa @%04d.%02d.%02d %02d%02d%02d.txt",
            BuildConfig.VERSION_NAME, d.year, d.monthValue, d.dayOfMonth, d.hour, d.minute, d.second,
        )
        effect(Effect.SavePreset(name))
    }

    fun writePreset(uri: Uri?) {
        uri ?: return
        val text = logic.presetText(state, settings.fresh)
        val resolver = getApplication<Application>().contentResolver
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    val out = runCatching { resolver.openOutputStream(uri, "wt") }.getOrNull() ?: resolver.openOutputStream(uri, "w")
                    checkNotNull(out).use { it.write(text.toByteArray()) }
                }.isSuccess
            }
            if (saved) {
                showToast("Preset saved")
            } else {
                sfx(UiSound.Error)
                message("Preset not saved", "The preset couldn't be written to that file.")
            }
        }
    }

    fun loadPreset() {
        menuOpen = false
        sfx(UiSound.Button)
        effect(Effect.LoadPreset)
    }

    fun readPreset(uri: Uri?) {
        uri ?: return
        val resolver = getApplication<Application>().contentResolver
        viewModelScope.launch {
            val (name, text) = withContext(Dispatchers.IO) {
                val name = runCatching {
                    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                        if (c.moveToFirst()) c.getString(0) else null
                    }
                }.getOrNull() ?: "That file"
                name to runCatching { resolver.openInputStream(uri)?.use { readText(it, PRESET_MAX_BYTES) } }.getOrNull()
            }
            val preset = text?.let(logic::parsePreset)
            if (preset == null) {
                sfx(UiSound.Error)
                message("Preset not recognised", "$name isn't a Fresh Random Civ Picker preset, so nothing has changed.")
                return@launch
            }
            clearHistory()
            logic.applyPreset(state, preset)
            commit()
            if (settings.jingles) jingle(currentId()) else sfx(UiSound.Button)
            showToast("Preset loaded")
        }
    }

    /** The text of a file up to max bytes long, or null for a longer one. */
    private fun readText(input: java.io.InputStream, max: Int): String? {
        val out = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        while (true) {
            val n = input.read(chunk)
            if (n < 0) break
            out.write(chunk, 0, n)
            if (out.size() > max) return null
        }
        return out.toByteArray().toString(Charsets.UTF_8)
    }

    companion object {
        const val SITE = "https://frcp.net/"
        const val GITHUB = "https://github.com/cnordenb/frcp_html"
        const val DONATE = "https://ko-fi.com/hjoerleif"
        private const val UNDO_CAP = 200
        private const val PRESET_MAX_BYTES = 65536
    }
}
