package org.niaz.timerapp.diff

import android.util.Log

class MyLogger {
    companion object {
        val MAX_LOG = 2500
        val TAG = "newapp"

        fun d(str: String) {
            Log.d(TAG, str.substring(0, Math.min(str.length, MAX_LOG)))
        }

        fun v(str: String) {
            d(str)
        }

        fun e(str: String) {
            Log.e(TAG, str)
        }

        fun s(str: String) {
            Log.e(TAG, str)
        }
    }
}