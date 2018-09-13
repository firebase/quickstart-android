package com.google.firebase.samples.apps.mlkit.kotlin

import android.graphics.Bitmap
import android.media.Image
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.common.VisionImageProcessor
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T> : VisionImageProcessor {

    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private val shouldThrottle = AtomicBoolean(false)

    override fun process(
            data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        val metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(frameMetadata.width)
                .setHeight(frameMetadata.height)
                .setRotation(frameMetadata.rotation)
                .build()

        detectInVisionImage(
                FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay)
    }

    // Bitmap version
    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap), null, graphicOverlay)
    }

    /**
     * Detects feature from given media.Image
     *
     * @return created FirebaseVisionImage
     */
    override fun process(image: Image, rotation:Int, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        // This is for overlay display's usage
        val frameMetadata = FrameMetadata.Builder()
                .setWidth(image.width)
                .setHeight(image.height)
                .build()
        val fbVisionImage = FirebaseVisionImage.fromMediaImage(image, rotation)
        detectInVisionImage(fbVisionImage, frameMetadata, graphicOverlay)
    }

    private fun detectInVisionImage(
            image: FirebaseVisionImage,
            metadata: FrameMetadata?,
            graphicOverlay: GraphicOverlay) {
        detectInImage(image)
                .addOnSuccessListener { results ->
                    shouldThrottle.set(false)
                    metadata?.let {
                        onSuccess(results, it, graphicOverlay)
                    }
                }
                .addOnFailureListener { e ->
                    shouldThrottle.set(false)
                    this@VisionProcessorBase.onFailure(e)
                }
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true)
    }

    override fun stop() {}

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    protected abstract fun onSuccess(
            results:T,
            frameMetadata: FrameMetadata,
            graphicOverlay: GraphicOverlay)

    protected abstract fun onFailure(e:Exception)

}
