package com.google.firebase.samples.apps.mlkit.kotlin.cloudlandmarkrecognition

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase

/** Cloud Landmark Detector Demo.  */
class CloudLandmarkRecognitionProcessor : VisionProcessorBase<List<FirebaseVisionCloudLandmark>>() {

    private val detector: FirebaseVisionCloudLandmarkDetector

    init {
        val options = FirebaseVisionCloudDetectorOptions.Builder()
                .setMaxResults(10)
                .setModelType(FirebaseVisionCloudDetectorOptions.STABLE_MODEL)
                .build()

        detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(options)
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionCloudLandmark>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
            landmarks: List<FirebaseVisionCloudLandmark>,
            frameMetadata: FrameMetadata,
            graphicOverlay: GraphicOverlay) {
        graphicOverlay.clear()
        Log.d(TAG, "cloud landmark size: ${landmarks.size}")
        for (i in landmarks.indices) {
            val landmark = landmarks[i]
            Log.d(TAG, "cloud landmark: $landmark")
            val cloudLandmarkGraphic = CloudLandmarkGraphic(graphicOverlay)
            graphicOverlay.add(cloudLandmarkGraphic)
            cloudLandmarkGraphic.updateLandmark(landmark)
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Cloud Landmark detection failed $e")
    }

    companion object {
        private const val TAG = "CloudLmkRecProc"
    }
}
