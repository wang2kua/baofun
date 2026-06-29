package com.baofun.app.voice

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Records short parent voice clips to the app's private files dir, lists them,
 * plays one back, and deletes. Files are .m4a (AAC in MPEG-4).
 */
class VoiceRecorder(private val context: Context) {
    private val dir: File = File(context.filesDir, "voices").apply { mkdirs() }
    private var recorder: MediaRecorder? = null
    private var playback: MediaPlayer? = null

    fun listClips(): List<File> =
        dir.listFiles { f -> f.extension.equals("m4a", true) }?.sortedBy { it.name } ?: emptyList()

    fun startRecording(): File {
        stopRecording()
        val out = File(dir, "voice_${listClips().size + 1}_${System.nanoTime()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
                else @Suppress("DEPRECATION") MediaRecorder()
        try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setOutputFile(out.absolutePath)
            r.prepare()
            r.start()
            recorder = r
        } catch (e: Exception) {
            try { r.release() } catch (_: Exception) {}
            recorder = null
        }
        return out
    }

    fun stopRecording() {
        recorder?.let { try { it.stop() } catch (_: RuntimeException) {}; it.release() }
        recorder = null
    }

    fun playRandomClip() {
        val clip = listClips().randomOrNull() ?: return
        playClip(clip)
    }

    fun playClip(file: File) {
        playback?.release()
        playback = null
        try {
            playback = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener { it.release() }
                prepare()
                start()
            }
        } catch (e: Exception) {
            playback?.release()
            playback = null
        }
    }

    fun deleteClip(file: File): Boolean = file.delete()

    fun hasClips(): Boolean = listClips().isNotEmpty()

    /** Release all media resources — call from Activity.onDestroy. */
    fun release() {
        stopRecording()
        playback?.release()
        playback = null
    }
}
