package net.frcp.app

import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.MediaPlayer

/** The UI sounds, by their file names in the web version's sounds/ui. */
enum class UiSound(val file: String) {
    Button("button_sound"),
    Tab("parchment_sound"),
    Click("click_sound"),
    Error("error_sound"),
    View("view_sound"),
    Mute("mute"),
}

/** Plays the jingles and UI sounds, by their paths in the assets. */
interface SoundPlayer {
    /** Starts loading the sound at [path], so it plays without delay later. */
    fun load(path: String)

    fun play(path: String)

    fun release()
}

/**
 * Plays the jingles and UI sounds from the assets, one at a time as on the desktop app: a new sound cuts off the one
 * playing. Each sound gets a player that stays prepared, so that a sound loaded ahead, such as the next draw's jingle,
 * starts at once. The players of the least recently used sounds are let go.
 */
class Sounds(private val assets: AssetManager) : SoundPlayer {
    private class Clip(val player: MediaPlayer) {
        var prepared = false
        var startWhenPrepared = false
    }

    private val clips = LinkedHashMap<String, Clip>(16, 0.75f, true)
    private var playing: Clip? = null

    override fun load(path: String) {
        clip(path)
    }

    override fun play(path: String) {
        stop()
        val c = clip(path) ?: return
        playing = c
        if (!c.prepared) {
            c.startWhenPrepared = true
            return
        }
        // Seeking takes a moment, so a sound already at its start, such as a preloaded jingle, plays without one.
        if (c.player.currentPosition != 0) c.player.seekTo(0)
        c.player.start()
    }

    fun stop() {
        val c = playing ?: return
        playing = null
        c.startWhenPrepared = false
        if (!c.prepared) return
        if (c.player.isPlaying) c.player.pause()
        c.player.seekTo(0)
    }

    override fun release() {
        playing = null
        for (c in clips.values) c.player.release()
        clips.clear()
    }

    private fun clip(path: String): Clip? {
        clips[path]?.let { return it }
        val c = runCatching { create(path) }.getOrNull() ?: return null
        clips[path] = c
        trim()
        return c
    }

    private fun create(path: String): Clip {
        val player = MediaPlayer()
        val c = Clip(player)
        player.setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),
        )
        try {
            assets.openFd(path).use { player.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
        } catch (e: Exception) {
            player.release()
            throw e
        }
        player.setOnPreparedListener {
            c.prepared = true
            if (c.startWhenPrepared) {
                c.startWhenPrepared = false
                it.start()
            }
        }
        // A sound that can't play stays silent.
        player.setOnErrorListener { _, _, _ -> true }
        player.prepareAsync()
        return c
    }

    private fun trim() {
        val it = clips.values.iterator()
        while (clips.size > CACHE_SIZE && it.hasNext()) {
            val c = it.next()
            if (c === playing) continue
            c.player.release()
            it.remove()
        }
    }

    private companion object {
        const val CACHE_SIZE = 12
    }
}
