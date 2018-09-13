package com.google.firebase.samples.apps.mlkit.kotlin.cloudlandmarkrecognition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay

/** Graphic instance for rendering detected landmark.  */
class CloudLandmarkGraphic(overlay: GraphicOverlay) : GraphicOverlay.Graphic(overlay) {

    private val rectPaint: Paint
    private val landmarkPaint: Paint
    private lateinit var landmark: FirebaseVisionCloudLandmark

    init {

        rectPaint = Paint()
        rectPaint.color = TEXT_COLOR
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = STROKE_WIDTH

        landmarkPaint = Paint()
        landmarkPaint.color = TEXT_COLOR
        landmarkPaint.textSize = TEXT_SIZE
    }

    /**
     * Updates the landmark instance from the detection of the most recent frame. Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    internal fun updateLandmark(landmark: FirebaseVisionCloudLandmark) {
        this.landmark = landmark
        postInvalidate()
    }

    /**
     * Draws the landmark block annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        if (landmark == null) {
            throw IllegalStateException("Attempting to draw a null landmark.")
        }
        if (landmark.landmark == null || landmark.boundingBox == null) {
            return
        }

        // Draws the bounding box around the LandmarkBlock.
        val rect = RectF(landmark.boundingBox)
        with(rect) {
            left = translateX(left)
            top = translateY(top)
            right = translateX(right)
            bottom = translateY(bottom)
            canvas.drawRect(this, rectPaint)

            // Renders the landmark at the bottom of the box.
            canvas.drawText(landmark.landmark, left, bottom, landmarkPaint)
        }

    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
    }
}