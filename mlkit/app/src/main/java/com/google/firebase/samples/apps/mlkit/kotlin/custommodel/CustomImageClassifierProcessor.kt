package com.google.firebase.samples.apps.mlkit.kotlin.custommodel

import android.app.Activity
import android.graphics.Bitmap
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.samples.apps.mlkit.common.BitmapUtils
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.common.VisionImageProcessor
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic
import java.nio.ByteBuffer

/** Custom Image Classifier Demo.  */
class CustomImageClassifierProcessor @Throws(FirebaseMLException::class)
constructor(private val activity: Activity) : VisionImageProcessor {

    private val classifier: CustomImageClassifier

    init {
        classifier = CustomImageClassifier(activity)
    }

    @Throws(FirebaseMLException::class)
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        classifier
            .classifyFrame(data, frameMetadata.width, frameMetadata.height)
            .addOnSuccessListener(
                activity
            ) { result ->
                val labelGraphic = LabelGraphic(graphicOverlay, result)
                val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
                val imageGraphic = CameraImageGraphic(graphicOverlay, bitmap)
                graphicOverlay.clear()
                graphicOverlay.add(imageGraphic)
                graphicOverlay.add(labelGraphic)
                graphicOverlay.postInvalidate()
            }
    }

    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        // nop
    }

    override fun stop() {}
}
