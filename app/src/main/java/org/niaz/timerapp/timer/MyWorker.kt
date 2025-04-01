package org.niaz.timerapp.timer

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.niaz.timerapp.diff.MyLogger
import org.niaz.timerapp.diff.MyPrefs
import kotlin.coroutines.CoroutineContext

class MyWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var timerJob: Job? = null

    override fun doWork(): Result {
        var currentCount = MyPrefs.read(MyPrefs.PREFS_VALUE)
        MyLogger.d("MyWorker - doWork currentCount=" + currentCount)
        if (currentCount > 0){
            WorkerManager.started = true
            WorkerManager.running = true
        }

        timerJob = coroutineScope.launch {
            MyLogger.d("MyWorker - doWork - isActive=" + isActive + " currentCount=" + currentCount)
            while (isActive && currentCount > 0) {
                if (!WorkerManager.started){
                    MyLogger.d("MyWorker - doWork - STOP")
                    currentCount = 0
                    WorkerManager.sendTimerUpdate(currentCount)
                    MyPrefs.write(MyPrefs.PREFS_VALUE, currentCount)
                }
                else if (WorkerManager.running){
                    delay(1000)
                    currentCount -= 1
                    WorkerManager.sendTimerUpdate(currentCount)
                    MyPrefs.write(MyPrefs.PREFS_VALUE, currentCount)
                }
            }
        }

        return try {
            Result.success()
        } finally {
            job.cancel()
        }
    }

    override fun onStopped() {
        super.onStopped()
        timerJob?.cancel()
        job.cancel()
    }
}