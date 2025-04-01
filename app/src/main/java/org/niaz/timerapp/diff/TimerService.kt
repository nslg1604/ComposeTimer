package org.niaz.timerapp.diff

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.niaz.timerapp.timer.WorkerManager
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {
    @Inject lateinit var myNotification: MyNotification
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)
    private var timerJob: Job? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        myNotification.createNotificationChannel()
        MyLogger.d("TimeService - onStartCommand")

        startTimer()

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        MyLogger.d("TimeService - onBind")
        TODO("Not yet implemented")
    }

    private fun startTimer() {
        var currentCount = MyPrefs.read(MyPrefs.PREFS_VALUE)

        timerJob = coroutineScope.launch {
            MyLogger.d("TimeService - startTimer - isActive=" + isActive + " currentCount=" + currentCount)
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

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


}