package org.niaz.timerapp.timer

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.niaz.timerapp.diff.MyLogger

object WorkerManager {
    @Volatile var started = false
    @Volatile var running = false
    private val _timerUpdates = MutableSharedFlow<Int>()
    val timerUpdates: SharedFlow<Int> = _timerUpdates

    suspend fun sendTimerUpdate(seconds: Int) {
        MyLogger.d("WorkerManager - sendTimerUpdate seconds=" + seconds)
        _timerUpdates.emit(seconds)
    }
}