package com.truvideo.sdk.core

import android.content.Context
import com.truvideo.sdk.core.interfaces.TruvideoSdk
import com.truvideo.sdk.core.interfaces.TruvideoSdkCallback
import com.truvideo.sdk.core.interfaces.TruvideoSdkLogAdapter
import com.truvideo.sdk.core.interfaces.TruvideoSdkSignatureProvider
import com.truvideo.sdk.core.interfaces.TruvideoSdkVersionPropertiesAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdkContextProvider
import truvideo.sdk.common.exceptions.TruvideoSdkException
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import truvideo.sdk.common.sdk_common
import truvideo.sdk.common.util.TruvideoSdkCommonExceptionParser
import truvideo.sdk.common.util.parse

internal class TruvideoSdkImpl(
    context: Context,
    private val logAdapter: TruvideoSdkLogAdapter,
    versionPropertiesAdapter: TruvideoSdkVersionPropertiesAdapter
) : TruvideoSdk {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val moduleVersion = versionPropertiesAdapter.readProperty("versionName") ?: "Unknown"

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

        if (!sdk_common.log.isInitialized.value) {
            sdk_common.log.initialize(LogFileUpload(context))
        }

        if (!sdk_common.ip.isStarted.value) {
            sdk_common.ip.start()
        }
    }

    override fun isAuthenticated(): Boolean {
        try {
            logAdapter.addLog(
                eventName = "event_core_is_authenticated",
                message = "",
                severity = TruvideoSdkLogSeverity.INFO
            )

            return sdk_common.auth.isAuthenticated()
        } catch (exception: Exception) {
            logAdapter.addLog(
                eventName = "event_core_is_authenticated",
                message = "Check isAuthenticated failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()
            throw parsedException
        }
    }

    override fun isAuthenticationExpired(): Boolean {
        try {
            logAdapter.addLog(
                eventName = "event_core_is_authentication_expired",
                message = "",
                severity = TruvideoSdkLogSeverity.INFO
            )

            return sdk_common.auth.isAuthenticationExpired()
        } catch (exception: Exception) {
            logAdapter.addLog(
                eventName = "event_core_is_authentication_expired",
                message = "Check isAuthenticationExpired failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()
            throw parsedException
        }
    }

    override fun getApiKey(): String {
        try {
            logAdapter.addLog(
                eventName = "event_core_api_key",
                message = "",
                severity = TruvideoSdkLogSeverity.INFO
            )

            return sdk_common.auth.getApiKey()
        } catch (exception: Exception) {
            logAdapter.addLog(
                eventName = "event_core_api_key",
                message = "Get current api key failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()
            throw parsedException
        }
    }


    private fun getBaseUrl(): String {
        val flavor = BuildConfig.FLAVOR

        @Suppress("KotlinConstantConditions")
        return when (flavor) {
            "dev" -> "https://sdk-mobile-api-dev.truvideo.com"
            "beta" -> "https://sdk-mobile-api-beta.truvideo.com"
            "rc" -> "https://sdk-mobile-api-rc.truvideo.com"
            "prod" -> "https://sdk-mobile-api.truvideo.com"
            else -> "https://sdk-mobile-api.truvideo.com"
        }
    }

    override suspend fun handleAuthentication(
        apiKey: String,
        externalId: String,
        signatureProvider: TruvideoSdkSignatureProvider
    ) {
        if (!isAuthenticated() || isAuthenticationExpired()) {
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
        externalId: String,
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
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException())
                }
            }
        }
    }

    override fun generatePayload(): String {
        try {
            logAdapter.addLog(
                eventName = "event_core_generate_payload",
                message = "",
                severity = TruvideoSdkLogSeverity.INFO
            )

            return sdk_common.auth.generatePayload()
        } catch (exception: Exception) {
            logAdapter.addLog(
                eventName = "event_core_generate_payload",
                message = "Generate payload failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()
            throw parsedException
        }
    }

    override suspend fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
        accessTokenTTL: Long?,
        refreshTokenTTL: Long?,
        externalId: String
    ) {
        try {
            logAdapter.addLog(
                eventName = "event_core_auth",
                message = "ApiKey: $apiKey. ExternalId: $externalId. Payload: ${payload}. Signature: $signature",
                severity = TruvideoSdkLogSeverity.INFO
            )

            if (sdk_common.log.isStarted.value) {
                sdk_common.log.stop()
            }

            sdk_common.auth.authenticate(
                baseUrl = getBaseUrl(),
                apiKey = apiKey,
                payload = payload,
                signature = signature,
                externalId = externalId,
                accessTokenTTL = accessTokenTTL,
                refreshTokenTTL = refreshTokenTTL
            )
        } catch (exception: Exception) {
            logAdapter.addLog(
                eventName = "event_core_auth",
                message = "Authentication failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()
            throw parsedException
        }
    }

    override fun authenticate(
        apiKey: String,
        payload: String,
        signature: String,
        externalId: String,
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
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException())
                }
            }
        }
    }

    override suspend fun initAuthentication(
        accessTokenTTL: Long?,
        refreshTokenTTL: Long?
    ) {
        try {
            if (sdk_common.log.isStarted.value) {
                sdk_common.log.stop()
            }

            logAdapter.addLog(
                eventName = "event_core_auth_init",
                message = "Initializing authentication",
                severity = TruvideoSdkLogSeverity.INFO
            )

            sdk_common.auth.init(
                forceRefresh = true,
                accessTokenTTL = accessTokenTTL,
                refreshTokenTTL = refreshTokenTTL
            )

            if (!sdk_common.log.isStarted.value) {
                sdk_common.log.start()
            }
        } catch (exception: Exception) {
            logAdapter.addLog(
                eventName = "event_core_auth_init",
                message = "Authentication initialization failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()
            throw parsedException
        }
    }

    override fun initAuthentication(callback: TruvideoSdkCallback<Unit>) {
        scope.launch {
            try {
                initAuthentication()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException())
                }
            }
        }
    }

    override fun clearAuthentication() {
        try {
            logAdapter.addLog(
                eventName = "event_core_auth_clear",
                message = "Clearing authentication",
                severity = TruvideoSdkLogSeverity.INFO
            )

            if (sdk_common.log.isStarted.value) {
                sdk_common.log.stop()
            }

            sdk_common.auth.clear()
        } catch (exception: Exception) {

            logAdapter.addLog(
                eventName = "event_core_auth_clear",
                message = "Clearing authentication failed. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()
            throw parsedException
        }
    }

    override val environment: String
        get() = BuildConfig.FLAVOR


    override val version: String
        get() = moduleVersion
}