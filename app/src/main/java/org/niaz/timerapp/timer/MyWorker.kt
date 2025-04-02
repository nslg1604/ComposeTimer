package org.niaz.timerapp.timer

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.niaz.timerapp.diff.TimerService
import org.niaz.timerapp.diff.MyLogger
import kotlin.coroutines.CoroutineContext

class MyWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        MyLogger.d("MyWorker - doWork")
        
        val serviceIntent = Intent(context, TimerService::class.java)
        MyLogger.d("MyWorker - doWork intent=" + serviceIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MyLogger.d("MyWorker - doWork api>=26")
            startForegroundService(context, serviceIntent)
        } else {
            MyLogger.d("MyWorker - doWork api<26")
            context.startService(serviceIntent)
        }

        return try {
            Result.success()
        } finally {
        }
    }

}