package com.truvideo.sdk.core.interfaces

import truvideo.sdk.common.exceptions.TruvideoSdkException


interface TruvideoSdkCallback<T> {

    fun onComplete(result: T)

    fun onError(exception: TruvideoSdkException)
}