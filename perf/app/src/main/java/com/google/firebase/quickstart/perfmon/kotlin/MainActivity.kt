package com.google.firebase.quickstart.perfmon.kotlin

import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.quickstart.perfmon.R
import kotlinx.android.synthetic.main.activity_main.button
import kotlinx.android.synthetic.main.activity_main.headerIcon
import kotlinx.android.synthetic.main.activity_main.textViewContent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Random
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

class MainActivity : AppCompatActivity() {

    private lateinit var trace: Trace

    private val numStartupTasks = CountDownLatch(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            // write 40 chars of random text to file
            val contentFile = File(this.filesDir, CONTENT_FILE)

            writeStringToFile(contentFile.absolutePath, "${getRandomString(40)}\n")
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.e(TAG, "Unable to write to file", task.exception)
                            return@addOnCompleteListener
                        }

                        loadFileFromDisk()
                    }
        }

        // Begin tracing app startup tasks.
        trace = FirebasePerformance.getInstance().newTrace(STARTUP_TRACE_NAME)
        Log.d(TAG, "Starting trace")
        trace.start()
        loadImageFromWeb()
        // Increment the counter of number of requests sent in the trace.
        Log.d(TAG, "Incrementing number of requests counter in trace")
        trace.incrementMetric(REQUESTS_COUNTER_NAME, 1)
        loadFileFromDisk()
        // Wait for app startup tasks to complete asynchronously and stop the trace.
        Thread(Runnable {
            try {
                numStartupTasks.await()
            } catch (e: InterruptedException) {
                Log.e(TAG, "Unable to wait for startup task completion.")
            } finally {
                Log.d(TAG, "Stopping trace")
                trace.stop()
                runOnUiThread {
                    Toast.makeText(this, "Trace completed",
                            Toast.LENGTH_SHORT).show()
                }
            }
        }).start()
    }

    private fun loadImageFromWeb() {
        Glide.with(this).load(IMAGE_URL)
                .placeholder(ColorDrawable(ContextCompat.getColor(this, R.color.colorAccent)))
                .listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(
                        e: Exception,
                        model: String,
                        target: Target<GlideDrawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        numStartupTasks.countDown() // Signal end of image load task.
                        return false
                    }

                    override fun onResourceReady(
                        resource: GlideDrawable,
                        model: String,
                        target: Target<GlideDrawable>,
                        isFromMemoryCache: Boolean,
                        isFirstResource: Boolean
                    ): Boolean {
                        numStartupTasks.countDown() // Signal end of image load task.
                        return false
                    }
                }).into(headerIcon)
    }

    private fun writeStringToFile(filename: String, content: String): Task<Void> {
        return Tasks.call(
                AsyncTask.THREAD_POOL_EXECUTOR,
                Callable<Void> {
                    val fos = FileOutputStream(filename, true)
                    fos.write(content.toByteArray())
                    fos.close()
                    null
                })
    }

    private fun loadStringFromFile(): Task<String> {
        return Tasks.call(
                AsyncTask.THREAD_POOL_EXECUTOR,
                Callable {
                    val contentFile = File(filesDir, CONTENT_FILE)
                    if (contentFile.createNewFile()) {
                        // Content file exist did not exist in internal storage and new file was created.
                        // Copy in the default content.
                        val `is`: InputStream = assets.open(DEFAULT_CONTENT_FILE)
                        val size = `is`.available()
                        val buffer = ByteArray(size)
                        `is`.read(buffer)
                        `is`.close()
                        val fos = FileOutputStream(contentFile)
                        fos.write(buffer)
                        fos.close()
                    }
                    val fis = FileInputStream(contentFile)
                    val content = ByteArray(contentFile.length().toInt())
                    fis.read(content)
                    return@Callable String(content)
                })
    }

    private fun loadFileFromDisk() {
        loadStringFromFile()
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e(TAG, "Couldn't read text file.")
                        Toast.makeText(
                                this, getString(R.string.text_read_error), Toast.LENGTH_LONG)
                                .show()
                        return@OnCompleteListener
                    }

                    val fileContent = task.result
                    textViewContent.text = task.result
                    // Increment a counter with the file size that was read.
                    Log.d(TAG, "Incrementing file size counter in trace")
                    trace.incrementMetric(
                            FILE_SIZE_COUNTER_NAME,
                            fileContent!!.toByteArray().size.toLong())
                    numStartupTasks.countDown()
                })
    }

    private fun getRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        val sb = StringBuilder()
        val random = Random()
        for (i in 0 until length) {
            val c = chars[random.nextInt(chars.size)]
            sb.append(c)
        }
        return sb.toString()
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val DEFAULT_CONTENT_FILE = "default_content.txt"
        private const val CONTENT_FILE = "content.txt"
        private const val IMAGE_URL =
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"

        private const val STARTUP_TRACE_NAME = "startup_trace"
        private const val REQUESTS_COUNTER_NAME = "requests sent"
        private const val FILE_SIZE_COUNTER_NAME = "file size"
    }
}
