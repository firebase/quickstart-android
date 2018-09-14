package com.google.firebase.samples.apps.mlkit.kotlin.cloudtextrecognition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class CloudTextGraphic(
    overlay: GraphicOverlay,
    private val element: FirebaseVisionText.Element?
) : GraphicOverlay.Graphic(overlay) {

    private val rectPaint: Paint
    private val textPaint: Paint

    init {

        rectPaint = Paint()
        rectPaint.color = TEXT_COLOR
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = STROKE_WIDTH

        textPaint = Paint()
        textPaint.color = TEXT_COLOR
        textPaint.textSize = TEXT_SIZE
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas.  */
    override fun draw(canvas: Canvas) {
        if (element == null) {
            throw IllegalStateException("Attempting to draw a null text.")
        }

        val rect = element.boundingBox
        rect?.let {
            canvas.drawRect(it, rectPaint)
            canvas.drawText(element.text, it.left.toFloat(), it.bottom.toFloat(), textPaint)
        }
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
    }
}