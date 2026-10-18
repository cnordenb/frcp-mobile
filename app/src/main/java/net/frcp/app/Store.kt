package net.frcp.app

import android.content.Context

/**
 * The saved settings and state, as JSON under the web version's keys and in its format, so a state can move between
 * the two.
 */
interface Store {
    var settings: String?
    var state: String?
}

/** The [Store] in the app's shared preferences, which stay on this device. */
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
