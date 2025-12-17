package com.ev.iot2.network

import android.content.Context

object TokenManager {
    private const val PREFS = "iot_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER = "jwt_user"

    private var initialized = false
    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
        initialized = true
    }

    private fun prefs() = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        if (!initialized) return
        prefs().edit().putString(KEY_TOKEN, token).apply()
    }

    fun saveUserJson(userJson: String) {
        if (!initialized) return
        prefs().edit().putString(KEY_USER, userJson).apply()
    }

    fun getUserJson(): String? {
        if (!initialized) return null
        return prefs().getString(KEY_USER, null)
    }

    fun getUserRole(): String? {
        val u = getUserJson() ?: return null
        return try {
            val obj = org.json.JSONObject(u)
            if (obj.has("role")) obj.getString("role") else null
        } catch (e: Exception) {
            null
        }
    }

    fun getToken(): String? {
        if (!initialized) return null
        return prefs().getString(KEY_TOKEN, null)
    }

    fun clear() {
        if (!initialized) return
        prefs().edit().remove(KEY_TOKEN).apply()
        prefs().edit().remove(KEY_USER).apply()
    }
}
