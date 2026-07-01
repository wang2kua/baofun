package com.baofun.app.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeLevelsTest {
    @Test fun percentsAreLowMedHigh() {
        assertEquals(40, VolumeLevels.LOW.percent)
        assertEquals(70, VolumeLevels.MEDIUM.percent)
        assertEquals(100, VolumeLevels.HIGH.percent)
    }

    @Test fun floatMatchesPercent() {
        assertEquals(0.4f, VolumeLevels.LOW.fraction, 0.001f)
        assertEquals(1.0f, VolumeLevels.HIGH.fraction, 0.001f)
    }

    @Test fun nextCyclesLowMedHighLow() {
        assertEquals(VolumeLevels.MEDIUM, VolumeLevels.LOW.next())
        assertEquals(VolumeLevels.HIGH, VolumeLevels.MEDIUM.next())
        assertEquals(VolumeLevels.LOW, VolumeLevels.HIGH.next())
    }

    @Test fun fromNameFallsBackToMedium() {
        assertEquals(VolumeLevels.HIGH, VolumeLevels.fromName("HIGH"))
        assertEquals(VolumeLevels.MEDIUM, VolumeLevels.fromName("garbage"))
    }
}
