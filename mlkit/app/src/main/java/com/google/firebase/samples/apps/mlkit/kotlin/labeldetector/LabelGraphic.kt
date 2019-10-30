package com.google.firebase.samples.apps.mlkit.kotlin.labeldetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay

/** Graphic instance for rendering a label within an associated graphic overlay view.  */
class LabelGraphic(
    private val overlay: GraphicOverlay,
    private val labels: List<FirebaseVisionImageLabel>
) : GraphicOverlay.Graphic(overlay) {

    private val textPaint: Paint = Paint()

    init {
        textPaint.color = Color.RED
        textPaint.textSize = 70.0f
        postInvalidate()
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        val x = overlay.width / 4.0f
        var y = overlay.height / 2.0f

        for (label in labels) {
            canvas.drawText(label.text + ": " + label.confidence, x, y, textPaint)
            y -= 72.0f
        }
    }
}