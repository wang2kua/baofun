package com.baofun.app.logic

/**
 * Gate that returns true roughly once every [minTaps]..[maxTaps] taps, then
 * re-rolls its next target. randomFn(bound) must return an Int in [0, bound).
 * Defaults to kotlin.random; tests inject a deterministic function.
 */
class EasterEggTrigger(
    private val minTaps: Int = 8,
    private val maxTaps: Int = 10,
    private val randomFn: (Int) -> Int = { bound -> (0 until bound).random() }
) {
    private var tapCount = 0
    private var target = roll()

    private fun roll(): Int {
        val span = (maxTaps - minTaps + 1).coerceAtLeast(1)
        return minTaps + randomFn(span)
    }

    /** Call on every baby tap. Returns true when an easter egg should fire. */
    fun onTap(): Boolean {
        tapCount++
        if (tapCount >= target) {
            tapCount = 0
            target = roll()
            return true
        }
        return false
    }
}
