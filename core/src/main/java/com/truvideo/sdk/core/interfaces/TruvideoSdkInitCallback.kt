package com.truvideo.sdk.core.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkInitCallback {
    fun onReady()

    fun onError(exception: TruvideoSdkException)
}