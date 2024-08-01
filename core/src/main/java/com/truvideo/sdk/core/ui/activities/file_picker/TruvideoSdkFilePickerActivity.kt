package com.truvideo.sdk.core.ui.activities.file_picker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.content.ContextCompat
import com.truvideo.sdk.core.adapters.TruvideoSdkAuthAdapterImpl
import com.truvideo.sdk.core.adapters.TruvideoSdkLogAdapterImpl
import com.truvideo.sdk.core.adapters.TruvideoSdkVersionPropertiesAdapterImpl
import com.truvideo.sdk.core.model.TruvideoSdkFilePickerType
import com.truvideo.sdk.core.utils.UriUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TruvideoSdkFilePickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val typeOrdinal = intent?.getIntExtra("type", -1)!!
        val type = TruvideoSdkFilePickerType.entries[typeOrdinal]
        val mediaType = when (type) {
            TruvideoSdkFilePickerType.Video -> PickVisualMedia.VideoOnly
            TruvideoSdkFilePickerType.Picture -> PickVisualMedia.ImageOnly
            TruvideoSdkFilePickerType.VideoAndPicture -> PickVisualMedia.ImageAndVideo
        }

        // Validate auth
        try {
            val authAdapter = TruvideoSdkAuthAdapterImpl(
                logAdapter = TruvideoSdkLogAdapterImpl(
                    versionPropertiesAdapter = TruvideoSdkVersionPropertiesAdapterImpl(
                        context = applicationContext
                    )
                )
            )
            authAdapter.validateAuthentication()
        } catch (exception: Exception) {
            finish()
            return
        }

        val filePickerResult = registerForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                val path = UriUtils.realPathFromUri(applicationContext, uri)
                setResult(RESULT_OK, Intent().apply {
                    putExtra("path", path)
                })
                finish()
            } else {
                finish()
            }
        }

        val permissionPickerResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val allGranted = it.values.all { permission -> permission }
            if (!allGranted) {
                finish()
                return@registerForActivityResult
            }

            filePickerResult.launch(PickVisualMediaRequest(mediaType = mediaType))
        }


        CoroutineScope(Dispatchers.Main).launch {
            // Validate read storage permission
            if (validatePermissions()) {
                filePickerResult.launch(PickVisualMediaRequest(mediaType = mediaType))
                return@launch
            }

            // Request read storage permission
            permissionPickerResult.launch(readStoragePermissions.toTypedArray())
        }
    }

    private val readStoragePermissions: List<String>
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            } else {
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }
        }

    private fun validatePermissions(): Boolean {
        val allGranted = readStoragePermissions.map {
            ContextCompat.checkSelfPermission(applicationContext, it)
        }.all { p -> p == PackageManager.PERMISSION_GRANTED }
        return allGranted
    }
}