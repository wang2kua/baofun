package com.baofun.app.audio

import android.content.Context
import android.media.MediaPlayer

/**
 * Plays baby songs bundled in assets/songs (e.g. user-supplied mp3s), looping
 * through the playlist. If no songs are present, play() is a safe no-op.
 */
class SongPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var playlist: List<String> = emptyList()
    private var index = 0

    private fun loadPlaylist(): List<String> =
        try {
            (context.assets.list("songs") ?: emptyArray())
                .filter { it.endsWith(".mp3", ignoreCase = true) }
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
