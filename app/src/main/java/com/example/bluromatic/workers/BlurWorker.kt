package com.example.bluromatic.workers

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

/*
The BlurWorker class extends the CoroutineWorker class instead of the more general Worker
class. The CoroutineWorker class implementation of the doWork() is a suspending function,
which lets it run asynchronous code that a Worker cannot do. As detailed in the guide
Threading in WorkManager, "CoroutineWorker is the recommended implementation for Kotlin
users."
 */

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)

    override suspend fun doWork(): Result {

        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        /*
         * A CoroutineWorker, by default, runs as Dispatchers.Default but can be changed by calling
         * withContext() and passing in the desired dispatcher.

         * Inside the call to withContext() pass Dispatchers.IO so the lambda function runs in a
         *  special thread pool for potentially blocking IO operations.
         */
        return withContext(Dispatchers.IO){

            /*
                Because this Worker runs very quickly, it is recommended to add a delay in
                the code to emulate slower running work.
                 */
            // This is an utility function added to emulate slower work.
            delay(DELAY_TIME_MILLIS)

            //return try {
            return@withContext try {

                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                    errorMessage
                }
//                val picture = BitmapFactory.decodeResource(
//                    applicationContext.resources,
//                    R.drawable.android_cupcake
//                )


                /*
                Since the image source is passed in as a URI, we need a ContentResolver object
                to read the contents pointed to by the URI.
                 */

                val resolver = applicationContext.contentResolver

                ///Because the image source is now the passed in URI, use
                // BitmapFactory.decodeStream() instead of BitmapFactory.decodeResource()
                // to create the Bitmap object.
                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )

                val output = blurBitmap(picture, blurLevel)

                // Write bitmap to a temp file
                val outputUri = writeBitmapToFile(applicationContext, output)

//                makeStatusNotification(
//                    "Output is $outputUri",
//                    applicationContext
//                )

                // The workDataOf() function creates a Data object from the passed in key and value pair.
                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

                /*
                 * Note: WorkManager uses Result.success() and Result.failure() to indicate the
                 * final status of the work request being performed.
                 */
                Result.success(outputData)

            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )
                Result.failure()
            }
        }

    }

}