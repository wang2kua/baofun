package com.baofun.app.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class GlowThemeTest {
    @Test fun amberIsTheWarmDefaultRgb() {
        assertEquals(0xFF, GlowTheme.AMBER.red)
        assertEquals(0xC4, GlowTheme.AMBER.green)
        assertEquals(0x78, GlowTheme.AMBER.blue)
    }

    @Test fun fourDistinctThemes() {
        val colors = GlowTheme.values().map { Triple(it.red, it.green, it.blue) }.toSet()
        assertEquals(4, colors.size)
    }

    @Test fun nextCyclesThroughAllFour() {
        assertEquals(GlowTheme.GREEN, GlowTheme.AMBER.next())
        assertEquals(GlowTheme.BLUE, GlowTheme.GREEN.next())
        assertEquals(GlowTheme.PINK, GlowTheme.BLUE.next())
        assertEquals(GlowTheme.AMBER, GlowTheme.PINK.next())
    }

    @Test fun fromNameFallsBackToAmber() {
        assertEquals(GlowTheme.GREEN, GlowTheme.fromName("GREEN"))
        assertEquals(GlowTheme.AMBER, GlowTheme.fromName("nonsense"))
        assertEquals(GlowTheme.AMBER, GlowTheme.fromName(null))
    }

    @Test fun blueIsFlaggedAsBluish() {
        assertEquals(true, GlowTheme.BLUE.bluish)
        assertEquals(false, GlowTheme.AMBER.bluish)
    }
}
