package com.baofun.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FadeCurveTest {
    private val curve = FadeCurve(totalMillis = 30 * 60_000L, fadeMillis = 30_000L, baseVolume = 1.0f)

    @Test fun fullVolumeBeforeFadeWindow() {
        assertEquals(1.0f, curve.volumeAt(0L), 0.001f)
        assertEquals(1.0f, curve.volumeAt(29 * 60_000L), 0.001f)
    }

    @Test fun halfwayThroughFadeIsHalfVolume() {
        val startFade = 30 * 60_000L - 30_000L
        assertEquals(0.5f, curve.volumeAt(startFade + 15_000L), 0.02f)
    }

    @Test fun zeroAtEnd() {
        assertEquals(0.0f, curve.volumeAt(30 * 60_000L), 0.001f)
    }

    @Test fun finishedAfterTotal() {
        assertTrue(curve.isFinished(30 * 60_000L))
        assertTrue(curve.isFinished(30 * 60_000L + 5_000L))
        assertTrue(!curve.isFinished(29 * 60_000L))
    }

    @Test fun respectsBaseVolume() {
        val c = FadeCurve(totalMillis = 1000L, fadeMillis = 1000L, baseVolume = 0.4f)
        assertEquals(0.4f, c.volumeAt(0L), 0.001f)
        assertEquals(0.2f, c.volumeAt(500L), 0.02f)
    }
}
