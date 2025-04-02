package org.niaz.timerapp.diff

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.niaz.timerapp.timer.WorkerManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.niaz.timerapp.R

class TimerService : Service() {
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)
    private var timerJob: Job? = null

    companion object {
        private const val FOREGROUND_CHANNEL_ID = "ServiceChannel"
        private const val NOTIFICATION_CHANNEL_ID = "NotificationChannel"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val REGULAR_NOTIFICATION_ID = 2
    }

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        MyLogger.d("TimerService - onCreate")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MyLogger.d("TimerService - onStartCommand")
        startForegroundService()

        startTimer()

        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = buildNotification(
            channelId = FOREGROUND_CHANNEL_ID,
            title = "",
            content = getString(R.string.service_started)
        )
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    fun sendNotification(title: String, content: String) {
        val notification = buildNotification(
            channelId = NOTIFICATION_CHANNEL_ID,
            title = title,
            content = content,
            priority = NotificationCompat.PRIORITY_HIGH
        )
        notificationManager.notify(REGULAR_NOTIFICATION_ID, notification)
    }

    private fun buildNotification(
        channelId: String,
        title: String,
        content: String,
        priority: Int = NotificationCompat.PRIORITY_LOW
    ): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.baseline_av_timer_24)
            .setPriority(priority)
            .setAutoCancel(true)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service notification channel"
            }

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Regular Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Regular notification channel"
            }

            notificationManager.createNotificationChannels(
                listOf(serviceChannel, notificationChannel)
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTimer() {
        var currentCount = MyPrefs.read(MyPrefs.PREFS_VALUE)
        MyLogger.d("TimerService - startTimer currentCount=" + currentCount)

        timerJob = coroutineScope.launch {
            MyLogger.d("TimeService - startTimer - isActive=" + isActive + " currentCount=" + currentCount)
            WorkerManager.started = true
            WorkerManager.running = true
            while (isActive && currentCount > 0) {
                MyLogger.d("TimerService - doWork - started=" + WorkerManager.started + " running=" + WorkerManager.running)
                if (!WorkerManager.started) {
                    MyLogger.d("TimerService - doWork - STOP")
                    currentCount = 0
                    WorkerManager.sendTimerUpdate(currentCount)
                    MyPrefs.write(MyPrefs.PREFS_VALUE, currentCount)
                    sendNotification("", getString(R.string.finish))
                } else if (WorkerManager.running) {
                    delay(1000)
                    currentCount -= 1
                    WorkerManager.sendTimerUpdate(currentCount)
                    MyPrefs.write(MyPrefs.PREFS_VALUE, currentCount)
                    if (currentCount <= 0){
                        MyLogger.d("TimerService - doWork - currentCount=0")
                        WorkerManager.started = false
                        sendNotification("", getString(R.string.finish_timer))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


}

