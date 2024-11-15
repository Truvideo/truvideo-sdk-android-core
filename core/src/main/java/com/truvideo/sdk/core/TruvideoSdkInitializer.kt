package com.truvideo.sdk.core

import android.content.Context
import androidx.startup.Initializer
import com.truvideo.sdk.core.adapters.TruvideoSdkLogAdapterImpl
import com.truvideo.sdk.core.adapters.TruvideoSdkVersionPropertiesAdapterImpl

@Suppress("unused")
class TruvideoSdkInitializer : Initializer<Unit> {

    companion object {
        fun init(context: Context) {
            val versionPropertiesAdapter = TruvideoSdkVersionPropertiesAdapterImpl(
                context = context
            )

            val logAdapter = TruvideoSdkLogAdapterImpl(
                versionPropertiesAdapter = versionPropertiesAdapter
            )

            TruvideoSdk = TruvideoSdkImpl(
                context = context,
                logAdapter = logAdapter,
                versionPropertiesAdapter = versionPropertiesAdapter
            )
        }
    }

    override fun create(context: Context) = init(context)

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}

