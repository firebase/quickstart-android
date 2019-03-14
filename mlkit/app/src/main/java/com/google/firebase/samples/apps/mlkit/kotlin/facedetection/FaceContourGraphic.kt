package com.google.firebase.samples.apps.mlkit.kotlin.facedetection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay.Graphic

/** Graphic instance for rendering face contours graphic overlay view.  */
class FaceContourGraphic(overlay: GraphicOverlay, private val firebaseVisionFace: FirebaseVisionFace?)
    : Graphic(overlay) {

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint

    init {
        val selectedColor = Color.WHITE

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor
        idPaint.textSize = ID_TEXT_SIZE

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    /** Draws the face annotations for position on the supplied canvas.  */
    override fun draw(canvas: Canvas) {
        val face = firebaseVisionFace ?: return

        // Draws a circle at the position of the detected face, with the face's track id below.
        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint)
        canvas.drawText("id: ${face.trackingId}", x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint)

        // Draws a bounding box around the face.
        val xOffset = scaleX(face.boundingBox.width() / 2.0f)
        val yOffset = scaleY(face.boundingBox.height() / 2.0f)
        val left = x - xOffset
        val top = y - yOffset
        val right = x + xOffset
        val bottom = y + yOffset
        canvas.drawRect(left, top, right, bottom, boxPaint)

        val contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS)
        for (point in contour.points) {
            val px = translateX(point.x)
            val py = translateY(point.y)
            canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint)
        }

        if (face.smilingProbability >= 0) {
            canvas.drawText(
                    "happiness: ${String.format("%.2f", face.smilingProbability)}",
                    x + ID_X_OFFSET * 3,
                    y - ID_Y_OFFSET,
                    idPaint)
        }

        if (face.rightEyeOpenProbability >= 0) {
            canvas.drawText(
                    "right eye: ${String.format("%.2f", face.rightEyeOpenProbability)}",
                    x - ID_X_OFFSET,
                    y,
                    idPaint)
        }
        if (face.leftEyeOpenProbability >= 0) {
            canvas.drawText(
                    "left eye: ${String.format("%.2f", face.leftEyeOpenProbability)}",
                    x + ID_X_OFFSET * 6,
                    y,
                    idPaint)
        }
        val leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
        leftEye?.position?.let {
            canvas.drawCircle(
                    translateX(it.x),
                    translateY(it.y),
                    FACE_POSITION_RADIUS,
                    facePositionPaint)
        }
        val rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
        rightEye?.position?.let {
            canvas.drawCircle(
                    translateX(it.x),
                    translateY(it.y),
                    FACE_POSITION_RADIUS,
                    facePositionPaint)
        }
        val leftCheek = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK)
        leftCheek?.position?.let {
            canvas.drawCircle(
                    translateX(it.x),
                    translateY(it.y),
                    FACE_POSITION_RADIUS,
                    facePositionPaint)
        }

        val rightCheek = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK)
        rightCheek?.position?.let {
            canvas.drawCircle(
                    translateX(it.x),
                    translateY(it.y),
                    FACE_POSITION_RADIUS,
                    facePositionPaint)
        }
    }

    companion object {

        private const val FACE_POSITION_RADIUS = 4.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val ID_Y_OFFSET = 80.0f
        private const val ID_X_OFFSET = -70.0f
        private const val BOX_STROKE_WIDTH = 5.0f
    }
}
