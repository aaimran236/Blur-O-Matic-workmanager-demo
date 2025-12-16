package com.example.bluromatic.workers

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluromatic.DELAY_TIME_MILLIS
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

                val picture = BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.drawable.android_cupcake
                )

                val output = blurBitmap(picture, 1)

                // Write bitmap to a temp file
                val outputUri = writeBitmapToFile(applicationContext, output)

                makeStatusNotification(
                    "Output is $outputUri",
                    applicationContext
                )

                /*
                 * Note: WorkManager uses Result.success() and Result.failure() to indicate the
                 * final status of the work request being performed.
                 */
                Result.success()

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