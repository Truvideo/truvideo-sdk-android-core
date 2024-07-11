package com.truvideo.sdk.app.ui.csv_viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencsv.CSVReader
import com.truvideo.sdk.app.ui.theme.TruvideoTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileReader
import java.io.StringReader


class CsvViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent.getStringExtra("path") ?: ""

        setContent {
            TruvideoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Content(path = path)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    @Composable
    fun Content(path: String) {
        var content by remember { mutableStateOf(listOf<Array<String>>()) }
        var loading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()


        fun fetch() {
            if (loading) return
            loading = true
            scope.launch {
                val reader = CSVReader(FileReader(path))
                content = reader.readAll()
                loading = false
            }
        }

        LaunchedEffect(path) { fetch() }

        Column {
            TopAppBar(
                title = { Text(File(path).name) },
                navigationIcon = {
                    IconButton(onClick = { finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = ""
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { fetch() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = ""
                        )
                    }
                }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = content.isEmpty(),
                    label = "",
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) {
                    if (it) {
                        Box(modifier = Modifier.fillMaxSize())
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            LazyColumn {
                                items(content) {
                                    Row {
                                        it.forEachIndexed { index, it ->
                                            TableCell(
                                                text = it,
                                                index = index
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    private fun getCellWidth(index: Int): Dp {
        return when (index) {
            // SessionUID
            0 -> 200.dp
            // DeviceId
            1 -> 200.dp
            // Timestamp
            2 -> 100.dp
            // Platform
            3 -> 100.dp
            // Model
            4 -> 100.dp
            // Brand
            5 -> 100.dp
            // OS Version
            6 -> 100.dp
            // Commons Version
            7 -> 100.dp
            // Module
            8 -> 100.dp
            // Module version
            9 -> 100.dp
            // IP
            10 -> 100.dp
            // IP Last updated
            11 -> 100.dp
            // Network
            12 -> 100.dp
            // Foreground
            13 -> 100.dp
            // TAG
            14 -> 100.dp
            // Message
            15 -> 300.dp
            // Severity
            16 -> 100.dp
            else -> 200.dp
        }
    }

    @Composable
    fun TableCell(
        text: String,
        index: Int,
    ) {
        Box(
            Modifier
                .width(getCellWidth(index))
                .height(30.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                fontSize = 10.sp
            )
        }
    }


}


@Composable
@Preview
private fun Test() {
    val content = "hola,chau,\"test,asas\""
    val reader = CSVReader(StringReader(content))
    val result = reader.readAll()

    Column {
        Text(content)
        result.forEach {
            Text("Line")
            it.forEach {
                Text(it)
            }
        }
    }
}