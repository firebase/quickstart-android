package com.google.firebase.samples.apps.mlkit.kotlin.automl

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import com.google.firebase.samples.apps.mlkit.R
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.common.preference.PreferenceUtils
import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase
import com.google.firebase.samples.apps.mlkit.kotlin.labeldetector.LabelGraphic
import java.io.IOException
import java.util.Collections

/**
 * AutoML image labeler Demo.
 */
class AutoMLImageLabelerProcessor @Throws(FirebaseMLException::class)
constructor(private val context: Context, private val mode: Mode) :
    VisionProcessorBase<List<FirebaseVisionImageLabel>>() {

    private val detector: FirebaseVisionImageLabeler
    private val modelDownloadingTask: Task<Void>?

    /**
     * The detection mode of the processor. Different modes will have different behavior on whether or
     * not waiting for the model download complete.
     */
    enum class Mode {
        STILL_IMAGE,
        LIVE_PREVIEW
    }

    init {
        val modelChoice = PreferenceUtils.getAutoMLRemoteModelChoice(context)

        if (modelChoice == context.getString(R.string.pref_entries_automl_models_local)) {
            Log.d(TAG, "Local model used.")
            val localModel =
                FirebaseAutoMLLocalModel.Builder().setAssetFilePath("automl/manifest.json").build()
            detector =
                FirebaseVision.getInstance()
                    .getOnDeviceAutoMLImageLabeler(
                        FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0F)
                            .build()
                    )
            modelDownloadingTask = null
        } else {
            Log.d(TAG, "Remote model used.")
            val remoteModelName = PreferenceUtils.getAutoMLRemoteModelName(context)
            val remoteModel = FirebaseAutoMLRemoteModel.Builder(remoteModelName).build()

            val downloadConditions = FirebaseModelDownloadConditions.Builder().requireWifi().build()
            modelDownloadingTask =
                FirebaseModelManager.getInstance().download(remoteModel, downloadConditions)
            detector =
                FirebaseVision.getInstance()
                    .getOnDeviceAutoMLImageLabeler(
                        FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel)
                            .setConfidenceThreshold(0F)
                            .build()
                    )
        }
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close the image labeler: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionImageLabel>> {
        if (modelDownloadingTask == null) {
            // No download task means only the locally bundled model is used. Model can be used directly.
            return detector.processImage(image)
        } else if (!modelDownloadingTask.isComplete) {
            if (mode == Mode.LIVE_PREVIEW) {
                Log.i(TAG, "Model download is in progress. Skip detecting image.")
                return Tasks.forResult(Collections.emptyList())
            } else {
                Log.i(TAG, "Model download is in progress. Waiting...")
                return modelDownloadingTask.continueWithTask {
                    return@continueWithTask processImageOnDownloadComplete(image)
                }
            }
        } else {
            return processImageOnDownloadComplete(image)
        }
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        labels: List<FirebaseVisionImageLabel>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        if (originalCameraImage != null) {
            val imageGraphic = CameraImageGraphic(
                graphicOverlay,
                originalCameraImage
            )
            graphicOverlay.add(imageGraphic)
        }
        val labelGraphic = LabelGraphic(graphicOverlay, labels)
        graphicOverlay.add(labelGraphic)
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Label detection failed.$e")
    }

    private fun processImageOnDownloadComplete(image: FirebaseVisionImage): Task<List<FirebaseVisionImageLabel>> {
        return if (modelDownloadingTask != null && modelDownloadingTask.isSuccessful) {
            detector.processImage(image)
        } else {
            val downloadingError = "Error downloading remote model."
            Log.e(TAG, downloadingError, modelDownloadingTask?.exception)
            Toast.makeText(context, downloadingError, Toast.LENGTH_SHORT).show()
            Tasks.forException(
                Exception("Failed to download remote model.",
                    modelDownloadingTask?.exception
                ))
        }
    }

    companion object {
        private const val TAG = "ODAutoMLILProcessor"
    }
}
