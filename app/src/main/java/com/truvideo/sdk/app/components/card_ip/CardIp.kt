package com.truvideo.sdk.app.components.card_ip

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
import com.truvideo.sdk.components.button.TruvideoButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

@Composable
fun CardIp(
    isStarted: Boolean,
    isFetching: Boolean,
    ip: String,
    lastUpdated: Long?,
    nextUpdateAt: Long?,
    onButtonUpdatePressed: (() -> Unit)? = null,
    onButtonStartPressed: (() -> Unit)? = null,
    onButtonStopPressed: (() -> Unit)? = null
) {
    var timer by remember { mutableStateOf<Timer?>(null) }
    var nextUpdateDelta by remember { mutableStateOf<Long?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(nextUpdateAt) {
        timer?.cancel()
        nextUpdateDelta = null

        if (nextUpdateAt == null) {
            timer = null
        } else {
            timer = Timer()
            timer?.schedule(
                object : TimerTask() {
                    override fun run() {
                        nextUpdateDelta = nextUpdateAt - System.currentTimeMillis()
                    }
                },
                0,
                100,
            )
        }
    }

    Column {

        Text("IP Provider", Modifier.padding(start = 8.dp))
        Box(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                Row {
                    Text("Is started")
                    Box(Modifier.weight(1f))
                    Text(isStarted.toString())
                }

                Row {
                    Text("Is fetching")
                    Box(Modifier.weight(1f))
                    Text(isFetching.toString())
                }

                Row {
                    Text("IP")
                    Box(Modifier.weight(1f))
                    Text(ip)
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize { _, _ -> }) {
                    if (lastUpdated != null) {
                        Row {
                            Text(
                                "Updated at",
                                fontSize = 10.sp
                            )
                            Box(Modifier.weight(1f))
                            Text(
                                dateFormat.format(Date(lastUpdated)),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize { _, _ -> }) {
                    if (nextUpdateAt != null) {
                        Row {
                            Text(
                                "Next update at", fontSize = 10.sp
                            )
                            Box(Modifier.weight(1f))
                            Text(
                                "${dateFormat.format(Date(nextUpdateAt))}${if (nextUpdateDelta != null) " (in ${nextUpdateDelta}ms)" else ""}",
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Box(Modifier.height(16.dp))

                TruvideoButton(
                    text = if (isStarted) "Stop" else "Start",
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
                    text = "Update now",
                    enabled = !isFetching,
                    onPressed = onButtonUpdatePressed
                )
            }
            Box(Modifier.height(16.dp))
        }
    }
}

@Composable
@Preview
private fun Test() {
    val isStarted by remember { mutableStateOf(false) }
    val isFetching by remember { mutableStateOf(false) }
    val ip by remember { mutableStateOf("") }
    val lastUpdated by remember { mutableStateOf<Long?>(null) }
    val nextUpdateAt by remember { mutableStateOf<Long?>(null) }

    CardIp(
        isStarted = isStarted,
        isFetching = isFetching,
        ip = ip,
        lastUpdated = lastUpdated,
        nextUpdateAt = nextUpdateAt
    )
}