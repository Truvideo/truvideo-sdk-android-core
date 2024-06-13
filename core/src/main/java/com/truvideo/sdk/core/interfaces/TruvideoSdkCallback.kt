package com.truvideo.sdk.core.interfaces

import com.truvideo.sdk.core.exceptions.TruvideoSdkException


interface TruvideoSdkCallback<T> {

    fun onComplete(result: T)

    fun onError(exception: TruvideoSdkException)
}