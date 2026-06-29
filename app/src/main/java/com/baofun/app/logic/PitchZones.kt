package com.baofun.app.logic

/**
 * Splits the screen into a columns x rows grid; each cell is one note.
 * Note index increases left-to-right, top-to-bottom (0 .. columns*rows-1).
 * Pure logic — no Android types — so it is JVM unit-testable.
 */
class PitchZones(val columns: Int = 3, val rows: Int = 3) {
    val noteCount: Int = columns * rows

    fun noteIndexFor(x: Float, y: Float, width: Float, height: Float): Int {
        if (width <= 0f || height <= 0f) return 0
        val col = ((x / width) * columns).toInt().coerceIn(0, columns - 1)
        val row = ((y / height) * rows).toInt().coerceIn(0, rows - 1)
        return row * columns + col
    }
}
