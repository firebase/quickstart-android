// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.samples.apps.mlkit.kotlin.custommodel

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.custom.FirebaseModelInterpreter
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions
import com.google.firebase.ml.custom.FirebaseModelOptions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseModelDataType
import com.google.firebase.ml.custom.FirebaseModelInputs
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.AbstractMap
import java.util.PriorityQueue
import kotlin.experimental.and

/**
 * A `FirebaseModelInterpreter` based image classifier.
 */
class CustomImageClassifier
/**
 * Initializes an `CustomImageClassifier`.
 */
@Throws(FirebaseMLException::class)
internal constructor(activity: Activity, private val useQuantizedModel: Boolean) {

    /* Preallocated buffers for storing image data in. */
    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)

    /**
     * An instance of the driver class to run model inference with Firebase.
     */
    private val interpreter: FirebaseModelInterpreter?

    /**
     * Data configuration of input & output data of model.
     */
    private val dataOptions: FirebaseModelInputOutputOptions

    /**
     * Labels corresponding to the output of the vision model.
     */
    private val labelList: List<String>

    private val sortedLabels = PriorityQueue<AbstractMap.SimpleEntry<String, Float>>(
            RESULTS_TO_SHOW,
            Comparator<AbstractMap.SimpleEntry<String, Float>> { o1, o2 -> o1.value.compareTo(o2.value) })

    /**
     * Gets the top-K labels, to be shown in UI as the results.
     */
    private val topKLabels: List<String>
        @Synchronized get() {
            val result = ArrayList<String>()
            val size = sortedLabels.size
            for (i in 0 until size) {
                val label = sortedLabels.poll()
                result.add("${label.key} : ${label.value}")
            }
            return result
        }

    init {
        val localModelName = if (useQuantizedModel)
            LOCAL_QUANT_MODEL_NAME
        else
            LOCAL_FLOAT_MODEL_NAME
        val hostedModelName = if (useQuantizedModel)
            HOSTED_QUANT_MODEL_NAME
        else
            HOSTED_FLOAT_MODEL_NAME
        val localModelPath = if (useQuantizedModel)
            LOCAL_QUANT_MODEL_PATH
        else
            LOCAL_FLOAT_MODEL_PATH
        val modelOptions = FirebaseModelOptions.Builder()
                .setRemoteModelName(hostedModelName)
                .setLocalModelName(localModelName)
                .build()
        val conditions = FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build()
        val localModelSource = FirebaseLocalModel.Builder(localModelName)
                .setAssetFilePath(localModelPath).build()
        val cloudSource = FirebaseRemoteModel.Builder(hostedModelName)
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions) // You could also specify different
                // conditions for updates.
                .build()
        val manager = FirebaseModelManager.getInstance()
        manager.registerLocalModel(localModelSource)
        manager.registerRemoteModel(cloudSource)
        interpreter = FirebaseModelInterpreter.getInstance(modelOptions)
        labelList = loadLabelList(activity)
        Log.d(TAG, "Created a Custom Image Classifier.")
        val inputDims = intArrayOf(DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE)
        val outputDims = intArrayOf(1, labelList.size)

        val dataType = if (useQuantizedModel)
            FirebaseModelDataType.BYTE
        else
            FirebaseModelDataType.FLOAT32
        dataOptions = FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, dataType, inputDims)
                .setOutputFormat(0, dataType, outputDims)
                .build()
        Log.d(TAG, "Configured input & output data for the custom image classifier.")
    }

    /**
     * Classifies a frame from the preview stream.
     */
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
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get labels array: ${e.message}")
                    e.printStackTrace()
                }
                .continueWith { task ->
                    if (useQuantizedModel) {
                        val labelProbArray = task.result!!.getOutput<Array<ByteArray>>(0)
                        getTopLabels(labelProbArray)
                    } else {
                        val labelProbArray = task.result!!.getOutput<Array<FloatArray>>(0)
                        getTopLabels(labelProbArray)
                    }
                }
    }

    /**
     * Reads label list from Assets.
     */
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

    /**
     * Writes Image data into a `ByteBuffer`.
     */
    @Synchronized
    private fun convertBitmapToByteBuffer(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val bytesPerChannel = if (useQuantizedModel)
            QUANT_NUM_OF_BYTES_PER_CHANNEL
        else
            FLOAT_NUM_OF_BYTES_PER_CHANNEL
        val imgData = ByteBuffer.allocateDirect(
                bytesPerChannel * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE)
        imgData.order(ByteOrder.nativeOrder())
        val bitmap = createResizedBitmap(buffer, width, height)
        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width,
                bitmap.height)
        // Convert the image to int points.
        var pixel = 0
        val startTime = SystemClock.uptimeMillis()
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val value = intValues[pixel++]
                // Normalize the values according to the model used:
                // Quantized model expects a [0, 255] scale while a float model expects [0, 1].
                if (useQuantizedModel) {
                    imgData.put((value shr 16 and 0xFF).toByte())
                    imgData.put((value shr 8 and 0xFF).toByte())
                    imgData.put((value and 0xFF).toByte())
                } else {
                    imgData.putFloat((value shr 16 and 0xFF) / 255.0f)
                    imgData.putFloat((value shr 8 and 0xFF) / 255.0f)
                    imgData.putFloat((value and 0xFF) / 255.0f)
                }
            }
        }
        val endTime = SystemClock.uptimeMillis()
        Log.d(TAG, "Timecost to put values into ByteBuffer: ${(endTime - startTime)}")
        return imgData
    }

    /**
     * Resizes image data from `ByteBuffer`.
     */
    private fun createResizedBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap {
        val img = YuvImage(buffer.array(), ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        img.compressToJpeg(Rect(0, 0, img.width, img.height), 50, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true)
    }

    @Synchronized
    private fun getTopLabels(labelProbArray: Array<ByteArray>): List<String> {
        for (i in labelList.indices) {
            sortedLabels.add(
                    AbstractMap.SimpleEntry(labelList[i],
                            (labelProbArray[0][i] and 0xff.toByte()) / 255.0f))
            if (sortedLabels.size > RESULTS_TO_SHOW) {
                sortedLabels.poll()
            }
        }
        return topKLabels
    }

    @Synchronized
    private fun getTopLabels(labelProbArray: Array<FloatArray>): List<String> {
        for (i in labelList.indices) {
            sortedLabels.add(
                    AbstractMap.SimpleEntry(labelList[i], labelProbArray[0][i]))
            if (sortedLabels.size > RESULTS_TO_SHOW) {
                sortedLabels.poll()
            }
        }
        return topKLabels
    }

    companion object {

        /**
         * Tag for the [Log].
         */
        private const val TAG = "MLKitDemoApp:Classifier"

        /**
         * Name of the floating point model file.
         */
        private const val LOCAL_FLOAT_MODEL_NAME = "mobilenet_float_v2_1.0_299"

        /**
         * Path of the floating point model file stored in Assets.
         */
        private const val LOCAL_FLOAT_MODEL_PATH = "mobilenet_float_v2_1.0_299.tflite"

        /**
         * Name of the floating point model uploaded to the Firebase console.
         */
        private const val HOSTED_FLOAT_MODEL_NAME = "mobilenet_float_v2_1.0_299"

        /**
         * Name of the quantized model file.
         */
        private const val LOCAL_QUANT_MODEL_NAME = "mobilenet_quant_v2_1.0_299"

        /**
         * Path of the quantized model file stored in Assets.
         */
        private const val LOCAL_QUANT_MODEL_PATH = "mobilenet_quant_v2_1.0_299.tflite"

        /**
         * Name of the quantized model uploaded to the Firebase console.
         */
        private const val HOSTED_QUANT_MODEL_NAME = "mobilenet_quant_v2_1.0_299"

        /**
         * Name of the label file stored in Assets.
         */
        private const val LABEL_PATH = "labels.txt"

        /**
         * Number of results to show in the UI.
         */
        private const val RESULTS_TO_SHOW = 3

        /**
         * Dimensions of inputs.
         */
        private const val DIM_BATCH_SIZE = 1

        private const val DIM_PIXEL_SIZE = 3

        private const val DIM_IMG_SIZE_X = 299
        private const val DIM_IMG_SIZE_Y = 299
        private const val QUANT_NUM_OF_BYTES_PER_CHANNEL = 1
        private const val FLOAT_NUM_OF_BYTES_PER_CHANNEL = 4
    }
}
