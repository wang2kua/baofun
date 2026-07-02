package com.baofun.app.logic

/**
 * Selectable glow colors for the touch halo in play mode. Low-saturation so
 * they stay eye-safe. Pure logic (raw RGB channels, no android.graphics) so it
 * is JVM-testable. [bluish] flags colors with a bedtime caveat.
 */
enum class GlowTheme(val red: Int, val green: Int, val blue: Int, val bluish: Boolean) {
    AMBER(0xFF, 0xC4, 0x78, false),
    GREEN(0x9C, 0xCC, 0x7A, false),
    BLUE(0x7A, 0xB8, 0xCC, true),
    PINK(0xE6, 0xA8, 0xC0, false);

    fun next(): GlowTheme = values()[(ordinal + 1) % values().size]

    companion object {
        fun fromName(name: String?): GlowTheme =
            values().firstOrNull { it.name == name } ?: AMBER
    }
}
