package com.truvideo.sdk.core

import android.content.Context
import com.truvideo.sdk.core.exceptions.TruvideoSdkException
import com.truvideo.sdk.core.interfaces.TruvideoSdk
import com.truvideo.sdk.core.interfaces.TruvideoSdkCallback
import com.truvideo.sdk.core.interfaces.TruvideoSdkLogAdapter
import com.truvideo.sdk.core.interfaces.TruvideoSdkSignatureProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdkContextProvider
import truvideo.sdk.common.model.TruvideoSdkEnvironment
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkImpl(
    context: Context,
    private val logAdapter: TruvideoSdkLogAdapter
) : TruvideoSdk {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val isAuthenticated: Boolean
        get() = sdk_common.auth.isAuthenticated

    override val isAuthenticationExpired: Boolean
        get() = sdk_common.auth.isAuthenticationExpired

    override val apiKey: String
        get() = sdk_common.auth.apiKey


    init {
        TruvideoSdkContextProvider.instance.init(context)

        @Suppress("KotlinConstantConditions")
        sdk_common.configuration.log.appendToFileEnabled = BuildConfig.FLAVOR == "prod" || BuildConfig.FLAVOR == "rc"
        sdk_common.configuration.log.printEnabled = true

        logAdapter.addLog(
            eventName = "event_core_init",
            message = "Init core module. Logs: ${sdk_common.configuration.log.appendToFileEnabled}",
            severity = TruvideoSdkLogSeverity.INFO
        )

        setupEnvironment()

        if (!sdk_common.log.isInitialized.value) {
            sdk_common.log.initialize(LogFileUpload(context))
        }

        if (!sdk_common.ip.isStarted.value) {
            sdk_common.ip.start()
        }
    }

    private fun setupEnvironment() {
        logAdapter.addLog(
            eventName = "event_core_set_env",
            message = "Setting up environment ${BuildConfig.FLAVOR}",
            severity = TruvideoSdkLogSeverity.INFO
        )

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
                    callback.onError(
                        TruvideoSdkException(
                            exception.localizedMessage ?: "Unknwn error"
                        )
                    )
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
        if (sdk_common.log.isStarted.value) {
            sdk_common.log.stop()
        }

        try {
            logAdapter.addLog(
                eventName = "event_core_auth",
                message = "ApiKey: $apiKey. ExternalId: $externalId. Payload: ${payload}. Signature: $signature",
                severity = TruvideoSdkLogSeverity.INFO
            )

            sdk_common.auth.authenticate(
                apiKey = apiKey,
                payload = payload,
                signature = signature,
                externalId = externalId
            )
        } catch (exception: Exception) {
            logAdapter.addLog(
                eventName = "event_core_auth",
                message = "Authentication failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
        externalId: String?,
        callback: TruvideoSdkCallback<Unit>
    ) {
        scope.launch {
            try {
                authenticate(
                    apiKey = apiKey,
                    payload = payload,
                    signature = signature,
                    externalId = externalId
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
        if (sdk_common.log.isStarted.value) {
            sdk_common.log.stop()
        }

        try {
            logAdapter.addLog(
                eventName = "event_core_auth_init",
                message = "Initializing authentication",
                severity = TruvideoSdkLogSeverity.INFO
            )
            sdk_common.auth.init()

            if (!sdk_common.log.isStarted.value) {
                sdk_common.log.start()
            }
        } catch (exception: Exception) {
            logAdapter.addLog(
                eventName = "event_core_auth_init",
                message = "Authentication initialization failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException(exception.localizedMessage ?: "Unknown error")
            }
        }
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
        if (sdk_common.log.isStarted.value) {
            sdk_common.log.stop()
        }

        logAdapter.addLog(
            eventName = "event_core_auth_clear",
            message = "Clearing authentication",
            severity = TruvideoSdkLogSeverity.INFO
        )
        sdk_common.auth.clear()
    }

    override val environment: String
        get() = BuildConfig.FLAVOR
}