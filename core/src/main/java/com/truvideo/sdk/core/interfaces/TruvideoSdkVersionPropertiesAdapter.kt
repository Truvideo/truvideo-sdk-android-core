package com.truvideo.sdk.core.interfaces

internal interface TruvideoSdkVersionPropertiesAdapter {
    fun readProperty(propertyName: String): String?
}