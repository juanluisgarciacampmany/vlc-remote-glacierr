package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "pc_remote_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_IP_ADDRESS = "key_ip_address"
        private const val KEY_PORT = "key_port"
        private const val KEY_PIN = "key_pin"
        private const val KEY_LAST_PATH = "key_last_path"
    }

    var ipAddress: String
        get() = prefs.getString(KEY_IP_ADDRESS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_IP_ADDRESS, value.trim()).apply()

    var port: Int
        get() = prefs.getInt(KEY_PORT, 8000)
        set(value) = prefs.edit().putInt(KEY_PORT, value).apply()

    var pin: String
        get() = prefs.getString(KEY_PIN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PIN, value.trim()).apply()

    var lastPath: String
        get() = prefs.getString(KEY_LAST_PATH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_PATH, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
