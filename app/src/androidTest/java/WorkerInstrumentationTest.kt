import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.workers.BlurWorker
import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class WorkerInstrumentationTest {

    lateinit var context: Context
    private val mockUriInput: Pair<String, String> =
        KEY_IMAGE_URI to "android.resource://com.example.bluromatic/drawable/android_cupcake"

    @Before
    fun setUP() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun cleanupWorker_doWork_resultSuccess(){
        val worker = TestListenableWorkerBuilder<CleanupWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Success)
        }
    }

//    val mockURI=Data.Builder()
//        .putString(KEY_IMAGE_URI,"android.resource://com.example.bluromatic/drawable/android_cupcake")
//        .build()


    @Test
    fun blurWorker_doWork_resultSuccessReturnsUri(){

            val worker= TestListenableWorkerBuilder<BlurWorker>(context)
                .setInputData(workDataOf(mockUriInput))
                .build()
        runBlocking {
            val result=worker.doWork()
            val resultUri=result.outputData.getString(KEY_IMAGE_URI)

            /*
            Make an assertion that the worker is successful.
             */
            assertTrue(result is ListenableWorker.Result.Success)

            /*
            The BlurWorker takes the URI and blur level from the input data and creates a
            temporary file. If the operation is successful, it returns a key-value pair
            containing the URI. To check that the contents of the output are correct, make an
            assertion that the output data contains the key KEY_IMAGE_URI.
             */
            assertTrue(result.outputData.keyValueMap.containsKey(KEY_IMAGE_URI))

            /*
            Make an assertion that the output data contains a URI that starts with the string
            "file:///data/user/0/com.example.bluromatic/files/blur_filter_outputs/blur-filter-output-"
             */
            assertTrue(
                resultUri?.startsWith("file:///data/user/0/com.example.bluromatic/files/blur_filter_outputs/blur-filter-output-")
                    ?: false
            )
        }
    }

    @Test
    fun saveImageToFileWorker_doWork_resultSuccessReturnsUrl() {
        val worker = TestListenableWorkerBuilder<SaveImageToFileWorker>(context)
            .setInputData(workDataOf(mockUriInput))
            .build()
        runBlocking {
            val result = worker.doWork()
            val resultUri = result.outputData.getString(KEY_IMAGE_URI)

            assertTrue(result is ListenableWorker.Result.Success)

            assertTrue(result.outputData.keyValueMap.containsKey(KEY_IMAGE_URI))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                TestCase.assertTrue(
                    resultUri?.startsWith("content://media/external_primary/images/media/")
                        ?: false
                )
            } else {
                TestCase.assertTrue(
                    resultUri?.startsWith("content://media/external/images/media/")
                        ?: false
                )
            }
        }
    }
}