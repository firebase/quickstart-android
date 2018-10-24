package com.google.firebase.samples.apps.mlkit.kotlin.cloudimagelabeling

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase

/** Cloud Label Detector Demo.  */
class CloudImageLabelingProcessor : VisionProcessorBase<List<FirebaseVisionCloudLabel>>() {

    private val detector: FirebaseVisionCloudLabelDetector by lazy {
        FirebaseVisionCloudDetectorOptions.Builder()
            .setMaxResults(10)
            .setModelType(FirebaseVisionCloudDetectorOptions.STABLE_MODEL)
            .build().let { options ->
                FirebaseVision.getInstance().getVisionCloudLabelDetector(options)
            }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionCloudLabel>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap,
        results: List<FirebaseVisionCloudLabel>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {

        graphicOverlay.clear()

        Log.d(TAG, "cloud label size: ${results.size}")

        val labelsStr = ArrayList<String>()
        for (i in results.indices) {
            val result = results[i]

            Log.d(TAG, "cloud label: $result")

            result.label?.let {
                labelsStr.add(it)
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
