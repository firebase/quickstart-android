package com.google.firebase.samples.apps.mlkit.kotlin.facedetection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import com.google.android.gms.vision.CameraSource
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(overlay: GraphicOverlay, private val firebaseVisionFace: FirebaseVisionFace?, private val facing: Int)
    : GraphicOverlay.Graphic(overlay) {

    /**
     * Draws the face annotations for position on the supplied canvas.
     */

    private val facePositionPaint = Paint().apply {
        color = Color.WHITE
    }

    private val idPaint = Paint().apply {
        color = Color.WHITE
        textSize = ID_TEXT_SIZE
    }

    private val boxPaint = Paint().apply {
        color = Color.WHITE
        style = Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    override fun draw(canvas: Canvas) {
        val face = firebaseVisionFace ?: return

        // Draws a circle at the position of the detected face, with the face's track id below.
        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint)
        canvas.drawText("id: " + face.trackingId, x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint)
        canvas.drawText(
            "happiness: ${String.format("%.2f", face.smilingProbability)}",
            x + ID_X_OFFSET * 3,
            y - ID_Y_OFFSET,
            idPaint
        )
        if (facing == CameraSource.CAMERA_FACING_FRONT) {
            canvas.drawText(
                "right eye: ${String.format("%.2f", face.rightEyeOpenProbability)}",
                x - ID_X_OFFSET,
                y,
                idPaint
            )
            canvas.drawText(
                "left eye: ${String.format("%.2f", face.leftEyeOpenProbability)}",
                x + ID_X_OFFSET * 6,
                y,
                idPaint
            )
        } else {
            canvas.drawText(
                "left eye: ${String.format("%.2f", face.leftEyeOpenProbability)}",
                x - ID_X_OFFSET,
                y,
                idPaint
            )
            canvas.drawText(
                "right eye: ${String.format("%.2f", face.rightEyeOpenProbability)}",
                x + ID_X_OFFSET * 6,
                y,
                idPaint
            )
        }

        // Draws a bounding box around the face.
        val xOffset = scaleX(face.boundingBox.width() / 2.0f)
        val yOffset = scaleY(face.boundingBox.height() / 2.0f)
        val left = x - xOffset
        val top = y - yOffset
        val right = x + xOffset
        val bottom = y + yOffset
        canvas.drawRect(left, top, right, bottom, boxPaint)

        // draw landmarks
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_BOTTOM)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_CHEEK)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EAR)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_LEFT)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EYE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.NOSE_BASE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_CHEEK)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EAR)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EYE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_RIGHT)
    }

    private fun drawLandmarkPosition(canvas: Canvas, face: FirebaseVisionFace, landmarkID: Int) {
        val landmark = face.getLandmark(landmarkID)
        landmark?.let {
            val point = it.position
            canvas.drawCircle(
                translateX(point.x),
                translateY(point.y),
                10f, idPaint
            )
        }
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 4.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val ID_Y_OFFSET = 50.0f
        private const val ID_X_OFFSET = -50.0f
        private const val BOX_STROKE_WIDTH = 5.0f
    }
}
