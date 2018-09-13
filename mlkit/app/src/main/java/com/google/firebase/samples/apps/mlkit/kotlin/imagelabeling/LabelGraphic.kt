package com.google.firebase.samples.apps.mlkit.kotlin.imagelabeling

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.label.FirebaseVisionLabel
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay

/** Graphic instance for rendering a label within an associated graphic overlay view.  */
class LabelGraphic (
        private val overlay: GraphicOverlay,
        private val labels: List<FirebaseVisionLabel>
) : GraphicOverlay.Graphic(overlay) {

    private val textPaint: Paint

    init {
        textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 60.0f
        postInvalidate()
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        val x = overlay.width / 4.0f
        var y = overlay.height / 2.0f

        for (label in labels) {
            canvas.drawText(label.label, x, y, textPaint)
            y -= 62.0f
        }
    }
}