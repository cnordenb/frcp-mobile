package net.frcp.app.picker

import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import net.frcp.app.device.Assets
import net.frcp.app.device.PresetFiles
import net.frcp.app.device.SoundPlayer
import net.frcp.app.device.Store
import net.frcp.app.device.UiSound
import net.frcp.app.logic.CIVS
import net.frcp.app.logic.MersenneTwister
import net.frcp.app.logic.PickerState
import net.frcp.app.logic.logic
import net.frcp.app.logic.makeState
import net.frcp.app.logic.seeded
import net.frcp.app.ui.Images

class MemoryStore(override var settings: String? = null, override var state: String? = null) : Store

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

private object NoFiles : PresetFiles {
    override suspend fun write(uri: Uri, text: String) = error("the tests write no files")
    override suspend fun read(uri: Uri, max: Int) = error("the tests read no files")
}

class TestPicker(
    state: PickerState? = null,
    settings: Settings = Settings(),
    rng: MersenneTwister? = seeded(),
) {
    val store = MemoryStore(settings.toJson(), state?.let(logic::serialiseState))
    val sounds = SoundLog()
    val vm = PickerViewModel(logic, Images { error("the tests decode no images") }, store, sounds, NoFiles, rng)

    val saved get() = logic.parseState(store.state)

    val savedSettings get() = Settings.parse(store.settings)

    val lastSound get() = sounds.played.lastOrNull()

    val current get() = vm.view.current?.id

    val round get() = vm.view.state.log.last().map { it.id }

    val totalDraws get() = vm.view.state.log.sumOf { it.size }

    fun effect(): Effect? = runBlocking { withTimeoutOrNull(100) { vm.effects.first() } }
}

val ERROR_SOUND = uiSound(UiSound.Error)

fun uiSound(s: UiSound) = Assets.uiSound(s.file)

fun jingleOf(id: Int?, legacy: Boolean = true): String {
    if (id == null) return Assets.jingle(Assets.RANDOM, legacy)
    val c = logic.civs[id]
    return Assets.jingle(c.file, legacy && c.legacy)
}

fun drawnPicker(n: Int, settings: Settings = Settings()) =
    TestPicker(settings = settings).also { p -> repeat(n) { p.vm.draw() } }

fun onlyCiv(name: String) = makeState(off = CIVS.filter { it.name != name }.map { it.name })

fun sharedLink(p: TestPicker, withDraws: Boolean): String {
    p.vm.shareLink(withDraws)
    return (p.effect() as Effect.Share).text
}
