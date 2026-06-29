package com.baofun.app.audio

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Plays a soft pitched tone per note index. Uses ToneGenerator's DTMF tones as
 * a dependency-free pitch source (no bundled audio files). Note index is mapped
 * onto a rising set of tone constants so different screen zones sound different.
 */
class ToneEngine {
    private var gen: ToneGenerator? =
        try { ToneGenerator(AudioManager.STREAM_MUSIC, 70) } catch (e: RuntimeException) { null }

    // A rising scale of DTMF tones — index 0 = lowest, ascending.
    private val tones = intArrayOf(
        ToneGenerator.TONE_DTMF_0,
        ToneGenerator.TONE_DTMF_1,
        ToneGenerator.TONE_DTMF_2,
        ToneGenerator.TONE_DTMF_3,
        ToneGenerator.TONE_DTMF_4,
        ToneGenerator.TONE_DTMF_5,
        ToneGenerator.TONE_DTMF_6,
        ToneGenerator.TONE_DTMF_7,
        ToneGenerator.TONE_DTMF_8
    )

    fun playNote(index: Int) {
        val g = gen ?: return
        val tone = tones[index.coerceIn(0, tones.size - 1)]
        g.startTone(tone, 180) // 180ms
    }

    fun setVolumePercent(percent: Int) {
        gen?.release()
        gen = try {
            ToneGenerator(AudioManager.STREAM_MUSIC, percent.coerceIn(0, 100))
        } catch (e: RuntimeException) { null }
    }

    fun release() { gen?.release(); gen = null }
}
