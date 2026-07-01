package com.baofun.app.logic

/**
 * Decides when a dragging finger should trigger a new note during glissando.
 * Emit if the note index changed, or if minIntervalMs elapsed since last emit.
 * Pure logic — caller injects time — so it is JVM-testable.
 */
class GlissandoThrottle(private val minIntervalMs: Long = 80L) {
    private var lastNote = -1
    private var lastEmitMs = Long.MIN_VALUE

    fun shouldEmit(noteIndex: Int, nowMillis: Long): Boolean {
        val changed = noteIndex != lastNote
        val enoughTime = nowMillis - lastEmitMs >= minIntervalMs
        if (changed || enoughTime) {
            lastNote = noteIndex
            lastEmitMs = nowMillis
            return true
        }
        return false
    }

    fun reset() {
        lastNote = -1
        lastEmitMs = Long.MIN_VALUE
    }
}
