package com.google.firebase.samples.apps.mlkit.kotlin.imagelabeling

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionLabel
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase
import java.io.IOException

/** Custom Image Classifier Demo.  */
class ImageLabelingProcessor : VisionProcessorBase<List<FirebaseVisionLabel>>() {

    private val detector: FirebaseVisionLabelDetector

    init {
        detector = FirebaseVision.getInstance().visionLabelDetector
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: $e")
        }

    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionLabel>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
            labels: List<FirebaseVisionLabel>,
            frameMetadata: FrameMetadata,
            graphicOverlay: GraphicOverlay) {
        graphicOverlay.clear()
        val labelGraphic = LabelGraphic(graphicOverlay, labels)
        graphicOverlay.add(labelGraphic)
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Label detection failed.$e")
    }

    companion object {

        private const val TAG = "ImageLabelingProcessor"
    }
}