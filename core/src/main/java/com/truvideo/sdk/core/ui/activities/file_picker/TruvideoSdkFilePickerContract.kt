package com.truvideo.sdk.core.ui.activities.file_picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.truvideo.sdk.core.model.TruvideoSdkFilePickerType

class TruvideoSdkFilePickerContract : ActivityResultContract<TruvideoSdkFilePickerType, String?>() {
    override fun createIntent(context: Context, input: TruvideoSdkFilePickerType): Intent {
        return Intent(context, TruvideoSdkFilePickerActivity::class.java).apply {
            putExtra("type", input.ordinal)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.getStringExtra("path")
            else -> null
        }
    }
}