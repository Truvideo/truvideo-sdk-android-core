package com.truvideo.sdk.core.log_upload

import android.content.Context
import android.util.Log
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.CannedAccessControlList
import truvideo.sdk.common.model.TruvideoSdkStorageCredentials
import truvideo.sdk.common.sdk_common
import truvideo.sdk.common.service.log.interfaces.LogFileUploadHandler
import java.io.File
import kotlin.coroutines.suspendCoroutine

internal class LogFileUpload(private val context: Context) : LogFileUploadHandler {

    override suspend fun upload(path: String, credentials: TruvideoSdkStorageCredentials): String? {
        if (!sdk_common.connectivity.isOnline()) return null

        val region = credentials.region
        val poolId = credentials.identityPoolID
        val client = getClient(region, poolId)
        val transferUtility = getTransferUtility(context, client)
        val file = File(path)

        val name = file.name
        val nameParts = name.split(".").first().split("_")
        val logUid = nameParts[0]
        val logVersion = nameParts[1]
        val awsPath = "${credentials.bucketFolderLogs}/$logVersion/${logUid}.csv"
        Log.d("TruvideoSdkCore", "Log aws path: $awsPath")

        val transferObserver = transferUtility.upload(
            credentials.bucketName,
            awsPath,
            File(path),
            CannedAccessControlList.PublicRead
        )

        return suspendCoroutine { cont ->
            transferObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(s3Id: Int, state: TransferState) {
                    if (state == TransferState.WAITING_FOR_NETWORK) {
                        Log.d("TruvideoSdkCore", "Log. No connectivity. Cancelling the file upload")
                        transferUtility.cancel(s3Id)
                        cont.resumeWith(Result.success(null))
                    } else if (state == TransferState.COMPLETED) {
                        val url = client.getUrl(credentials.bucketName, awsPath).toString()
                        Log.d("TruvideoSdkCore", "Log. File upload ready. Url: $url")
                        cont.resumeWith(Result.success(url))
                    }
                }

                override fun onProgressChanged(
                    s3Id: Int, bytesCurrent: Long, bytesTotal: Long
                ) {
                }

                override fun onError(s3Id: Int, ex: Exception) {
                    Log.d("TruvideoSdkCore", "Log error", ex)
                    cont.resumeWith(Result.failure(ex))
                }
            })
        }
    }

    private suspend fun getClient(
        region: String,
        poolId: String,
    ): AmazonS3Client = suspendCoroutine {
        val parsedRegion = Regions.fromName(region)
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.maxErrorRetry = 0
        clientConfiguration.socketTimeout = 10 * 60 * 1000
        val credentialsProvider = CognitoCredentialsProvider(poolId, parsedRegion)
        val client = AmazonS3Client(
            credentialsProvider, Region.getRegion(parsedRegion), clientConfiguration
        )

        //TODO: check accelerate
        val accelerate = false
        client.setS3ClientOptions(
            S3ClientOptions.builder().setAccelerateModeEnabled(accelerate).build()
        )

        it.resumeWith(Result.success(client))
    }

    private suspend fun getTransferUtility(
        context: Context, client: AmazonS3Client
    ): TransferUtility = suspendCoroutine {
        TransferNetworkLossHandler.getInstance(context)
        val awsConfiguration = AWSMobileClient.getInstance().configuration
        val transferUtility = TransferUtility.builder().context(context).s3Client(client).awsConfiguration(awsConfiguration).build()
        it.resumeWith(Result.success(transferUtility))
    }
}