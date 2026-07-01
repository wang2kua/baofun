package com.baofun.app

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.baofun.app.audio.SongPlayer
import com.baofun.app.audio.ToneEngine
import com.baofun.app.logic.AutoStopTimer
import com.baofun.app.logic.EasterEggTrigger
import com.baofun.app.logic.GlissandoThrottle
import com.baofun.app.logic.PitchZones
import com.baofun.app.parent.ParentMenuController
import com.baofun.app.system.Haptics
import com.baofun.app.system.ScreenController
import com.baofun.app.voice.VoiceRecorder

class MainActivity : Activity() {

    private enum class Mode { PLAY, SONG, STOPPED }

    private lateinit var view: PlayView
    private lateinit var screen: ScreenController
    private lateinit var haptics: Haptics
    private lateinit var tones: ToneEngine
    private lateinit var songs: SongPlayer
    private lateinit var recorder: VoiceRecorder
    private lateinit var menu: ParentMenuController

    private val zones = PitchZones(3, 3)
    private val egg = EasterEggTrigger(8, 10)
    private val glissando = GlissandoThrottle(80L)
    private val timer = AutoStopTimer(10 * 60_000L)

    private val handler = Handler(Looper.getMainLooper())
    private var mode = Mode.PLAY
    private var isRecording = false

    private val timerTick = object : Runnable {
        override fun run() {
            if (mode != Mode.STOPPED && timer.isExpired(System.currentTimeMillis())) {
                enterStopped()
            } else {
                handler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = PlayView(this)
        setContentView(view)

        screen = ScreenController(this)
        haptics = Haptics(this)
        tones = ToneEngine()
        songs = SongPlayer(this)
        recorder = VoiceRecorder(this)
        menu = ParentMenuController { openParentMenu() }

        view.tapListener = PlayView.TapListener { x, y, w, h -> onBabyTap(x, y, w, h) }
        view.moveListener = PlayView.MoveListener { x, y, w, h -> onBabyDrag(x, y, w, h) }

        startPlayMode()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN ->
                menu.onDown(ev.x, ev.y, view.width, view.height)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                menu.onUp()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun onBabyTap(x: Float, y: Float, w: Int, h: Int) {
        if (mode != Mode.PLAY) return
        val note = zones.noteIndexFor(x, y, w.toFloat(), h.toFloat())
        tones.playNote(note)
        haptics.blip()
        if (egg.onTap() && recorder.hasClips()) recorder.playRandomClip()
    }

    private fun onBabyDrag(x: Float, y: Float, w: Int, h: Int) {
        if (mode != Mode.PLAY) return
        val note = zones.noteIndexFor(x, y, w.toFloat(), h.toFloat())
        if (glissando.shouldEmit(note, System.currentTimeMillis())) {
            tones.playNote(note)
        }
    }

    private fun startPlayMode() {
        mode = Mode.PLAY
        songs.stop()
        view.setGlowEnabled(true)
        screen.applyMinBrightnessAndImmersive()
        timer.start(System.currentTimeMillis())
        handler.removeCallbacks(timerTick)
        handler.postDelayed(timerTick, 1000L)
    }

    private fun startSongMode() {
        mode = Mode.SONG
        view.setGlowEnabled(false) // pure black while listening
        screen.applyMinBrightnessAndImmersive()
        songs.start()
        timer.start(System.currentTimeMillis())
        handler.removeCallbacks(timerTick)
        handler.postDelayed(timerTick, 1000L)
    }

    private fun enterStopped() {
        mode = Mode.STOPPED
        if (isRecording) {
            recorder.stopRecording()
            isRecording = false
        }
        songs.stop()
        view.setGlowEnabled(false)
        handler.removeCallbacks(timerTick)
        Toast.makeText(this, getString(R.string.timer_stopped), Toast.LENGTH_LONG).show()
    }

    private fun openParentMenu() {
        val items = arrayOf(
            getString(R.string.menu_mode_play),
            getString(R.string.menu_mode_song),
            getString(R.string.menu_record),
            getString(R.string.menu_restart_timer),
            getString(R.string.menu_exit)
        )
        // NOTE: AlertDialog shows EITHER a message OR a list in its content area,
        // not both. setMessage() would suppress the tappable items entirely, so
        // the airplane hint goes into the title instead and the items stay shown.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_title) + "\n\n" + getString(R.string.menu_airplane_hint))
            .setItems(items) { d, which ->
                when (which) {
                    0 -> startPlayMode()
                    1 -> startSongMode()
                    2 -> toggleRecording()
                    3 -> { startPlayMode() } // restart timer = re-enter play
                    4 -> { finish() }
                }
                d.dismiss()
                screen.enterImmersive()
            }
            .setCancelable(true)
            .show()
    }

    private fun toggleRecording() {
        if (!hasMicPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
            return
        }
        if (!isRecording) {
            recorder.startRecording()
            isRecording = true
            Toast.makeText(this, "录音中… 再次长按右上角停止", Toast.LENGTH_LONG).show()
        } else {
            recorder.stopRecording()
            isRecording = false
            Toast.makeText(this, "录音已保存", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasMicPermission(): Boolean =
        checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission just granted — start recording immediately so the
            // parent's first "record" tap isn't silently dropped.
            if (!isRecording) {
                recorder.startRecording()
                isRecording = true
                Toast.makeText(this, "录音中… 再次长按右上角停止", Toast.LENGTH_LONG).show()
            }
        }
        screen.enterImmersive()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Block back so the baby can't exit the app.
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) screen.enterImmersive()
    }

    override fun onPause() {
        super.onPause()
        menu.onUp()
        songs.stop()
    }

    override fun onResume() {
        super.onResume()
        // Song playback is stopped in onPause; resume it on return so the baby
        // isn't left with a silent black screen after a transient interruption.
        // Only resume if still within the 5-min cap; otherwise enter STOPPED.
        if (mode == Mode.SONG) {
            if (timer.isExpired(System.currentTimeMillis())) {
                enterStopped()
            } else {
                songs.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerTick)
        tones.release()
        songs.stop()
        recorder.release()
        screen.restoreBrightness()
    }
}
