package com.baofun.app.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlissandoThrottleTest {
    @Test fun emitsOnFirstMove() {
        val g = GlissandoThrottle(minIntervalMs = 80L)
        assertTrue(g.shouldEmit(noteIndex = 3, nowMillis = 0L))
    }

    @Test fun emitsWhenNoteChangesEvenIfFast() {
        val g = GlissandoThrottle(minIntervalMs = 80L)
        g.shouldEmit(noteIndex = 3, nowMillis = 0L)
        assertTrue(g.shouldEmit(noteIndex = 4, nowMillis = 10L))
    }

    @Test fun suppressesSameNoteWithinInterval() {
        val g = GlissandoThrottle(minIntervalMs = 80L)
        g.shouldEmit(noteIndex = 3, nowMillis = 0L)
        assertFalse(g.shouldEmit(noteIndex = 3, nowMillis = 40L))
    }

    @Test fun emitsSameNoteAfterInterval() {
        val g = GlissandoThrottle(minIntervalMs = 80L)
        g.shouldEmit(noteIndex = 3, nowMillis = 0L)
        assertTrue(g.shouldEmit(noteIndex = 3, nowMillis = 100L))
    }

    @Test fun resetClearsState() {
        val g = GlissandoThrottle(minIntervalMs = 80L)
        g.shouldEmit(noteIndex = 3, nowMillis = 0L)
        g.reset()
        assertTrue(g.shouldEmit(noteIndex = 3, nowMillis = 5L))
    }
}
