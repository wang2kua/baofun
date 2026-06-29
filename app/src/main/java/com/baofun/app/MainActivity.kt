package com.baofun.app

import android.app.Activity
import android.os.Bundle
import android.view.View

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = View(this)
        v.setBackgroundColor(0xFF000000.toInt())
        setContentView(v)
    }
}
