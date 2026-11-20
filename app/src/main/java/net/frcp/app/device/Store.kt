package net.frcp.app.device

import android.content.Context

interface Store {
    var settings: String?
    var state: String?
}

class PrefsStore(context: Context) : Store {
    private val prefs = context.getSharedPreferences("frcp", Context.MODE_PRIVATE)

    override var settings: String?
        get() = prefs.getString(KEY_SETTINGS, null)
        set(value) = prefs.edit().putString(KEY_SETTINGS, value).apply()

    override var state: String?
        get() = prefs.getString(KEY_STATE, null)
        set(value) = prefs.edit().putString(KEY_STATE, value).apply()

    private companion object {
        const val KEY_SETTINGS = "frcp.settings"
        const val KEY_STATE = "frcp.state"
    }
}
