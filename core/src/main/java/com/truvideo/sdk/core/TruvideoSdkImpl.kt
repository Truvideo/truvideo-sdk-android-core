package com.truvideo.sdk.core

import android.content.Context
import androidx.activity.ComponentActivity
import com.truvideo.sdk.core.exceptions.TruvideoSdkException
import com.truvideo.sdk.core.interfaces.TruvideoSdk
import com.truvideo.sdk.core.interfaces.TruvideoSdkCallback
import com.truvideo.sdk.core.usecases.PermissionUseCase
import com.truvideo.sdk.core.usecases.PickFileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdkContextProvider
import truvideo.sdk.common.model.TruvideoSdkEnvironment
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkImpl(
    context: Context,
    private val pickFileUseCase: PickFileUseCase,
    private val permissionUseCase: PermissionUseCase
) : TruvideoSdk {

    private val scope = CoroutineScope(Dispatchers.IO)

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
        @Suppress("KotlinConstantConditions")
        sdk_common.configuration.environment = when (BuildConfig.FLAVOR) {
//            "dev" -> {
//                TruvideoSdkEnvironment.DEV
//            }

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
                apiKey, payload, signature
            )
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun authenticate(
        apiKey: String, payload: String, signature: String, callback: TruvideoSdkCallback<Unit>
    ) {
        scope.launch {
            try {
                authenticate(
                    apiKey = apiKey, payload = payload, signature = signature
                )
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(
                        TruvideoSdkException(
                            exception.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    override suspend fun initAuthentication() {
//        if (sdk_common.log.isStarted.value) {
//            sdk_common.log.stop()
//        }

        try {
            sdk_common.auth.init()
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException(exception.localizedMessage ?: "Unknown error")
            }
        }

//        if (!sdk_common.log.isStarted.value) {
//            sdk_common.log.start()
//        }
    }

    override fun initAuthentication(callback: TruvideoSdkCallback<Unit>) {
        scope.launch {
            try {
                initAuthentication()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(
                        TruvideoSdkException(
                            exception.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    override fun clearAuthentication() {
//        if (sdk_common.log.isStarted.value) {
//            sdk_common.log.stop()
//        }
        sdk_common.auth.clear()
    }

    override fun initPermissionHandler(activity: ComponentActivity) =
        permissionUseCase.init(activity)

    override fun initFilePicker(activity: ComponentActivity) = pickFileUseCase.init(activity)

    override val environment: String
        get() = BuildConfig.FLAVOR
}