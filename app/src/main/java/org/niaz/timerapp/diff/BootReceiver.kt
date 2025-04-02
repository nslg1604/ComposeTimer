package org.niaz.timerapp.diff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import org.niaz.timerapp.timer.MyWorker
import org.niaz.timerapp.timer.WorkerManager
import org.niaz.timerapp.ui.activities.MainActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)

            var currentCount = MyPrefs.read(MyPrefs.PREFS_VALUE)
            MyLogger.d("BootReceiver - currentCount=$currentCount")
            if (currentCount > 0) {
                val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}
