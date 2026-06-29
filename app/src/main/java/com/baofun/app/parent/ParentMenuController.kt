package com.baofun.app.parent

import android.os.Handler
import android.os.Looper

/**
 * Detects a 3-second long-press in the top-right corner to open the parent menu.
 * Babies almost never hold one fixed corner for 3s; adults do it easily one-handed.
 *
 * The Activity feeds raw DOWN/UP events plus the touch location and view size.
 * Corner = top 20% of height AND right 20% of width.
 */
class ParentMenuController(private val onTrigger: () -> Unit) {
    private val handler = Handler(Looper.getMainLooper())
    private val holdMs = 3000L
    private var armed = false

    private val fire = Runnable {
        if (armed) { armed = false; onTrigger() }
    }

    private fun inCorner(x: Float, y: Float, w: Int, h: Int): Boolean {
        if (w <= 0 || h <= 0) return false
        return x >= w * 0.8f && y <= h * 0.2f
    }

    fun onDown(x: Float, y: Float, w: Int, h: Int) {
        if (inCorner(x, y, w, h)) {
            armed = true
            handler.postDelayed(fire, holdMs)
        }
    }

    fun onUp() {
        armed = false
        handler.removeCallbacks(fire)
    }
}
