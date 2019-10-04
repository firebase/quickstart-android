package com.google.firebase.samples.apps.mlkit.kotlin.automl

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast

import com.google.android.gms.tasks.Task
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase
import com.google.firebase.samples.apps.mlkit.kotlin.labeldetector.LabelGraphic

import java.io.IOException

/**
 * AutoML image labeler Demo.
 */
class AutoMLImageLabelerProcessor @Throws(FirebaseMLException::class)
constructor(context: Context) : VisionProcessorBase<List<FirebaseVisionImageLabel>>() {

    private val detector: FirebaseVisionImageLabeler

    init {
        val remoteModel = FirebaseRemoteModel.Builder(REMOTE_MODEL_NAME).build()
        FirebaseModelManager.getInstance()
            .registerRemoteModel(remoteModel)

        FirebaseModelManager.getInstance()
            .registerLocalModel(
                FirebaseLocalModel.Builder(LOCAL_MODEL_NAME)
                    .setAssetFilePath("automl/manifest.json")
                    .build()
            )

        val optionsBuilder =
            FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder().setConfidenceThreshold(0.5f)

        optionsBuilder.setLocalModelName(LOCAL_MODEL_NAME).setRemoteModelName(REMOTE_MODEL_NAME)

        detector =
            FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(optionsBuilder.build())

        Toast.makeText(context, "Begin downloading the remote AutoML model.", Toast.LENGTH_SHORT)
            .show()
        // To track the download and get notified when the download completes, call
        // downloadRemoteModelIfNeeded. Note that if you don't call downloadRemoteModelIfNeeded, the model
        // downloading is still triggered implicitly.
        FirebaseModelManager.getInstance().downloadRemoteModelIfNeeded(remoteModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Download remote AutoML model success.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    val downloadingError =
                        "Error downloading remote model."
                    Log.e(TAG, downloadingError, task.exception)
                    Toast.makeText(context, downloadingError, Toast.LENGTH_SHORT).show()
                }
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
        return detector.processImage(image)
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

    companion object {

        private const val TAG = "ODAutoMLILProcessor"

        private const val LOCAL_MODEL_NAME = "automl_image_labeling_model"
        private const val REMOTE_MODEL_NAME = "mlkit_flowers"
    }
}
