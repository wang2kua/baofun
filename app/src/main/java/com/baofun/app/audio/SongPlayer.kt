package com.baofun.app.audio

import android.content.Context
import android.media.MediaPlayer

/**
 * Plays baby songs bundled in assets/songs in shuffled order, looping forever.
 * Accepts the common audio formats Android supports natively
 * (.mp3/.ogg/.opus/.m4a). If no songs are present, start() is a safe no-op.
 *
 * Shuffle: the playlist is shuffled when playback starts and re-shuffled each
 * time a full cycle completes, so songs play in random order without repeating
 * within a cycle.
 */
class SongPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var playlist: List<String> = emptyList()
    private var index = 0
    private var volume = 1.0f

    private val supportedExtensions = listOf(".mp3", ".ogg", ".opus", ".m4a")

    private fun loadPlaylist(): List<String> =
        try {
            (context.assets.list("songs") ?: emptyArray())
                .filter { name -> supportedExtensions.any { name.endsWith(it, ignoreCase = true) } }
                .sorted()
                .shuffled()
        } catch (e: Exception) { emptyList() }

    fun hasSongs(): Boolean = loadPlaylist().isNotEmpty()

    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
        player?.setVolume(volume, volume)
    }

    fun start() {
        playlist = loadPlaylist()
        if (playlist.isEmpty()) return
        index = 0
        playCurrent()
    }

    private fun playCurrent() {
        stop()
        val name = playlist.getOrNull(index) ?: return
        try {
            val mp = MediaPlayer()
            mp.setVolume(volume, volume)
            context.assets.openFd("songs/$name").use { afd ->
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            mp.setOnCompletionListener {
                index += 1
                if (index >= playlist.size) {
                    // Re-shuffle for the next cycle so order varies each round.
                    playlist = playlist.shuffled()
                    index = 0
                }
                playCurrent()
            }
            mp.prepare()
            mp.start()
            player = mp
        } catch (e: Exception) {
            player = null
        }
    }

    fun stop() {
        player?.let { try { it.stop() } catch (_: IllegalStateException) {}; it.release() }
        player = null
    }
}
