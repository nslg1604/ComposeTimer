package org.niaz.timerapp.diff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import org.niaz.timerapp.timer.MyWorker
import org.niaz.timerapp.timer.WorkerManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val count = MyPrefs.read(MyPrefs.PREFS_VALUE)
            MyLogger.d("BootReceiver restored=" + count)
            val inputData = workDataOf(
                WorkerManager.COUNT_KEY to count
            )

            val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
