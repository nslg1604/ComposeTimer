package org.niaz.timerapp.diff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.startForegroundService
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import org.niaz.timerapp.timer.MyWorker
import org.niaz.timerapp.timer.WorkerManager
import org.niaz.timerapp.ui.activities.MainActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
//            val launchIntent = Intent(context, MainActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            context.startActivity(launchIntent)

            var currentCount = MyPrefs.read(MyPrefs.PREFS_VALUE)
            MyLogger.d("BootReceiver - android=" + Build.VERSION.SDK_INT + " currentCount=$currentCount")
            if (currentCount > 0) {
                val serviceIntent = Intent(context, TimerService::class.java)
                MyLogger.d("BootReceiver - intent=" + serviceIntent)
                if (Build.VERSION.SDK_INT <= 33) { // <= Android 13
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        MyLogger.d("BootReceiver - api>=26")
                        startForegroundService(context, serviceIntent)
                    } else {
                        MyLogger.d("BootReceiver - api<26")
                        context.startService(serviceIntent)
                    }
                }
                // Android 14+
                else {
                    MyLogger.d("MyWorker - BootReceiver Android 14+")
                    val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
                        .build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                }
            }
        }
    }
}
