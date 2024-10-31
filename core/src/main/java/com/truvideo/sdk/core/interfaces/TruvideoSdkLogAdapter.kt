package com.truvideo.sdk.core.interfaces

import truvideo.sdk.common.model.TruvideoSdkLogSeverity

internal interface TruvideoSdkLogAdapter {
    fun addLog(
        eventName: String,
        message: String,
        severity: TruvideoSdkLogSeverity
    )
}