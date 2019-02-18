package com.google.firebase.samples.apps.mlkit.kotlin.cloudimagelabeling

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase

/** Cloud Label Detector Demo.  */
class CloudImageLabelingProcessor : VisionProcessorBase<List<FirebaseVisionImageLabel>>() {

    private val detector: FirebaseVisionImageLabeler by lazy {
        FirebaseVisionCloudImageLabelerOptions.Builder()
            .build().let { options ->
                FirebaseVision.getInstance().getCloudImageLabeler(options)
            }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionImageLabel>> {
        return detector.processImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionImageLabel>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        Log.d(TAG, "cloud label size: ${results.size}")
        val labelsStr = ArrayList<String>()

        results.forEach {
            Log.d(TAG, "cloud label: $it")
            it.text?.let { text ->
                labelsStr.add(text)
            }
        }

        val cloudLabelGraphic = CloudLabelGraphic(graphicOverlay, labelsStr)
        graphicOverlay.add(cloudLabelGraphic)
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Cloud Label detection failed $e")
    }

    companion object {
        private const val TAG = "CloudImgLabelProc"
    }
}
