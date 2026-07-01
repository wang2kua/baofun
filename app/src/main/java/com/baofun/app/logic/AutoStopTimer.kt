package com.baofun.app.logic

/**
 * Hard usage cap. Caller injects the current time (System.currentTimeMillis())
 * so the logic is deterministic and JVM-testable. Limit is injected by the caller.
 *
 * The timer must be armed by calling [start] before it can expire. Until then
 * it is fail-safe: [isExpired] returns false and [remainingMillis] returns the
 * full [limitMillis], so a forgotten start() never reports a premature stop.
 */
class AutoStopTimer(val limitMillis: Long = 5 * 60_000L) {
    private var startedAt: Long = 0L
    private var started: Boolean = false

    /** Arms (or re-arms) the timer at the given time. Must be called to start the cap. */
    fun start(nowMillis: Long) {
        startedAt = nowMillis
        started = true
    }

    fun isExpired(nowMillis: Long): Boolean =
        started && nowMillis - startedAt >= limitMillis

    fun remainingMillis(nowMillis: Long): Long =
        if (!started) limitMillis
        else (limitMillis - (nowMillis - startedAt)).coerceAtLeast(0L)
}
