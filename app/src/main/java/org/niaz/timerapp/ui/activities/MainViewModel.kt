package org.niaz.timerapp.ui.activities

import android.content.Context
import android.view.View
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.niaz.timerapp.R
import org.niaz.timerapp.diff.MyLogger
import org.niaz.timerapp.diff.MyNotification
import org.niaz.timerapp.diff.MyPrefs
import org.niaz.timerapp.timer.MyWorker
import org.niaz.timerapp.timer.WorkerManager
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor
    (@ApplicationContext val context: Context) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)
    private var workerId: UUID? = null

    private val _timerValue = MutableStateFlow(0)
    val timerValue: StateFlow<Int> = _timerValue.asStateFlow()
    @Inject lateinit var myNotification: MyNotification

    init {
        val finishText = context.getString(R.string.finish)
        viewModelScope.launch {
            WorkerManager.timerUpdates.collect { seconds ->
                _timerValue.value = seconds
                MyLogger.d("MainViewModel - collected seconds=" + seconds)
                if (seconds == 0){
                    myNotification.sendNotificationWithSound(
                        finishText, true
                    )
                }
            }
        }
    }

    fun startWorker(count: Int) {
        MyLogger.d("MainViewModel - startWorker count=" + count)
        myNotification.createNotificationChannel()

        MyPrefs.write(MyPrefs.PREFS_VALUE, count)
        val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
            .build()

        workerId = workRequest.id
        workManager.enqueue(workRequest)
    }

    fun stopTimer() {
        MyLogger.d("MainViewModel - stopTimer")
        workerId?.let { id ->
            workManager.cancelWorkById(id)
        }
        WorkerManager.started = false
    }

    fun initCurrentCount(){
        var currentCount = MyPrefs.read(MyPrefs.PREFS_VALUE)
        MyLogger.d("MainViewModel - initCurrentCount - currentCount=" + currentCount)
        if (currentCount > 0) {
            startWorker(currentCount)
        }

    }
}