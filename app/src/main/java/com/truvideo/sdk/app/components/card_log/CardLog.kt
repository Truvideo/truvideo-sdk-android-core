package com.truvideo.sdk.app.components.card_log

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truvideo.sdk.app.util.FileSize
import truvideo.sdk.components.button.TruvideoButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

@Composable
fun CardLog(
    isAuthenticated: Boolean,
    isAuthenticationExpired: Boolean,
    isAuthenticationInitialized: Boolean,
    isInitialized: Boolean,
    isRunning: Boolean,
    isStarted: Boolean,
    currentBufferFileSize: Long,
    nextRunAt: Long? = null,
    onButtonStartPressed: (() -> Unit)? = null,
    onButtonStopPressed: (() -> Unit)? = null,
    onButtonSyncNowPressed: (() -> Unit)? = null,
    onButtonFilesPressed: (() -> Unit)? = null,
    onButtonRefreshPressed: (() -> Unit)? = null
) {
    var timer by remember { mutableStateOf<Timer?>(null) }
    var nextRunDelta by remember { mutableStateOf<Long?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(nextRunAt) {
        timer?.cancel()
        nextRunDelta = null

        if (nextRunAt == null) {
            timer = null
        } else {
            timer = Timer()
            timer?.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        nextRunDelta = nextRunAt - System.currentTimeMillis()
                    }
                },
                0,
                100,
            )
        }
    }

    Column {
        Text("Log System", Modifier.padding(start = 8.dp))
        Box(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                Row {
                    Text("Is initialized")
                    Box(Modifier.weight(1f))
                    Text(isInitialized.toString())
                }

                Row {
                    Text("Is started")
                    Box(Modifier.weight(1f))
                    Text(isStarted.toString())
                }

                Row {
                    Text("Is running")
                    Box(Modifier.weight(1f))
                    Text(isRunning.toString())
                }

                Row {
                    Text("Buffer file size")
                    Box(Modifier.weight(1f))
                    Text(
                        FileSize.toHuman(currentBufferFileSize),
                        fontSize = 10.sp
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize { _, _ -> }) {
                    if (nextRunAt != null) {
                        Row {
                            Text(
                                "Next run at", fontSize = 10.sp
                            )
                            Box(Modifier.weight(1f))
                            Text(
                                "${dateFormat.format(Date(nextRunAt))}${if (nextRunDelta != null) " (in ${nextRunDelta}ms)" else ""}",
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Box(Modifier.height(16.dp))

                TruvideoButton(
                    text = "Refresh",
                    onPressed = onButtonRefreshPressed
                )

                Box(Modifier.height(8.dp))

                TruvideoButton(
                    text = if (isStarted) "Stop" else "Start",
                    enabled = isInitialized && isAuthenticated && !isAuthenticationExpired && isAuthenticationInitialized,
                    onPressed = {
                        if (isStarted) {
                            onButtonStopPressed?.invoke()
                        } else {
                            onButtonStartPressed?.invoke()
                        }
                    }
                )

                Box(Modifier.height(8.dp))

                TruvideoButton(
                    text = "Sync now",
                    enabled = isInitialized && isStarted,
                    onPressed = onButtonSyncNowPressed
                )

                Box(Modifier.height(8.dp))

                TruvideoButton(
                    text = "files",
                    onPressed = onButtonFilesPressed
                )
            }
            Box(Modifier.height(16.dp))
        }
    }
}

@Composable
@Preview
private fun Test() {
    var isInitialized by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var currentBufferFileSize by remember { mutableStateOf(0L) }

    CardLog(
        isInitialized = isInitialized,
        isStarted = isStarted,
        isRunning = isRunning,
        isAuthenticated = false,
        isAuthenticationExpired = false,
        isAuthenticationInitialized = false,
        currentBufferFileSize = currentBufferFileSize
    )
}