package com.truvideo.sdk.core.usecases

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.truvideo.sdk.core.interfaces.TruvideoSdkCallback
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

internal class PermissionUseCase {

    private var handlers = mutableMapOf<ComponentActivity, TruvideoSdkPermissionHandler>()

    fun init(activity: ComponentActivity): TruvideoSdkPermissionHandler {
        var handler = handlers[activity]
        if (handler == null) {
            handler = TruvideoSdkPermissionHandler(activity)
            handlers[activity] = handler
        }

        return handler
    }
}

class TruvideoSdkPermissionHandler(private val activity: ComponentActivity) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var continuation: CancellableContinuation<Boolean>? = null
    private val startForResult: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val allGranted = it.values.all { permission -> permission }
            continuation?.resumeWith(Result.success(allGranted))
        }

    suspend fun askReadStoragePermission(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        startForResult.launch(permissions.toTypedArray())
        return suspendCancellableCoroutine { continuation = it }
    }

    fun askReadStoragePermission(callback: TruvideoSdkCallback<Boolean>) {
        scope.launch {
            val result = askReadStoragePermission()
            callback.onComplete(result)
        }
    }

    fun hasReadStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val videoPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)
            val imagePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
            return videoPermission == PackageManager.PERMISSION_GRANTED && imagePermission == PackageManager.PERMISSION_GRANTED
        } else {
            val storagePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            return storagePermission == PackageManager.PERMISSION_GRANTED
        }
    }
}