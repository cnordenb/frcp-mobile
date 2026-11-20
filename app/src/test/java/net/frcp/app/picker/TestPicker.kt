package net.frcp.app

import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import net.frcp.app.logic.MersenneTwister
import net.frcp.app.logic.PickerState
import net.frcp.app.logic.logic
import net.frcp.app.logic.seeded
import net.frcp.app.ui.Images

// What the view model tests share: the view model with the app's data, and stand-ins for what it needs from the
// phone, which keep what it saved and played. The random source is seeded, so every run draws the same civs.

/** Saved settings and state, kept in memory. */
class MemoryStore(override var settings: String? = null, override var state: String? = null) : Store

/** The sounds loaded and played, in order. */
class SoundLog : SoundPlayer {
    val loaded = mutableListOf<String>()
    val played = mutableListOf<String>()
    var released = false

    override fun load(path: String) {
        loaded += path
    }

    override fun play(path: String) {
        played += path
    }

    override fun release() {
        released = true
    }
}

/** No test picks a preset file: they load a preset's text with loadPresetText. */
private object NoFiles : PresetFiles {
    override suspend fun write(uri: Uri, text: String) = error("the tests write no files")
    override suspend fun read(uri: Uri, max: Int) = error("the tests read no files")
}

/** A picker as the app opens it, with this saved state and these settings. */
class TestPicker(
    state: PickerState? = null,
    settings: Settings = Settings(),
    rng: MersenneTwister? = seeded(),
) {
    val store = MemoryStore(settings.toJson(), state?.let(logic::serialiseState))
    val sounds = SoundLog()
    val vm = PickerViewModel(logic, Images { error("the tests decode no images") }, store, sounds, NoFiles, rng)

    /** The state the picker saved last. */
    val saved get() = logic.parseState(store.state)

    /** The settings the picker saved last. */
    val savedSettings get() = Settings.parse(store.settings)

    /** The last sound played, or null for none. */
    val lastSound get() = sounds.played.lastOrNull()

    /** The current civ's ID, or null before the round's first draw. */
    val current get() = vm.view.current?.id

    /** The IDs drawn this round, in order. */
    val round get() = vm.view.state.log.last().map { it.id }

    /** Something the picker asked the screen to do, if it did. */
    fun effect(): Effect? = runBlocking { withTimeoutOrNull(100) { vm.effects.first() } }
}

fun uiSound(s: UiSound) = Assets.uiSound(s.file)

/** The jingle of a civ, or random's for null: its legacy jingle, if it has one, with [legacy] jingles on. */
fun jingleOf(id: Int?, legacy: Boolean = true): String {
    if (id == null) return Assets.jingle(Assets.RANDOM, legacy)
    val c = logic.civs[id]
    return Assets.jingle(c.file, legacy && c.legacy)
}
