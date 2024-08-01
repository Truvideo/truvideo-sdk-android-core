package com.truvideo.sdk.core.adapters

import com.truvideo.sdk.core.interfaces.TruvideoSdkAuthAdapter
import com.truvideo.sdk.core.interfaces.TruvideoSdkLogAdapter
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkNotInitializedException
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkAuthAdapterImpl(
    private val logAdapter: TruvideoSdkLogAdapter
) : TruvideoSdkAuthAdapter {
    override fun validateAuthentication() {
        val isAuthenticated = sdk_common.auth.isAuthenticated.value
        if (!isAuthenticated) {
            logAdapter.addLog(
                eventName = "event_core_auth_validate",
                message = "Validate authentication failed: SDK not authenticated",
                severity = TruvideoSdkLogSeverity.ERROR
            )
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val isInitialized = sdk_common.auth.isInitialized.value
        if (!isInitialized) {
            logAdapter.addLog(
                eventName = "event_core_auth_validate",
                message = "Validate authentication failed: SDK not initialized",
                severity = TruvideoSdkLogSeverity.ERROR
            )
            throw TruvideoSdkNotInitializedException()
        }
    }
}