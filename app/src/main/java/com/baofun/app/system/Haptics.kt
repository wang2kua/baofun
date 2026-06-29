package com.baofun.app.system

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/** Short tactile "blip" for the cause-and-effect feedback on each tap. */
class Haptics(context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    fun blip() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(30, 60)) // 30ms, low amplitude
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(30)
        }
    }
}
