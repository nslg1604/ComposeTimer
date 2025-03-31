package org.niaz.timerapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.niaz.timerapp.diff.MyLogger

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        myApp = this.applicationContext as MyApp
        MyLogger.d("MyApp - onCreate")
//        setUncaughtException()
    }

    companion object {
        lateinit var myApp: MyApp
        fun getInstance(): MyApp {
            return myApp
        }
    }
}