package com.baofun.app.audio

import android.content.Context
import android.media.MediaPlayer

/**
 * Plays baby songs bundled in assets/songs, looping through the playlist.
 * Accepts the common audio formats Android supports natively (.mp3/.ogg/.opus).
 * If no songs are present, start() is a safe no-op.
 */
class SongPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var playlist: List<String> = emptyList()
    private var index = 0

    private val supportedExtensions = listOf(".mp3", ".ogg", ".opus")

    private fun loadPlaylist(): List<String> =
        try {
            (context.assets.list("songs") ?: emptyArray())
                .filter { name -> supportedExtensions.any { name.endsWith(it, ignoreCase = true) } }
                .sorted()
        } catch (e: Exception) { emptyList() }

    fun hasSongs(): Boolean = loadPlaylist().isNotEmpty()

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
            context.assets.openFd("songs/$name").use { afd ->
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            mp.setOnCompletionListener {
                index = (index + 1) % playlist.size
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
