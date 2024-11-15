package com.truvideo.sdk.app.ui.home

//import com.truvideo.sdk.core.usecases.TruvideoSdkFilePicker
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.app.components.card_auth.CardAuthentication
import com.truvideo.sdk.app.ui.theme.TruvideoTheme
import com.truvideo.sdk.core.TruvideoSdk
import kotlinx.coroutines.launch
import truvideo.sdk.common.model.TruvideoSdkAuthentication
import truvideo.sdk.common.model.TruvideoSdkLog
import truvideo.sdk.common.model.TruvideoSdkLogModule
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import truvideo.sdk.common.model.TruvideoSdkSettings
import truvideo.sdk.common.sdk_common
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TruvideoTheme {
                Content()
            }
        }
    }


    @Composable
    fun Content() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var apiKey by remember { mutableStateOf("VS2SG9WK") }
        var secret by remember { mutableStateOf("ST2K33GR") }
        var accessTokenTTL by remember { mutableStateOf("") }
        var refreshTokenTTL by remember { mutableStateOf("") }
        var isAuthenticating by remember { mutableStateOf(false) }
        var isAuthenticated by remember { mutableStateOf(false) }
        var isAuthenticatedInitialized by remember { mutableStateOf(false) }
        var isAuthenticationExpired by remember { mutableStateOf(false) }
        var currentApiKey by remember { mutableStateOf("") }
        var authentication by remember { mutableStateOf<TruvideoSdkAuthentication?>(null) }
        var settings by remember { mutableStateOf<TruvideoSdkSettings?>(null) }

//        val logIsInitialized by sdk_common.log.isInitialized.collectAsState()
//        val logIsRunning by sdk_common.log.isRunning.collectAsState()
//        val logIsStarted by sdk_common.log.isStarted.collectAsState()
//        var logCurrentBufferFileSize by remember { mutableLongStateOf(sdk_common.log.currentBufferFileSize) }
//        val logNextRunAt by sdk_common.log.nextRunAt.collectAsState()
//
//        val ipIsStarted by sdk_common.ip.isStarted.collectAsState()
//        val ipIsFetching by sdk_common.ip.isFetching.collectAsState()
//        val ip by sdk_common.ip.ip.collectAsState()
//        val ipLastUpdated by sdk_common.ip.lastUpdated.collectAsState()
//        val ipNextUpdateAt by sdk_common.ip.nextUpdateAt.collectAsState()

        fun refreshAuth() {
            isAuthenticated = sdk_common.auth.isAuthenticated()
            isAuthenticationExpired = sdk_common.auth.isAuthenticationExpired()
            isAuthenticatedInitialized = sdk_common.auth.isInitialized
            authentication = sdk_common.auth.getAuthentication()
            settings = sdk_common.auth.getSettings()
            currentApiKey = sdk_common.auth.getApiKey()
        }

        fun showAuthError(message: String) {
            AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Accept") { _, _ ->

                }
                .show()
        }

        fun authenticate() {
            isAuthenticating = true

            scope.launch {
                try {
                    if (!TruvideoSdk.isAuthenticated() || TruvideoSdk.isAuthenticationExpired()) {
                        val payload = TruvideoSdk.generatePayload()
                        val signature = generateSignature(payload, secret)
                        TruvideoSdk.authenticate(
                            apiKey = apiKey,
                            payload = payload,
                            signature = signature
                        )
                    }

                    TruvideoSdk.initAuthentication()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    showAuthError(exception.message ?: "Unknown error")
                } finally {
                    isAuthenticating = false
                    refreshAuth()
                }
            }
        }


        fun clear() {
            TruvideoSdk.clearAuthentication()
            refreshAuth()
        }

        fun refreshLog() {
//            logCurrentBufferFileSize = sdk_common.log.currentBufferFileSize
        }

        fun addLogs(count: Int) {
            val logs = mutableListOf<TruvideoSdkLog>()
            for (i in 0..count) {
                logs.add(
                    TruvideoSdkLog(
                        tag = "test",
                        message = "Log nº $i",
                        severity = TruvideoSdkLogSeverity.DEBUG,
                        module = TruvideoSdkLogModule.CORE,
                        moduleVersion = "0.0.1"
                    )
                )
            }

            sdk_common.log.addMultiple(logs)
        }

        LaunchedEffect(Unit) {
            refreshAuth()
            if (isAuthenticated) {
                authenticate()
            }
        }

        Scaffold { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    TextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Api Key") }
                    )
                    Box(modifier = Modifier.height(16.dp))
                    TextField(
                        value = secret,
                        onValueChange = { secret = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Secret") }
                    )
                    Box(modifier = Modifier.height(16.dp))
                    TextField(
                        value = accessTokenTTL,
                        onValueChange = { accessTokenTTL = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Access Token TTL") }
                    )
                    Box(modifier = Modifier.height(16.dp))
                    TextField(
                        value = refreshTokenTTL,
                        onValueChange = { refreshTokenTTL = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Refresh Token TTL") }
                    )
                    Box(modifier = Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            apiKey = "VS2SG9WK"
                            secret = "ST2K33GR"
                        }
                    ) {
                        Text("Use default")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = (!isAuthenticated || isAuthenticationExpired) && apiKey.isNotEmpty() && secret.isNotEmpty() && !isAuthenticating,
                        onClick = { authenticate() }
                    ) {
                        Text("Authenticate")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { refreshLog() }
                    ) {
                        Text("Refresh authentication")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isAuthenticated && !isAuthenticating,
                        onClick = { clear() }
                    ) {
                        Text("Clear")
                    }
//                    Box(modifier = Modifier.height(8.dp))
//                    TruvideoButton(
//                        text = "Add 1 logs",
//                        onPressed = { addLogs(1) }
//                    )
//                    Box(modifier = Modifier.height(8.dp))
//                    TruvideoButton(
//                        text = "Add 10 logs",
//                        onPressed = { addLogs(10) }
//                    )
//                    Box(modifier = Modifier.height(8.dp))
//                    TruvideoButton(
//                        text = "Add 100 logs",
//                        onPressed = { addLogs(100) }
//                    )
//                    Box(modifier = Modifier.height(8.dp))
//                    TruvideoButton(
//                        text = "Add 1000 logs",
//                        onPressed = { addLogs(1000) }
//                    )
//                    Box(modifier = Modifier.height(8.dp))
//                    TruvideoButton(
//                        text = "Add 10000 logs",
//                        onPressed = { addLogs(10000) }
//                    )
                    Box(Modifier.padding(top = 32.dp))

                    CardAuthentication(
                        isAuthenticated = isAuthenticated,
                        isExpired = isAuthenticationExpired,
                        isInitialized = isAuthenticatedInitialized,
                        apiKey = currentApiKey,
                        authentication = authentication,
                        settings = settings
                    )

                    Box(Modifier.padding(top = 16.dp))

//                CardLog(
//                    isAuthenticated = isAuthenticated,
//                    isAuthenticationExpired = isAuthenticationExpired,
//                    isAuthenticationInitialized = isAuthenticatedInitialized,
//                    isInitialized = logIsInitialized,
//                    isStarted = logIsStarted,
//                    isRunning = logIsRunning,
//                    nextRunAt = logNextRunAt,
//                    currentBufferFileSize = logCurrentBufferFileSize,
//                    onButtonStartPressed = { sdk_common.log.start() },
//                    onButtonStopPressed = { sdk_common.log.stop() },
//                    onButtonSyncNowPressed = { sdk_common.log.sync() },
//                    onButtonFilesPressed = {
//                        val intent = Intent(context, FilesActivity::class.java)
//                        startActivity(intent)
//                    },
//                    onButtonRefreshPressed = { refreshLog() }
//                )
//
//                Box(Modifier.padding(top = 16.dp))
//
//                CardIp(
//                    isStarted = ipIsStarted,
//                    isFetching = ipIsFetching,
//                    ip = ip,
//                    lastUpdated = ipLastUpdated,
//                    nextUpdateAt = ipNextUpdateAt,
//                    onButtonUpdatePressed = { sdk_common.ip.fetch() },
//                    onButtonStartPressed = { sdk_common.ip.start() },
//                    onButtonStopPressed = { sdk_common.ip.stop() }
//                )
                }
            }
        }
    }

    @Preview
    @Composable
    private fun Test() {
        TruvideoTheme {
            Content()
        }
    }
}


private fun generateSignature(payload: String, secret: String): String {
    val keyBytes: ByteArray = secret.toByteArray(StandardCharsets.UTF_8)
    val messageBytes: ByteArray = payload.toByteArray(StandardCharsets.UTF_8)

    try {
        val hmacSha256 = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(keyBytes, "HmacSHA256")
        hmacSha256.init(secretKeySpec)
        val signatureBytes = hmacSha256.doFinal(messageBytes)
        return bytesToHex(signatureBytes)
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: InvalidKeyException) {
        e.printStackTrace()
    }
    return ""
}

private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = "0123456789abcdef"
    val hex = StringBuilder(bytes.size * 2)
    for (i in bytes.indices) {
        val value = bytes[i].toInt() and 0xFF
        hex.append(hexChars[value shr 4])
        hex.append(hexChars[value and 0x0F])
    }
    return hex.toString()
}