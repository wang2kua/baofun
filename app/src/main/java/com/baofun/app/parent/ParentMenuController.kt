package com.baofun.app.parent

import android.os.Handler
import android.os.Looper

/**
 * Detects a 3-second long-press in the top-right corner to open the parent menu.
 * Babies almost never hold one fixed corner for 3s; adults do it easily one-handed.
 *
 * The Activity feeds raw DOWN/UP events plus the touch location and view size.
 * Corner = top 20% of height AND right 20% of width.
 *
 * Note: there is no move tracking, so a finger that starts in the corner and
 * drifts out while held still triggers after 3s. The Activity must map
 * ACTION_CANCEL to onUp() and also call onUp() on lifecycle pause to drain any
 * pending trigger.
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
            handler.removeCallbacks(fire) // make re-arm idempotent (multi-touch safe)
            armed = true
            handler.postDelayed(fire, holdMs)
        }
    }

    fun onUp() {
        armed = false
        handler.removeCallbacks(fire)
    }
}
