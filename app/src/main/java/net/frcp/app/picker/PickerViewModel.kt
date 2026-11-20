package net.frcp.app.picker

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.frcp.app.BuildConfig
import net.frcp.app.device.Assets
import net.frcp.app.device.PresetFiles
import net.frcp.app.device.SoundPlayer
import net.frcp.app.device.Store
import net.frcp.app.device.UiSound
import net.frcp.app.logic.Logic
import net.frcp.app.logic.MersenneTwister
import net.frcp.app.logic.SharedLink
import net.frcp.app.ui.Images
import net.frcp.app.ui.Page
import java.time.LocalDateTime
import java.util.Locale

class PickerViewModel(
    logic: Logic,
    val images: Images,
    store: Store,
    sounds: SoundPlayer,
    private val files: PresetFiles,
    rng: MersenneTwister? = runCatching { MersenneTwister() }.getOrNull(),
) : PickerCore(logic, store, sounds, rng) {
    private var launched = false
    private var toastSerial = 0L
    private val effectChannel = Channel<Effect>(Channel.BUFFERED)

    var menuOpen by mutableStateOf(false)
        private set
    var dialog by mutableStateOf<Dialog?>(null)
        private set
    var toast by mutableStateOf<Toast?>(null)
        private set
    val effects = effectChannel.receiveAsFlow()

    internal fun decodeAhead() {
        viewModelScope.launch(Dispatchers.Default) {
            images[Assets.page("background")]
            for (tab in 0..2) images[Page.strip(tab)]
            for (c in logic.civs) images[Assets.civIcon(c.file)]
        }
    }

    fun onLaunch(uri: String?) {
        if (launched) return
        launched = true
        if (settings.newRoundOnVisit) logic.closeRound(state)
        val code = linkCode(uri)
        if (code != null) openLinkCode(code) else if (settings.drawOnLoad) draw(silent = true)
        saveState()
        publish()
    }

    fun onLink(uri: String?) {
        linkCode(uri)?.let(::openLinkCode)
    }

    private fun linkCode(uri: String?) = uri?.substringAfter('#', "")?.takeIf { it.startsWith("p=") }?.substring(2)

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

    fun toggleSetting(change: Settings.() -> Settings) {
        sfx(UiSound.Button)
        updateSettings(change)
    }

    private fun playMute() = sounds.play(Assets.uiSound(UiSound.Mute.file))

    fun setJingles(on: Boolean) {
        updateSettings { copy(jingles = on) }
        if (on) jingle(currentId()) else playMute()
    }

    fun setUiSounds(on: Boolean) {
        updateSettings { copy(uiSounds = on) }
        if (on) sfx(UiSound.Button) else playMute()
    }

    fun setLegacyJingles(legacy: Boolean) {
        updateSettings { copy(legacyJingles = legacy) }
        val c = currentId()?.let { logic.civs[it] }
        if (c == null || c.legacy) jingle(currentId()) else sfx(UiSound.View)
    }

    fun setFreshN(value: Int, max: Int) {
        val n = if (value >= max) Settings.FRESH_MOST else value
        if (n != settings.freshN) keepSettings(settings.copy(freshN = n))
    }

    fun freshNSettled() = loadAhead()

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

    fun shareLink(withDraws: Boolean) {
        sfx(UiSound.Button)
        effect(Effect.Share(SITE + "#p=" + logic.shareCode(state, withDraws)))
    }

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
            message("Link not recognised", "This link isn't a valid $APP_NAME link, so nothing has changed.")
            return
        }
        offerLink(link)
    }

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

    fun linkFailed() {
        sfx(UiSound.Error)
        showToast("No app here can open that")
    }

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
        viewModelScope.launch {
            if (files.write(uri, text)) {
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
        viewModelScope.launch {
            val file = files.read(uri, PRESET_MAX_BYTES)
            loadPresetText(file.name ?: "That file", file.text)
        }
    }

    internal fun loadPresetText(name: String, text: String?) {
        val preset = text?.let(logic::parsePreset)
        if (preset == null) {
            sfx(UiSound.Error)
            message("Preset not recognised", "$name isn't a $APP_NAME preset, so nothing has changed.")
            return
        }
        clearHistory()
        logic.applyPreset(state, preset)
        commit()
        jingleOrButton(currentId())
        showToast("Preset loaded")
    }

    companion object {
        const val SITE = "https://frcp.net/"
        const val GITHUB = "https://github.com/cnordenb/frcp_html"
        const val DONATE = "https://ko-fi.com/hjoerleif"
        private const val APP_NAME = "Fresh Random Civ Picker"
        private const val PRESET_MAX_BYTES = 65536
    }
}
