package com.baofun.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.MotionEvent
import android.view.View

/**
 * Full-screen black view. On each touch it spawns a transient warm, low-saturation,
 * feathered glow at the touch point that fades over ~2s, and reports the tap via
 * [onTap] so the Activity can play a pitched tone + haptic + maybe an easter egg.
 *
 * Eye-safety: glow is a soft radial gradient (feathered edge, not a hard bright dot),
 * warm amber, low max alpha. Listening mode renders pure black (setGlowEnabled(false)).
 */
class PlayView(context: Context) : View(context) {

    fun interface TapListener { fun onTap(x: Float, y: Float, width: Int, height: Int) }
    fun interface MoveListener { fun onMove(x: Float, y: Float, width: Int, height: Int) }

    private class Glow(val x: Float, val y: Float, var bornAt: Long)

    private val glows = ArrayList<Glow>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fadeMs = 2000L
    private val maxAlpha = 90 // out of 255 — dim
    private val warm = Color.rgb(255, 196, 120) // low-saturation amber
    private var glowEnabled = true
    var tapListener: TapListener? = null
    var moveListener: MoveListener? = null

    fun setGlowEnabled(enabled: Boolean) {
        glowEnabled = enabled
        if (!enabled) glows.clear()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN ||
            event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            val idx = event.actionIndex
            val x = event.getX(idx)
            val y = event.getY(idx)
            if (glowEnabled) {
                glows.add(Glow(x, y, System.currentTimeMillis()))
                invalidate()
            }
            tapListener?.onTap(x, y, width, height)
            return true
        }
        if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            val idx = event.actionIndex
            val x = event.getX(idx)
            val y = event.getY(idx)
            if (glowEnabled) {
                glows.add(Glow(x, y, System.currentTimeMillis()))
                invalidate()
            }
            moveListener?.onMove(x, y, width, height)
            return true
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        if (!glowEnabled) return
        val now = System.currentTimeMillis()
        val it = glows.iterator()
        var anyAlive = false
        while (it.hasNext()) {
            val g = it.next()
            val age = now - g.bornAt
            if (age >= fadeMs) { it.remove(); continue }
            anyAlive = true
            val frac = 1f - age.toFloat() / fadeMs
            val alpha = (maxAlpha * frac).toInt().coerceIn(0, 255)
            val radius = 140f
            val warmA = Color.argb(alpha, Color.red(warm), Color.green(warm), Color.blue(warm))
            paint.shader = RadialGradient(
                g.x, g.y, radius,
                intArrayOf(warmA, Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(g.x, g.y, radius, paint)
        }
        paint.shader = null
        if (anyAlive) postInvalidateOnAnimation()
    }
}
