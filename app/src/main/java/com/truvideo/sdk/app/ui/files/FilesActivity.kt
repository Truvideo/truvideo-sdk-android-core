package com.truvideo.sdk.app.ui.files

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truvideo.sdk.app.ui.csv_viewer.CsvViewerActivity
import com.truvideo.sdk.app.ui.theme.TruvideoTheme
import com.truvideo.sdk.app.ui.txt_viewer.TxtViewerActivity
import com.truvideo.sdk.app.util.FileSize
import com.truvideo.sdk.components.TruvideoColors
import com.truvideo.sdk.components.scale_button.TruvideoScaleButton
import kotlinx.coroutines.launch
import truvideo.sdk.common.sdk_common
import java.io.File
import java.util.Locale

class FilesActivity : ComponentActivity() {

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


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Content(path: String) {
        val context = LocalContext.current
        val currentPath = if (path.trim().isEmpty()) sdk_common.log.directoryPath else path
        var files by remember { mutableStateOf(listOf<File>()) }
        var fetching by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        fun fetch() {
            if (fetching) return
            fetching = true
            scope.launch {
                val data = (File(currentPath).listFiles())?.toList() ?: listOf()

                val directories = data
                    .filter { it.isDirectory }
                    .sortedBy { it.path }
                    .toList()

                val nonDirectories = data
                    .filter { !it.isDirectory }
                    .sortedBy { it.path }
                    .toList()

                val result = mutableListOf<File>()
                result.addAll(directories)
                result.addAll(nonDirectories)
                files = result.toList()

                fetching = false
            }
        }

        LaunchedEffect(Unit) { fetch() }

        Column {
            TopAppBar(
                title = { Text(File(currentPath).name) },
                navigationIcon = {
                    IconButton(
                        onClick = { finish() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = ""
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { fetch() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = ""
                        )
                    }
                }
            )
            Box(Modifier.weight(1f)) {
                LazyColumn {
                    items(files) {
                        FileListItem(
                            it,
                            onPressed = {
                                if (it.isDirectory) {
                                    val newIntent = Intent(context, FilesActivity::class.java)
                                    newIntent.putExtra("path", it.path)
                                    startActivity(newIntent)
                                } else {
                                    val extension = it.extension.trim().lowercase(Locale.getDefault())
                                    when (extension) {
                                        "csv" -> {
                                            val newIntent = Intent(context, CsvViewerActivity::class.java)
                                            newIntent.putExtra("path", it.path)
                                            startActivity(newIntent)
                                        }

                                        "txt" -> {
                                            val newIntent = Intent(context, TxtViewerActivity::class.java)
                                            newIntent.putExtra("path", it.path)
                                            startActivity(newIntent)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun FileListItem(file: File, onPressed: () -> Unit) {

        val size by remember { mutableLongStateOf(file.length()) }
        val isDirectory by remember { mutableStateOf(file.isDirectory) }

        TruvideoScaleButton(
            onPressed = onPressed
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .background(TruvideoColors.gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDirectory) Icons.Outlined.Folder else Icons.Outlined.Description,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Box(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    Text(file.name, fontSize = 10.sp)
                }
                if (!file.isDirectory) {
                    Box(Modifier.width(8.dp))
                    Text(FileSize.toHuman(size), fontSize = 10.sp)
                }
            }
        }
    }

    @Composable
    @Preview
    private fun Test() {
        FileListItem(
            file = File("hola"),
            onPressed = {

            }
        )
    }
}