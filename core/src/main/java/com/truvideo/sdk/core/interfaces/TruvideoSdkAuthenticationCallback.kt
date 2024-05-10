package com.truvideo.sdk.core.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkAuthenticationCallback {
    fun onReady()

    fun onError(exception: TruvideoSdkException)
}