package com.truvideo.sdk.core.interfaces

interface TruvideoSdkSignatureProvider {
    fun generateSignature(payload: String): String
}