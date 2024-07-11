package com.truvideo.sdk.core

import android.content.Context
import android.util.Log
import com.truvideo.sdk.core.exceptions.TruvideoSdkException
import com.truvideo.sdk.core.interfaces.TruvideoSdk
import com.truvideo.sdk.core.interfaces.TruvideoSdkCallback
import com.truvideo.sdk.core.interfaces.TruvideoSdkSignatureProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdkContextProvider
import truvideo.sdk.common.model.TruvideoSdkEnvironment
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkImpl(
    context: Context,
) : TruvideoSdk {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val isAuthenticated: Boolean
        get() = sdk_common.auth.isAuthenticated.value

    override val isAuthenticationExpired: Boolean
        get() = sdk_common.auth.isAuthenticationExpired.value

    override val apiKey: String
        get() = sdk_common.auth.apiKey.value


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
        Log.d("TruvideoSdkCode", "Environment ${BuildConfig.FLAVOR}")

        @Suppress("KotlinConstantConditions")
        sdk_common.configuration.environment = when (BuildConfig.FLAVOR) {
            "dev" -> {
                TruvideoSdkEnvironment.DEV
            }

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

    override suspend fun handleAuthentication(
        apiKey: String,
        externalId: String?,
        signatureProvider: TruvideoSdkSignatureProvider
    ) {
        if (!isAuthenticated || isAuthenticationExpired) {
            val payload = generatePayload()
            authenticate(
                apiKey = apiKey,
                payload = payload,
                externalId = externalId,
                signature = signatureProvider.generateSignature(payload)
            )
        }

        initAuthentication()
    }

    override suspend fun handleAuthentication(
        apiKey: String,
        externalId: String?,
        signatureProvider: TruvideoSdkSignatureProvider,
        callback: TruvideoSdkCallback<Unit>
    ) {
        scope.launch {
            try {
                handleAuthentication(
                    apiKey = apiKey,
                    externalId = externalId,
                    signatureProvider = signatureProvider
                )
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException(exception.localizedMessage ?: "Unknwn error"))
                }
            }
        }
    }

    override fun generatePayload(): String = sdk_common.auth.generatePayload()

    override suspend fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
        externalId: String?
    ) {
//        if (sdk_common.log.isStarted.value) {
//            sdk_common.log.stop()
//        }

        try {
            sdk_common.auth.authenticate(
                apiKey = apiKey,
                payload = payload,
                signature = signature,
                externalId = externalId
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
        apiKey: String, payload: String, signature: String, externalId: String?, callback: TruvideoSdkCallback<Unit>
    ) {
        scope.launch {
            try {
                authenticate(
                    apiKey = apiKey, payload = payload, signature = signature, externalId = externalId
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

    override val environment: String
        get() = BuildConfig.FLAVOR
}