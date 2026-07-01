package com.baofun.app.logic

/** Three discrete volume levels used across all audio engines. Pure logic. */
enum class VolumeLevels(val percent: Int) {
    LOW(40), MEDIUM(70), HIGH(100);

    val fraction: Float get() = percent / 100f

    fun next(): VolumeLevels = when (this) {
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> LOW
    }

    companion object {
        fun fromName(name: String?): VolumeLevels =
            values().firstOrNull { it.name == name } ?: MEDIUM
    }
}
