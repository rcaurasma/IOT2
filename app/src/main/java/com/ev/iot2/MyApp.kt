package com.ev.iot2

import android.app.Application
import com.ev.iot2.network.TokenManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
