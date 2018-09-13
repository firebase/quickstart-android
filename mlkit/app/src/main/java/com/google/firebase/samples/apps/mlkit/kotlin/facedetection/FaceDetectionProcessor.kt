package com.google.firebase.samples.apps.mlkit.kotlin.facedetection

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase
import java.io.IOException

/** Face Detector Demo.  */
class FaceDetectionProcessor : VisionProcessorBase<List<FirebaseVisionFace>>() {

    private val detector: FirebaseVisionFaceDetector

    init {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setTrackingEnabled(true)
                .build()

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }

    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
            faces: List<FirebaseVisionFace>,
            frameMetadata: FrameMetadata,
            graphicOverlay: GraphicOverlay) {
        graphicOverlay.clear()
        for (i in faces.indices) {
            val face = faces[i]
            val faceGraphic = FaceGraphic(graphicOverlay)
            graphicOverlay.add(faceGraphic)
            faceGraphic.updateFace(face, frameMetadata.cameraFacing)
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {

        private const val TAG = "FaceDetectionProcessor"
    }
}
