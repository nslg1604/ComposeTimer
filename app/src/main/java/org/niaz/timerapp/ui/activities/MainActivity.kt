package org.niaz.timerapp.ui.activities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import org.niaz.timerapp.R
import org.niaz.timerapp.diff.MyLogger
import org.niaz.timerapp.diff.MyPrefs
import org.niaz.timerapp.timer.WorkerManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyLogger.d("MainActivity restored=" + MyPrefs.read(MyPrefs.PREFS_VALUE))
        setContent {
            AskNotificationPermission()
            EditScreen(
                viewModel
            )
        }
    }


    @Composable
    fun EditScreen(viewModel: MainViewModel) {
        val timerValue by viewModel.timerValue.collectAsState()
        var count by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.enter_data),
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            EnterCount(
                count = count,
                onCountChange = { newValue -> count = newValue }
            )

            ShowInfo(viewModel, count)

            Text(
                text = "Timer: $timerValue seconds",
                fontSize = 30.sp,
                modifier = Modifier.padding(16.dp)
            )


        }
    }

    @Composable
    fun EnterCount(count: String, onCountChange: (String) -> Unit) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        OutlinedTextField(
            value = count,
            onValueChange = { newValue ->
                onCountChange(newValue)
            },
            label = {
                Text(
                    text = stringResource(R.string.enter_value)
                )
            },
            textStyle = TextStyle(
                fontSize = 24.sp,
                color = Color.Black
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .padding(8.dp),
        )
    }


    @Composable
    fun ShowInfo(
        viewModel: MainViewModel,
        count: String
    ) {
        var pauseState by remember { mutableStateOf(true) }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .height(70.dp)
                .padding(5.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable {
                        MyLogger.d("click Run")
                        viewModel.startWorker(count)
                        WorkerManager.running = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.start),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            MyLogger.d("MainActivity running=${WorkerManager.running}")

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable {
                        pauseState = !pauseState
                        WorkerManager.running = pauseState
                        MyLogger.d("MainActivity after click=${WorkerManager.running}")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = if (pauseState) R.string.pause else R.string.resume),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable {
                        viewModel.stopTimer()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.reset),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )
            }


        }
    }

    override fun onPause() {
        super.onPause()
        val currentCount = viewModel.timerValue.value
        MyPrefs.write(MyPrefs.PREFS_VALUE, currentCount)
    }

}

@Composable
fun AskNotificationPermission() {
    MyLogger.d("AskNotificationPermission - Android ver=" + Build.VERSION.SDK_INT)
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                (context as Activity),
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    LaunchedEffect(Unit) {
        MyLogger.d("AskNotificationPermission - LaunchedEffect Android ver=" + Build.VERSION.SDK_INT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    MyLogger.d("Notification permission already granted")
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    (context as Activity),
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    // user just prohibited, checkbox=false
                    showRationale = true
                    MyLogger.d("User prohibited notification permission")
                }

                else -> {
                    MyLogger.d("Ask notification permission")
                    permissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("User permissions") },
            text = { Text("Please grant notifications permission") },
            confirmButton = {
                Button(onClick = {
                    permissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    showRationale = false
                }) {
                    Text("Grant now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Later")
                }
            }
        )
    }
}

