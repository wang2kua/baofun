package com.baofun.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EasterEggTriggerTest {
    // Deterministic: no jitter, fixed period of 3 -> fires on every 3rd tap.
    private fun fixed(period: Int) = EasterEggTrigger(minTaps = period, maxTaps = period) { 0 }

    @Test fun firesOnThirdTap() {
        val t = fixed(3)
        assertFalse(t.onTap()) // 1
        assertFalse(t.onTap()) // 2
        assertTrue(t.onTap())  // 3 -> fire
    }

    @Test fun resetsAfterFiring() {
        val t = fixed(2)
        assertFalse(t.onTap())
        assertTrue(t.onTap())
        assertFalse(t.onTap())
        assertTrue(t.onTap())
    }

    @Test fun jitterStaysWithinBounds() {
        // randomFn returns max value -> period should be maxTaps (8..10 -> 10)
        val t = EasterEggTrigger(minTaps = 8, maxTaps = 10) { bound -> bound - 1 }
        var fires = 0
        repeat(10) { if (t.onTap()) fires++ }
        assertEquals(1, fires) // fires exactly once at tap 10
    }
}
