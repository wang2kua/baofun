package com.baofun.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

/**
 * Home screen shown at launch and on returning from a mode. Dark background,
 * four large colored tiles (Play / Song / Sleep / Settings). Normal brightness —
 * this is a parent-facing chooser, not something the baby stares at. Selecting a
 * tile hands off to a full-screen mode via [listener].
 */
class DashboardView(context: Context) : LinearLayout(context) {

    interface Listener {
        fun onPlay(); fun onSong(); fun onSleep(); fun onSettings()
    }
    var listener: Listener? = null

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#1A1A1A"))
        gravity = Gravity.CENTER
        val pad = dp(24)
        setPadding(pad, pad, pad, pad)

        addTile("🎨  玩耍模式", "#E8804A") { listener?.onPlay() }
        addTile("🎵  听歌模式", "#4A9E8F") { listener?.onSong() }
        addTile("🌙  哄睡模式", "#5B6BB5") { listener?.onSleep() }
        addTile("⚙️  设置", "#6B6B6B") { listener?.onSettings() }
    }

    private fun addTile(text: String, colorHex: String, onClick: () -> Unit) {
        val b = Button(context)
        b.text = text
        b.textSize = 22f
        b.setTextColor(Color.WHITE)
        b.typeface = Typeface.DEFAULT_BOLD
        b.isAllCaps = false
        b.setBackgroundColor(Color.parseColor(colorHex))
        b.setOnClickListener { onClick() }
        val lp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(88))
        lp.setMargins(0, dp(10), 0, dp(10))
        addView(b, lp)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
