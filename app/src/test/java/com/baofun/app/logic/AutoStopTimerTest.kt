package com.baofun.app.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoStopTimerTest {
    @Test fun notExpiredBeforeLimit() {
        val t = AutoStopTimer(limitMillis = 5 * 60_000L)
        t.start(nowMillis = 0L)
        assertFalse(t.isExpired(nowMillis = 4 * 60_000L))
    }

    @Test fun expiredAtLimit() {
        val t = AutoStopTimer(limitMillis = 5 * 60_000L)
        t.start(nowMillis = 1_000L)
        assertTrue(t.isExpired(nowMillis = 1_000L + 5 * 60_000L))
    }

    @Test fun restartResetsExpiry() {
        val t = AutoStopTimer(limitMillis = 5 * 60_000L)
        t.start(nowMillis = 0L)
        assertTrue(t.isExpired(nowMillis = 5 * 60_000L))
        t.start(nowMillis = 5 * 60_000L) // restart
        assertFalse(t.isExpired(nowMillis = 5 * 60_000L + 1_000L))
    }

    @Test fun remainingMillisCountsDown() {
        val t = AutoStopTimer(limitMillis = 5 * 60_000L)
        t.start(nowMillis = 0L)
        assertTrue(t.remainingMillis(nowMillis = 60_000L) == 4 * 60_000L)
    }
}
