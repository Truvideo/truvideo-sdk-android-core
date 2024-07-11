package com.truvideo.sdk.core

import android.content.Context
import androidx.startup.Initializer

@Suppress("unused")
class TruvideoSdkInitializer : Initializer<Unit> {

    companion object {
        fun init(context: Context) {
            TruvideoSdk = TruvideoSdkImpl(
                context = context
            )
        }
    }

    override fun create(context: Context) = init(context)

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}

