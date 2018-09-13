package com.google.firebase.samples.apps.mlkit.kotlin.custommodel

import android.app.Activity
import android.graphics.*
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.experimental.and

/** A `FirebaseModelInterpreter` based image classifier.  */
class CustomImageClassifier
/** Initializes an `CustomImageClassifier`.  */
@Throws(FirebaseMLException::class)
constructor(activity: Activity) {

    /* Preallocated buffers for storing image data in. */
    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)

    /** An instance of the driver class to run model inference with Firebase.  */
    private val interpreter: FirebaseModelInterpreter?

    /** Data configuration of input & output data of model.  */
    private val dataOptions: FirebaseModelInputOutputOptions

    /** Labels corresponding to the output of the vision model.  */
    private val labelList: List<String>

    private val sortedLabels = PriorityQueue<MutableMap.MutableEntry<String, Float>>(
            RESULTS_TO_SHOW,
            Comparator<Map.Entry<String, Float>> { o1, o2 ->
                o1.value.compareTo(o2.value)
            })

    init {
        val modelOptions = FirebaseModelOptions.Builder()
                .setCloudModelName(HOSTED_MODEL_NAME)
                .setLocalModelName(LOCAL_MODEL_NAME)
                .build()
        val conditions = FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build()
        val localModelSource = FirebaseLocalModelSource.Builder(LOCAL_MODEL_NAME)
                .setAssetFilePath(LOCAL_MODEL_PATH).build()
        val cloudSource = FirebaseCloudModelSource.Builder(HOSTED_MODEL_NAME)
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)  // You could also specify different
                // conditions for updates.
                .build()
        val manager = FirebaseModelManager.getInstance()
        manager.registerLocalModelSource(localModelSource)
        manager.registerCloudModelSource(cloudSource)
        interpreter = FirebaseModelInterpreter.getInstance(modelOptions)
        labelList = loadLabelList(activity)
        Log.d(TAG, "Created a Custom Image Classifier.")
        val inputDims = intArrayOf(DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE)
        val outputDims = intArrayOf(1, labelList.size)
        dataOptions = FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.BYTE, inputDims)
                .setOutputFormat(0, FirebaseModelDataType.BYTE, outputDims)
                .build()
        Log.d(TAG, "Configured input & output data for the custom image classifier.")
    }

    /** Classifies a frame from the preview stream.  */
    @Throws(FirebaseMLException::class)
    internal fun classifyFrame(buffer: ByteBuffer, width: Int, height: Int): Task<List<String>> {
        if (interpreter == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.")
            val uninitialized = ArrayList<String>()
            uninitialized.add("Uninitialized Classifier.")
            Tasks.forResult<List<String>>(uninitialized)
        }
        // Create input data.
        val imgData = convertBitmapToByteBuffer(buffer, width, height)

        val inputs = FirebaseModelInputs.Builder().add(imgData).build()
        // Here's where the magic happens!!
        return interpreter!!
                .run(inputs, dataOptions)
                .continueWith { task ->
                    val labelProbArray = task.result.getOutput<Array<ByteArray>>(0)
                    printTopKLabels(labelProbArray)
                }
    }

    /** Reads label list from Assets.  */
    private fun loadLabelList(activity: Activity): List<String> {
        val labelList = ArrayList<String>()
        try {
            BufferedReader(InputStreamReader(activity.assets.open(LABEL_PATH))).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    labelList.add(line)
                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read label list.", e)
        }

        return labelList
    }

    /** Writes Image data into a `ByteBuffer`.  */
    @Synchronized
    private fun convertBitmapToByteBuffer(
            buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(
                DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE)
        imgData.order(ByteOrder.nativeOrder())
        val bitmap = createResizedBitmap(buffer, width, height)
        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to int points.
        var pixel = 0
        val startTime = SystemClock.uptimeMillis()
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val value = intValues[pixel++]
                imgData.put((value shr 16 and 0xFF).toByte())
                imgData.put((value shr 8 and 0xFF).toByte())
                imgData.put((value and 0xFF).toByte())
            }
        }
        val endTime = SystemClock.uptimeMillis()
        val timeCost = endTime - startTime
        Log.d(TAG, "Timecost to put values into ByteBuffer: $timeCost")
        return imgData
    }

    /** Resizes image data from `ByteBuffer`.  */
    private fun createResizedBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap {
        val img = YuvImage(buffer.array(), ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        img.compressToJpeg(Rect(0, 0, img.width, img.height), 50, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true)
    }

    /** Prints top-K labels, to be shown in UI as the results.  */
    @Synchronized
    private fun printTopKLabels(labelProbArray: Array<ByteArray>): List<String> {
        for (i in labelList.indices) {
            sortedLabels.add(
                    AbstractMap.SimpleEntry<String, Float>(labelList[i], (labelProbArray[0][i] and 0xff.toByte()) / 255.0f))
            if (sortedLabels.size > RESULTS_TO_SHOW) {
                sortedLabels.poll()
            }
        }
        val result = ArrayList<String>()
        val size = sortedLabels.size
        for (i in 0 until size) {
            val label = sortedLabels.poll()
            result.add("${label.key}:${label.value}")
        }
        return result
    }

    companion object {

        /** Tag for the [Log].  */
        private const val TAG = "MLKitDemoApp:Classifier"

        /** Name of the model file.  */
        private const val LOCAL_MODEL_NAME = "mobilenet_quant_v1"

        /** Path of the model file stored in Assets.  */
        private const val LOCAL_MODEL_PATH = "mobilenet_quant_v1_224.tflite"

        /** Name of the model uploaded to the Firebase console.  */
        private const val HOSTED_MODEL_NAME = "mobilenet_v1"

        /** Name of the label file stored in Assets.  */
        private const val LABEL_PATH = "labels.txt"

        /** Number of results to show in the UI.  */
        private const val RESULTS_TO_SHOW = 3

        /** Dimensions of inputs.  */
        private const val DIM_BATCH_SIZE = 1

        private const val DIM_PIXEL_SIZE = 3

        private const val DIM_IMG_SIZE_X = 224
        private const val DIM_IMG_SIZE_Y = 224
    }
}