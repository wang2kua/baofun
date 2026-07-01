package com.baofun.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.baofun.app.logic.FadeCurve

/**
 * Sleep-mode player: loops one bundled audio file (nature/pink-noise) at a base
 * volume, then over the final fade window ramps volume to 0 and stops. Drives
 * volume via a FadeCurve, ticking on the main looper. onFinished is called when
 * the fade completes so the Activity can enter STOPPED.
 */
class SleepPlayer(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var curve: FadeCurve? = null
    private var startedAt = 0L
    private var baseVolume = 1.0f
    var onFinished: (() -> Unit)? = null

    private fun firstFile(dir: String): String? =
        try {
            (context.assets.list(dir) ?: emptyArray())
                .filter { it.endsWith(".ogg", true) || it.endsWith(".mp3", true) ||
                          it.endsWith(".wav", true) || it.endsWith(".oga", true) }
                .sorted().firstOrNull()
        } catch (e: Exception) { null }

    fun setBaseVolume(v: Float) { baseVolume = v.coerceIn(0f, 1f) }

    /** Start looping the sleep sound with a total window and final fade (ms). */
    fun start(totalMillis: Long, fadeMillis: Long, nowMillis: Long) {
        stop()
        val name = firstFile("sfx/sleep") ?: return
        try {
            val mp = MediaPlayer()
            context.assets.openFd("sfx/sleep/$name").use { afd ->
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            mp.isLooping = true
            mp.setVolume(baseVolume, baseVolume)
            mp.prepare()
            mp.start()
            player = mp
            curve = FadeCurve(totalMillis, fadeMillis, baseVolume)
            startedAt = nowMillis
            handler.post(fadeTick)
        } catch (e: Exception) { player = null }
    }

    private val fadeTick = object : Runnable {
        override fun run() {
            val c = curve ?: return
            val elapsed = System.currentTimeMillis() - startedAt
            val v = c.volumeAt(elapsed)
            player?.setVolume(v, v)
            if (c.isFinished(elapsed)) {
                stop()
                onFinished?.invoke()
            } else {
                handler.postDelayed(this, 500L)
            }
        }
    }

    fun stop() {
        handler.removeCallbacks(fadeTick)
        player?.let { try { it.stop() } catch (_: IllegalStateException) {}; it.release() }
        player = null
        curve = null
    }
}
