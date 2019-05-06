package com.google.firebase.samples.apps.mlkit.kotlin.cloudtextrecognition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class CloudDocumentTextGraphic(
    overlay: GraphicOverlay,
    private val symbol: FirebaseVisionDocumentText.Symbol?
) : GraphicOverlay.Graphic(overlay) {

    private val rectPaint = Paint().apply {
        color = TEXT_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    private val textPaint = Paint().apply {
        color = TEXT_COLOR
        textSize = TEXT_SIZE
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas.  */
    override fun draw(canvas: Canvas) {
        symbol?.let { syb ->
            val rect = syb.boundingBox
            rect?.let {
                canvas.drawRect(it, rectPaint)
                canvas.drawText(syb.text, it.left.toFloat(), it.bottom.toFloat(), textPaint)
            }
        } ?: kotlin.run { throw IllegalStateException("Attempting to draw a null text.") }
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
    }
}
