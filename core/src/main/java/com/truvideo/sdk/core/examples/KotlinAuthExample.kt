package com.truvideo.sdk.core.examples

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.truvideo.sdk.core.TruvideoSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KotlinAuthExample : ComponentActivity() {

    private val scope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scope.launch {
            authenticate()
        }
    }

    private suspend fun authenticate() {
        try {
            val isAuthenticated = TruvideoSdk.isAuthenticated
            val isAuthenticationExpired = TruvideoSdk.isAuthenticationExpired
            if (!isAuthenticated || isAuthenticationExpired) {
                val apiKey = "your-api-key"
                val payload = TruvideoSdk.generatePayload()
                val signature = "your-signature"
                TruvideoSdk.authenticate(
                    apiKey = apiKey,
                    payload = payload,
                    signature = signature
                )
            }

            TruvideoSdk.initAuthentication()

            // Authentication ready
            // Truvideo SDK its ready to be used
        } catch (exception: Exception) {
            exception.printStackTrace()
            // Handle error
        }
    }
}