package com.baofun.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PitchZonesTest {
    private val zones = PitchZones(columns = 3, rows = 3) // 9 zones, 9 notes

    @Test fun topLeftIsZoneZero() {
        assertEquals(0, zones.noteIndexFor(x = 10f, y = 10f, width = 300f, height = 300f))
    }

    @Test fun bottomRightIsLastZone() {
        assertEquals(8, zones.noteIndexFor(x = 299f, y = 299f, width = 300f, height = 300f))
    }

    @Test fun centerIsMiddleZone() {
        assertEquals(4, zones.noteIndexFor(x = 150f, y = 150f, width = 300f, height = 300f))
    }

    @Test fun clampsOutOfBoundsToValidRange() {
        // x clamps to col 0, y clamps to row 2 -> index 6
        assertEquals(6, zones.noteIndexFor(x = -50f, y = 9999f, width = 300f, height = 300f))
    }

    @Test fun xAtExactWidthMapsToLastColumn() {
        assertEquals(2, zones.noteIndexFor(x = 300f, y = 10f, width = 300f, height = 300f))
    }

    @Test fun zeroSizeDoesNotCrash() {
        assertEquals(0, zones.noteIndexFor(x = 5f, y = 5f, width = 0f, height = 0f))
    }

    @Test fun rejectsZeroColumns() {
        assertThrows(IllegalArgumentException::class.java) {
            PitchZones(columns = 0, rows = 3)
        }
    }
}
