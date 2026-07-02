package com.baofun.app.settings

import android.content.Context
import com.baofun.app.logic.GlowTheme
import com.baofun.app.logic.VolumeLevels

/**
 * Single place for persisted user settings (volume, play-mode sound choice,
 * sleep-content choice). Backed by SharedPreferences.
 */
class Settings(context: Context) {
    enum class PlaySound { TONES, ANIMALS }
    enum class SleepContent { NOISE, SONGS }

    private val prefs = context.getSharedPreferences("baofun", Context.MODE_PRIVATE)

    var volume: VolumeLevels
        get() = VolumeLevels.fromName(prefs.getString("volume", null))
        set(v) { prefs.edit().putString("volume", v.name).apply() }

    var playSound: PlaySound
        get() = runCatching { PlaySound.valueOf(prefs.getString("playSound", "") ?: "") }
            .getOrDefault(PlaySound.TONES)
        set(v) { prefs.edit().putString("playSound", v.name).apply() }

    var sleepContent: SleepContent
        get() = runCatching { SleepContent.valueOf(prefs.getString("sleepContent", "") ?: "") }
            .getOrDefault(SleepContent.NOISE)
        set(v) { prefs.edit().putString("sleepContent", v.name).apply() }

    var glowColor: GlowTheme
        get() = GlowTheme.fromName(prefs.getString("glowColor", null))
        set(v) { prefs.edit().putString("glowColor", v.name).apply() }

    var easterEggEnabled: Boolean
        get() = prefs.getBoolean("easterEggEnabled", true)
        set(v) { prefs.edit().putBoolean("easterEggEnabled", v).apply() }
}
