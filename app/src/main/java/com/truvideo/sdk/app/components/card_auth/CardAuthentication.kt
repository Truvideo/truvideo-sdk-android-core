package com.truvideo.sdk.app.components.card_auth

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.components.button.TruvideoButton
import truvideo.sdk.common.model.TruvideoSdkAuthentication
import truvideo.sdk.common.model.TruvideoSdkSettings

@Composable
fun CardAuthentication(
    isAuthenticated: Boolean,
    isExpired: Boolean,
    isInitialized: Boolean,
    apiKey: String,
    authentication: TruvideoSdkAuthentication?,
    settings: TruvideoSdkSettings?,
    onButtonAuthenticationPressed: (() -> Unit)? = null,
    onButtonSettingsPressed: (() -> Unit)? = null
) {
    Column {

        Text("Authentication", Modifier.padding(start = 8.dp))
        Box(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                Row {
                    Text("Is Authenticated")
                    Box(Modifier.weight(1f))
                    Text(isAuthenticated.toString())
                }
                Row {
                    Text("Is expired")
                    Box(Modifier.weight(1f))
                    Text(isExpired.toString())
                }
                Row {
                    Text("Is initialized")
                    Box(Modifier.weight(1f))
                    Text(isInitialized.toString())
                }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize { _, _ -> }
                ) {
                    if (apiKey.trim().isNotEmpty()) {
                        Row {
                            Text("Api key")
                            Box(Modifier.weight(1f))
                            Text(apiKey)
                        }
                    }
                }

                Box(Modifier.height(16.dp))

                TruvideoButton(
                    text = "Authentication",
                    enabled = authentication != null,
                    onPressed = onButtonAuthenticationPressed
                )

                Box(Modifier.height(8.dp))

                TruvideoButton(
                    text = "Settings",
                    enabled = settings != null,
                    onPressed = onButtonSettingsPressed
                )
            }
            Box(Modifier.height(16.dp))
        }
    }
}

@Composable
@Preview
private fun Test() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var isAuthenticationExpired by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var authentication by remember { mutableStateOf<TruvideoSdkAuthentication?>(null) }
    var settings by remember { mutableStateOf<TruvideoSdkSettings?>(null) }
    var apiKey by remember { mutableStateOf("") }

    CardAuthentication(
        isAuthenticated = isAuthenticated,
        isExpired = isAuthenticationExpired,
        isInitialized = isInitialized,
        apiKey = apiKey,
        authentication = authentication,
        settings = settings
    )
}