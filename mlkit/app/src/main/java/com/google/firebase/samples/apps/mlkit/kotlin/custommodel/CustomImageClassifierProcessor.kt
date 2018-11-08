package com.google.firebase.samples.apps.mlkit.kotlin.custommodel

import android.app.Activity
import android.graphics.Bitmap
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.samples.apps.mlkit.common.BitmapUtils
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.common.VisionImageProcessor
import java.lang.ref.WeakReference
import java.nio.ByteBuffer

/** Custom Image Classifier Demo.  */
class CustomImageClassifierProcessor @Throws(FirebaseMLException::class)
constructor(activity: Activity) : VisionImageProcessor {

    private val classifier = CustomImageClassifier(activity)
    private val activityRef = WeakReference(activity)

    @Throws(FirebaseMLException::class)
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        activityRef.get()?.let { activity ->
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
    }

    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        // nop
    }

    override fun stop() {}
}
