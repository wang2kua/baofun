package com.baofun.app.system

import android.app.Activity
import android.view.View
import android.view.WindowManager

/**
 * Eye-safety + anti-mistouch window controls:
 *  - force per-window brightness to the minimum visible level
 *  - keep the screen on (no auto-dim-to-off mid-play)
 *  - immersive sticky fullscreen so status/nav bars stay hidden from baby swipes
 */
class ScreenController(private val activity: Activity) {

    fun applyMinBrightnessAndImmersive() {
        val lp = activity.window.attributes
        lp.screenBrightness = 0.01f // lowest non-zero: dimmest backlight
        activity.window.attributes = lp
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterImmersive()
    }

    fun enterImmersive() {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
    }

    fun restoreBrightness() {
        val lp = activity.window.attributes
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = lp
    }
}
