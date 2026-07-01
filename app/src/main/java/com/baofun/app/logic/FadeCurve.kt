package com.baofun.app.logic

/**
 * Sleep-mode volume envelope: hold [baseVolume] until the final [fadeMillis] of
 * the [totalMillis] window, then fade linearly to 0. Pure logic (time injected).
 */
class FadeCurve(
    private val totalMillis: Long,
    private val fadeMillis: Long,
    private val baseVolume: Float = 1.0f
) {
    private val fadeStart = totalMillis - fadeMillis

    fun volumeAt(elapsedMillis: Long): Float {
        if (elapsedMillis <= fadeStart) return baseVolume
        if (elapsedMillis >= totalMillis) return 0f
        val progress = (elapsedMillis - fadeStart).toFloat() / fadeMillis
        return (baseVolume * (1f - progress)).coerceIn(0f, baseVolume)
    }

    fun isFinished(elapsedMillis: Long): Boolean = elapsedMillis >= totalMillis
}
