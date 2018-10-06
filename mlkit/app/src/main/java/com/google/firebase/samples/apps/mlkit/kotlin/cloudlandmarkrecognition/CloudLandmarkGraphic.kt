package com.google.firebase.samples.apps.mlkit.kotlin.cloudlandmarkrecognition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay

/** Graphic instance for rendering detected landmark.  */
class CloudLandmarkGraphic(overlay: GraphicOverlay) : GraphicOverlay.Graphic(overlay) {

    private val rectPaint = Paint().apply {
        color = TEXT_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }
    private val landmarkPaint = Paint().apply {
        color = TEXT_COLOR
        textSize = TEXT_SIZE
    }
    private var landmark: FirebaseVisionCloudLandmark? = null

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
        landmark?.let { lm ->
            if (lm.landmark == null || lm.boundingBox == null) {
                return
            }

            // Draws the bounding box around the LandmarkBlock.
            val rect = RectF(lm.boundingBox)
            with(rect) {
                left = translateX(left)
                top = translateY(top)
                right = translateX(right)
                bottom = translateY(bottom)
                canvas.drawRect(this, rectPaint)

                // Renders the landmark at the bottom of the box.
                canvas.drawText(lm.landmark, left, bottom, landmarkPaint)
            }
        } ?: kotlin.run { throw IllegalStateException("Attempting to draw a null landmark.") }
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
    }
}