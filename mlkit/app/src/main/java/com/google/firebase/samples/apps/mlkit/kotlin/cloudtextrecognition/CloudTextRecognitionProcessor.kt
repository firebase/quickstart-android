package com.google.firebase.samples.apps.mlkit.kotlin.cloudtextrecognition

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase

/**
 * Processor for the cloud text detector demo.
 */
class CloudTextRecognitionProcessor : VisionProcessorBase<FirebaseVisionText>() {

    private val detector: FirebaseVisionTextRecognizer = FirebaseVision.getInstance().cloudTextRecognizer

    override fun detectInImage(image: FirebaseVisionImage): Task<FirebaseVisionText> {
        return detector.processImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: FirebaseVisionText,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        if (results == null) {
            return // TODO: investigate why this is needed
        }
        val blocks = results.textBlocks
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (l in elements.indices) {
                    val cloudTextGraphic = CloudTextGraphic(
                        graphicOverlay,
                        elements[l]
                    )
                    graphicOverlay.add(cloudTextGraphic)
                    graphicOverlay.postInvalidate()
                }
            }
        }
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Cloud Text detection failed.$e")
    }

    companion object {

        private const val TAG = "CloudTextRecProc"
    }
}
