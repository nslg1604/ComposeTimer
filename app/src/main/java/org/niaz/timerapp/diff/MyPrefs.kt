package org.niaz.timerapp.diff

import android.content.Context
import org.niaz.timerapp.MyApp


object MyPrefs {
    const val PREFS_MAIN = "PREFS_MAIN"
    const val PREFS_VALUE = "PREFS_VALUE"

    /**
     * Write to Shared Preferences
     */
    fun write(name: String?, data: Int) {
        val editor = MyApp.getInstance().getSharedPreferences(
            PREFS_MAIN, Context.MODE_PRIVATE)?.edit()
        editor?.putInt(name, data)
        editor?.apply()
    }

    /**
     * Read from Shared Preferences
     */
    fun read(name: String): Int {
        val prefs = MyApp.getInstance().getSharedPreferences(
            PREFS_MAIN, Context.MODE_PRIVATE)
        val value = prefs?.getInt(name, 0) ?: return 0
        return value
    }

}
