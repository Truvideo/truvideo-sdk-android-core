package com.truvideo.sdk.core

import android.content.Context
import androidx.startup.Initializer
import com.truvideo.sdk.core.adapters.TruvideoSdkAuthAdapterImpl
import com.truvideo.sdk.core.usecases.PermissionUseCase
import com.truvideo.sdk.core.usecases.PickFileUseCase

@Suppress("unused")
class TruvideoSdkInitializer : Initializer<Unit> {

    companion object {
        fun init(context: Context) {
            val authAdapter = TruvideoSdkAuthAdapterImpl()
            val permissionUseCase = PermissionUseCase()
            val pickFileUseCase = PickFileUseCase(
                authAdapter = authAdapter,
                permissionUseCase = permissionUseCase
            )

            TruvideoSdk = TruvideoSdkImpl(
                context = context,
                pickFileUseCase = pickFileUseCase,
                permissionUseCase = permissionUseCase
            )
        }
    }

    override fun create(context: Context) = init(context)

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}

