package com.baofun.app.logic

/**
 * Hard usage cap. Caller injects the current time (System.currentTimeMillis())
 * so the logic is deterministic and JVM-testable. Default 5 minutes.
 */
class AutoStopTimer(val limitMillis: Long = 5 * 60_000L) {
    private var startedAt: Long = 0L

    fun start(nowMillis: Long) { startedAt = nowMillis }

    fun isExpired(nowMillis: Long): Boolean = nowMillis - startedAt >= limitMillis

    fun remainingMillis(nowMillis: Long): Long =
        (limitMillis - (nowMillis - startedAt)).coerceAtLeast(0L)
}
