package com.baofun.app

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.baofun.app.audio.SleepPlayer
import com.baofun.app.audio.SongPlayer
import com.baofun.app.audio.SoundEngine
import com.baofun.app.audio.ToneEngine
import com.baofun.app.logic.AutoStopTimer
import com.baofun.app.logic.EasterEggTrigger
import com.baofun.app.logic.GlissandoThrottle
import com.baofun.app.logic.PitchZones
import com.baofun.app.parent.ParentMenuController
import com.baofun.app.settings.Settings
import com.baofun.app.system.Haptics
import com.baofun.app.system.ScreenController
import com.baofun.app.voice.VoiceRecorder

class MainActivity : Activity() {

    private enum class Mode { PLAY, SONG, SLEEP, STOPPED }

    private lateinit var view: PlayView
    private lateinit var dashboard: DashboardView
    private lateinit var screen: ScreenController
    private lateinit var haptics: Haptics
    private lateinit var tones: ToneEngine
    private lateinit var songs: SongPlayer
    private lateinit var recorder: VoiceRecorder
    private lateinit var sounds: SoundEngine
    private lateinit var sleep: SleepPlayer
    private lateinit var settings: Settings
    private lateinit var menu: ParentMenuController

    private val zones = PitchZones(3, 3)
    private val egg = EasterEggTrigger(8, 10)
    private val glissando = GlissandoThrottle(80L)
    private val timer = AutoStopTimer(10 * 60_000L)

    private val handler = Handler(Looper.getMainLooper())
    private var mode = Mode.PLAY
    private var isRecording = false

    private var sleepSongsDeadline = 0L
    private val sleepSongsTick = object : Runnable {
        override fun run() {
            if (mode == Mode.SLEEP && System.currentTimeMillis() >= sleepSongsDeadline) {
                enterStopped()
            } else if (mode == Mode.SLEEP) {
                handler.postDelayed(this, 1000L)
            }
        }
    }

    private val timerTick = object : Runnable {
        override fun run() {
            // Only the 10-min cap modes (PLAY/SONG) are governed by this tick.
            // SLEEP has its own 30-min fade timer, so guard on the active modes
            // explicitly rather than "!= STOPPED" (which would wrongly include SLEEP).
            if ((mode == Mode.PLAY || mode == Mode.SONG) &&
                timer.isExpired(System.currentTimeMillis())) {
                enterStopped()
            } else if (mode == Mode.PLAY || mode == Mode.SONG) {
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
        sounds = SoundEngine(this)
        sleep = SleepPlayer(this)
        sleep.onFinished = { enterStopped() }
        settings = Settings(this)
        menu = ParentMenuController { onLongPressCorner() }

        view.tapListener = PlayView.TapListener { x, y, w, h -> onBabyTap(x, y, w, h) }
        view.moveListener = PlayView.MoveListener { x, y, w, h -> onBabyDrag(x, y, w, h) }

        applyVolume()
        dashboard = DashboardView(this)
        dashboard.listener = object : DashboardView.Listener {
            override fun onPlay() { startPlayMode() }
            override fun onSong() { startSongMode() }
            override fun onSleep() { startSleepMode() }
            override fun onSettings() { openSettings() }
        }
        showDashboard()
    }

    private fun showDashboard() {
        mode = Mode.STOPPED
        songs.stop()
        sleep.stop()
        handler.removeCallbacks(timerTick)
        handler.removeCallbacks(sleepSongsTick)
        setContentView(dashboard)
        screen.restoreBrightness()
        screen.enterImmersive()
    }

    private fun enterModeView() {
        (view.parent as? ViewGroup)?.removeView(view)
        setContentView(view)
    }

    private fun onLongPressCorner() {
        if (mode != Mode.STOPPED) showDashboard()
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
        playPlayModeSound(note)
        haptics.blip()
        if (settings.easterEggEnabled && egg.onTap() && recorder.hasClips()) recorder.playRandomClip()
    }

    private fun playPlayModeSound(note: Int) {
        when (settings.playSound) {
            Settings.PlaySound.ANIMALS -> if (sounds.count() > 0) sounds.playSound(note) else tones.playNote(note)
            Settings.PlaySound.TONES -> tones.playNote(note)
        }
    }

    private fun applyVolume() {
        val f = settings.volume.fraction
        tones.setVolumePercent(settings.volume.percent)
        songs.setVolume(f)
        sounds.setVolume(f)
    }

    private fun onBabyDrag(x: Float, y: Float, w: Int, h: Int) {
        if (mode != Mode.PLAY) return
        val note = zones.noteIndexFor(x, y, w.toFloat(), h.toFloat())
        if (glissando.shouldEmit(note, System.currentTimeMillis())) {
            playPlayModeSound(note)
        }
    }

    private fun startPlayMode() {
        enterModeView()
        glissando.reset()
        mode = Mode.PLAY
        songs.stop()
        sleep.stop()
        handler.removeCallbacks(sleepSongsTick)
        view.setGlowEnabled(true)
        val c = settings.glowColor
        view.setGlowColor(c.red, c.green, c.blue)
        screen.applyMinBrightnessAndImmersive()
        timer.start(System.currentTimeMillis())
        handler.removeCallbacks(timerTick)
        handler.postDelayed(timerTick, 1000L)
    }

    private fun startSongMode() {
        enterModeView()
        mode = Mode.SONG
        sleep.stop()
        handler.removeCallbacks(sleepSongsTick)
        view.setGlowEnabled(false) // pure black while listening
        screen.applyMinBrightnessAndImmersive()
        songs.start()
        timer.start(System.currentTimeMillis())
        handler.removeCallbacks(timerTick)
        handler.postDelayed(timerTick, 1000L)
    }

    private fun startSleepMode() {
        enterModeView()
        mode = Mode.SLEEP
        songs.stop()
        view.setGlowEnabled(false) // pure black while sleeping
        screen.applyMinBrightnessAndImmersive()
        handler.removeCallbacks(timerTick) // sleep uses its own fade timer, not the 10-min cap
        val total = 30 * 60_000L
        val fade = 30_000L
        when (settings.sleepContent) {
            Settings.SleepContent.NOISE -> {
                sleep.setBaseVolume(settings.volume.fraction)
                sleep.start(total, fade, System.currentTimeMillis())
            }
            Settings.SleepContent.SONGS -> {
                songs.setVolume(settings.volume.fraction)
                songs.start()
                sleepSongsDeadline = System.currentTimeMillis() + total
                handler.removeCallbacks(sleepSongsTick)
                handler.postDelayed(sleepSongsTick, 1000L)
            }
        }
    }

    private fun enterStopped() {
        mode = Mode.STOPPED
        if (isRecording) {
            recorder.stopRecording()
            isRecording = false
        }
        songs.stop()
        sleep.stop()
        handler.removeCallbacks(sleepSongsTick)
        view.setGlowEnabled(false)
        handler.removeCallbacks(timerTick)
        Toast.makeText(this, getString(R.string.timer_stopped), Toast.LENGTH_LONG).show()
    }

    private fun openSettings() {
        val vol = when (settings.volume) {
            com.baofun.app.logic.VolumeLevels.LOW -> "小"
            com.baofun.app.logic.VolumeLevels.MEDIUM -> "中"
            com.baofun.app.logic.VolumeLevels.HIGH -> "大"
        }
        val sound = if (settings.playSound == Settings.PlaySound.TONES) "乐音" else "动物叫声"
        val sleepC = if (settings.sleepContent == Settings.SleepContent.NOISE) "白噪音" else "儿歌"
        val glow = when (settings.glowColor) {
            com.baofun.app.logic.GlowTheme.AMBER -> "暖橙"
            com.baofun.app.logic.GlowTheme.GREEN -> "柔绿"
            com.baofun.app.logic.GlowTheme.BLUE -> "淡蓝"
            com.baofun.app.logic.GlowTheme.PINK -> "暖粉"
        }
        val egg = if (settings.easterEggEnabled) "开" else "关"
        val items = arrayOf(
            "音量:$vol",
            "玩耍音效:$sound",
            "哄睡内容:$sleepC",
            "微光颜色:$glow",
            "爸妈录音彩蛋:$egg",
            getString(R.string.rec_title),
            getString(R.string.settings_back)
        )
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings_title))
            .setItems(items) { d, which ->
                when (which) {
                    0 -> { settings.volume = settings.volume.next(); applyVolume(); openSettings() }
                    1 -> { settings.playSound = if (settings.playSound == Settings.PlaySound.TONES)
                                Settings.PlaySound.ANIMALS else Settings.PlaySound.TONES; openSettings() }
                    2 -> { settings.sleepContent = if (settings.sleepContent == Settings.SleepContent.NOISE)
                                Settings.SleepContent.SONGS else Settings.SleepContent.NOISE; openSettings() }
                    3 -> { settings.glowColor = settings.glowColor.next(); openSettings() }
                    4 -> { settings.easterEggEnabled = !settings.easterEggEnabled; openSettings() }
                    5 -> { d.dismiss(); openRecordingManager() }
                    6 -> { d.dismiss(); showDashboard() }
                }
            }
            .setOnCancelListener { showDashboard() }
            .show()
    }

    private fun openRecordingManager() {
        val clips = recorder.listClips()
        val items = ArrayList<String>()
        items.add(if (isRecording) getString(R.string.rec_stop) else getString(R.string.rec_start))
        if (clips.isEmpty()) {
            items.add(getString(R.string.rec_none))
        } else {
            for (i in clips.indices) items.add(getString(R.string.rec_play, i + 1))
            for (i in clips.indices) items.add(getString(R.string.rec_delete, i + 1))
        }
        items.add(getString(R.string.settings_back))
        val arr = items.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.rec_title))
            .setItems(arr) { d, which ->
                val n = clips.size
                when {
                    which == 0 -> { toggleRecording(); d.dismiss(); openRecordingManager() }
                    clips.isEmpty() && which == 1 -> { d.dismiss(); openRecordingManager() }
                    which in 1..n -> { recorder.playClip(clips[which - 1]); d.dismiss(); openRecordingManager() }
                    which in (n + 1)..(2 * n) -> { recorder.deleteClip(clips[which - 1 - n]); d.dismiss(); openRecordingManager() }
                    else -> { d.dismiss(); openSettings() }
                }
            }
            .setOnCancelListener { openSettings() }
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
        sleep.stop()
    }

    override fun onResume() {
        super.onResume()
        if (mode == Mode.SONG) {
            if (timer.isExpired(System.currentTimeMillis())) enterStopped() else songs.start()
        } else if (mode == Mode.SLEEP) {
            startSleepMode() // restart sleep cleanly on return (fade timer resets)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerTick)
        handler.removeCallbacks(sleepSongsTick)
        tones.release()
        songs.stop()
        recorder.release()
        sounds.release()
        sleep.stop()
        screen.restoreBrightness()
    }
}
