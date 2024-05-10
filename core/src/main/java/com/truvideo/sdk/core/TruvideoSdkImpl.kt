package com.truvideo.sdk.core

import android.content.Context
import com.truvideo.sdk.core.interfaces.TruvideoSdk
import com.truvideo.sdk.core.interfaces.TruvideoSdkAuthenticationCallback
import com.truvideo.sdk.core.interfaces.TruvideoSdkInitCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdkContextProvider
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.model.TruvideoSdkEnvironment
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkImpl(context: Context) : TruvideoSdk {

    override val isAuthenticated: Boolean
        get() = sdk_common.auth.isAuthenticated.value

    override val isAuthenticationExpired: Boolean
        get() = sdk_common.auth.isAuthenticationExpired.value

    override val apiKey: String
        get() = sdk_common.auth.apiKey.value

    override fun generatePayload(): String = sdk_common.auth.generatePayload()

    init {
        TruvideoSdkContextProvider.instance.init(context)
        sdk_common.configuration.log.printEnabled = true
        sdk_common.configuration.log.appendToFileEnabled = false
        setupEnvironment()

//        if (!sdk_common.log.isInitialized.value) {
//            sdk_common.log.initialize(LogFileUpload(context))
//        }
//
//        if (!sdk_common.ip.isStarted.value) {
//            sdk_common.ip.start()
//        }
    }

    private fun setupEnvironment() {
        sdk_common.configuration.environment = when (BuildConfig.FLAVOR) {
            "beta" -> {
                TruvideoSdkEnvironment.BETA
            }

            "rc" -> {
                TruvideoSdkEnvironment.RC
            }

            "prod" -> {
                TruvideoSdkEnvironment.PROD
            }

            else -> {
                TruvideoSdkEnvironment.PROD
            }
        }
    }

    override suspend fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
    ) {
//        if (sdk_common.log.isStarted.value) {
//            sdk_common.log.stop()
//        }

        try {
            sdk_common.auth.authenticate(
                apiKey,
                payload,
                signature
            )
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "Unknown error")
            }
        }
    }

    override fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
        callback: TruvideoSdkAuthenticationCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                authenticate(
                    apiKey = apiKey,
                    payload = payload,
                    signature = signature
                )
                callback.onReady()
            } catch (ex: Exception) {
                ex.printStackTrace()

                if (ex is TruvideoSdkException) {
                    callback.onError(ex)
                } else {
                    callback.onError(TruvideoSdkException(ex.message ?: "Unknown error"))
                }
            }
        }
    }

    override suspend fun init() {
//        if (sdk_common.log.isStarted.value) {
//            sdk_common.log.stop()
//        }

        try {
            sdk_common.auth.init()
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "Unknown error")
            }
        }

//        if (!sdk_common.log.isStarted.value) {
//            sdk_common.log.start()
//        }
    }

    override fun init(callback: TruvideoSdkInitCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                init()
                callback.onReady()
            } catch (ex: Exception) {
                ex.printStackTrace()

                if (ex is TruvideoSdkException) {
                    callback.onError(ex)
                } else {
                    callback.onError(TruvideoSdkException(ex.message ?: "Unknown error"))
                }
            }
        }
    }

    override fun clear() {
//        if (sdk_common.log.isStarted.value) {
//            sdk_common.log.stop()
//        }
        sdk_common.auth.clear()
    }

    override val environment: String
        get() = BuildConfig.FLAVOR
}