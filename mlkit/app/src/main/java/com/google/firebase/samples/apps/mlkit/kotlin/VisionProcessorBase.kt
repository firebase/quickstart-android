package com.google.firebase.samples.apps.mlkit.kotlin

import android.graphics.Bitmap
import android.support.annotation.GuardedBy
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.samples.apps.mlkit.common.BitmapUtils
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.common.VisionImageProcessor
import java.nio.ByteBuffer

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T> : VisionImageProcessor {

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    @Synchronized
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay)
        }
    }

    // Bitmap version
    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        detectInVisionImage(
            null /* bitmap */,
            FirebaseVisionImage.fromBitmap(bitmap),
            null,
            graphicOverlay
        )
    }

    @Synchronized
    private fun processLatestImage(graphicOverlay: GraphicOverlay) {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null) {
            processImage(
                processingImage as ByteBuffer,
                processingMetaData as FrameMetadata,
                graphicOverlay
            )
        }
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setWidth(frameMetadata.getWidth())
            .setHeight(frameMetadata.getHeight())
            .setRotation(frameMetadata.getRotation())
            .build()

        val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
        detectInVisionImage(
            bitmap,
            FirebaseVisionImage.fromByteBuffer(data, metadata),
            frameMetadata,
            graphicOverlay
        )
    }

    private fun detectInVisionImage(
        originalCameraImage: Bitmap?,
        image: FirebaseVisionImage,
        metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                metadata?.let {
                    onSuccess(originalCameraImage!!, results, it, graphicOverlay)
                }
            }
            .addOnFailureListener { e ->
                this@VisionProcessorBase.onFailure(e)
            }
    }

    override fun stop() {}

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    protected abstract fun onSuccess(
        originalCameraImage: Bitmap,
        results: T,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    )

    protected abstract fun onFailure(e: Exception)
}
