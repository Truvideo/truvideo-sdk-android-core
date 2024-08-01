package com.truvideo.sdk.core.adapters

import com.truvideo.sdk.core.interfaces.TruvideoSdkVersionPropertiesAdapter
import com.truvideo.sdk.core.interfaces.TruvideoSdkLogAdapter
import truvideo.sdk.common.model.TruvideoSdkLog
import truvideo.sdk.common.model.TruvideoSdkLogModule
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkLogAdapterImpl(
    versionPropertiesAdapter: TruvideoSdkVersionPropertiesAdapter
) : TruvideoSdkLogAdapter {

    private val moduleVersion = versionPropertiesAdapter.readProperty("versionName") ?: "Unknown"

    override fun addLog(eventName: String, message: String, severity: TruvideoSdkLogSeverity) {
        sdk_common.log.add(
            TruvideoSdkLog(
                tag = eventName,
                message = message,
                severity = severity,
                module = TruvideoSdkLogModule.CORE,
                moduleVersion = moduleVersion,
            )
        )
    }
}