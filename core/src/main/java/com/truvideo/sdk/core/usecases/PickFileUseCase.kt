package com.truvideo.sdk.core.usecases

import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import com.truvideo.sdk.core.exceptions.TruvideoSdkException
import com.truvideo.sdk.core.interfaces.TruvideoSdkAuthAdapter
import com.truvideo.sdk.core.interfaces.TruvideoSdkCallback
import com.truvideo.sdk.core.model.TruvideoSdkFilePickerType
import com.truvideo.sdk.core.utils.UriUtils
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

internal class PickFileUseCase(
    private val authAdapter: TruvideoSdkAuthAdapter,
    private val permissionUseCase: PermissionUseCase
) {

    private var requesters = mutableMapOf<ComponentActivity, TruvideoSdkFilePicker>()

    fun init(activity: ComponentActivity): TruvideoSdkFilePicker {
        val permissionRequester = permissionUseCase.init(activity)

        var requester = requesters[activity]
        if (requester == null) {
            requester = TruvideoSdkFilePicker(activity, permissionRequester)
            requesters[activity] = requester
        }

        requester.authAdapter = authAdapter
        return requester
    }
}

class TruvideoSdkFilePicker(
    activity: ComponentActivity,
    private val permissionRequester: TruvideoSdkPermissionHandler
) {
    internal var authAdapter: TruvideoSdkAuthAdapter? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var continuation: CancellableContinuation<String?>? = null
    private val startForResult = activity.registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = UriUtils.realPathFromUri(activity, uri)
            if (path == null) {
                continuation?.resumeWith(Result.failure(TruvideoSdkException("No path found for the selected file")))
            } else {
                continuation?.resumeWith(Result.success(path))
            }
        } else {
            continuation?.resumeWith(Result.success(null))
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun pick(type: TruvideoSdkFilePickerType): String? {
        authAdapter?.validateAuthentication()

        val permission = permissionRequester.askReadStoragePermission()
        if (!permission) {
            throw TruvideoSdkException("Read storage permission denied")
        }

        val mediaType = when (type) {
            TruvideoSdkFilePickerType.Video -> PickVisualMedia.VideoOnly
            TruvideoSdkFilePickerType.Picture -> PickVisualMedia.ImageOnly
            TruvideoSdkFilePickerType.VideoAndPicture -> PickVisualMedia.ImageAndVideo
        }

        startForResult.launch(PickVisualMediaRequest(mediaType = mediaType))
        return suspendCancellableCoroutine { continuation = it }
    }


    @Suppress("unused")
    fun pick(type: TruvideoSdkFilePickerType, callback: TruvideoSdkCallback<String?>) {
        scope.launch {
            try {
                val result = pick(type)
                callback.onComplete(result)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException(exception.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }
}