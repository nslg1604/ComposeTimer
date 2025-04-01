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
import kotlin.coroutines.CoroutineContext

class MyWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var timerJob: Job? = null

    override fun doWork(): Result {
        MyLogger.d("MyWorker - doWork")

        val count = inputData.getString(WorkerManager.COUNT_KEY)
        MyLogger.d("MyWorker - received count: $count")

        if (count == null){
            return Result.failure()
        }
        var currentCount = count.toInt()
        timerJob = coroutineScope.launch {
            MyLogger.d("MyWorker - doWork - isActive=" + isActive + " currentCount=" + currentCount)
            while (isActive && currentCount > 0) {
                if (WorkerManager.stop){
                    MyLogger.d("MyWorker - doWork - STOP")
                    currentCount = 0
                }
                else if (WorkerManager.running){
                    delay(1000)
                    currentCount -= 1
                }
                WorkerManager.sendTimerUpdate(currentCount)
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