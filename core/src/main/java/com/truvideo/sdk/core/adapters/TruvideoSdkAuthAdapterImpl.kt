package com.truvideo.sdk.core.adapters

import com.truvideo.sdk.core.interfaces.TruvideoSdkAuthAdapter
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkNotInitializedException
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkAuthAdapterImpl : TruvideoSdkAuthAdapter {
    override fun validateAuthentication() {
        val isAuthenticated = sdk_common.auth.isAuthenticated.value
        if (!isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val isInitialized = sdk_common.auth.isInitialized.value
        if (!isInitialized) {
            throw TruvideoSdkNotInitializedException()
        }
    }
}