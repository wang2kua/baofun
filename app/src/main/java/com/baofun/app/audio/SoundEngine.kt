package com.baofun.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Low-latency playback of short animal sounds bundled in assets/sfx/animals.
 * Files are loaded once into a SoundPool; playSound(index) picks by index
 * (wrapping). Safe no-op if no sounds loaded.
 */
class SoundEngine(private val context: Context) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = ArrayList<Int>()
    private var volume = 1.0f

    init {
        try {
            val files = (context.assets.list("sfx/animals") ?: emptyArray())
                .filter { it.endsWith(".ogg", true) || it.endsWith(".oga", true) ||
                          it.endsWith(".wav", true) || it.endsWith(".mp3", true) }
                .sorted()
            for (name in files) {
                context.assets.openFd("sfx/animals/$name").use { afd ->
                    val id = pool.load(afd.fileDescriptor, afd.startOffset, afd.length, 1)
                    soundIds.add(id)
                }
            }
        } catch (e: Exception) { /* leave soundIds empty -> playSound is no-op */ }
    }

    fun count(): Int = soundIds.size

    fun setVolume(v: Float) { volume = v.coerceIn(0f, 1f) }

    fun playSound(index: Int) {
        if (soundIds.isEmpty()) return
        val id = soundIds[((index % soundIds.size) + soundIds.size) % soundIds.size]
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() { pool.release(); soundIds.clear() }
}
