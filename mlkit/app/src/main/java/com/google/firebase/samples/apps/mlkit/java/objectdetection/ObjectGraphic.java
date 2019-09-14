package com.google.firebase.samples.apps.mlkit.java.objectdetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay.Graphic;

/** Draw the detected object info in preview. */
public class ObjectGraphic extends Graphic {

  private static final float TEXT_SIZE = 54.0f;
  private static final float STROKE_WIDTH = 4.0f;

  private final FirebaseVisionObject object;
  private final Paint boxPaint;
  private final Paint textPaint;

  ObjectGraphic(GraphicOverlay overlay, FirebaseVisionObject object) {
    super(overlay);

    this.object = object;

    boxPaint = new Paint();
    boxPaint.setColor(Color.WHITE);
    boxPaint.setStyle(Style.STROKE);
    boxPaint.setStrokeWidth(STROKE_WIDTH);

    textPaint = new Paint();
    textPaint.setColor(Color.WHITE);
    textPaint.setTextSize(TEXT_SIZE);
  }

  @Override
  public void draw(Canvas canvas) {
    // Draws the bounding box.
    RectF rect = new RectF(object.getBoundingBox());
    rect.left = translateX(rect.left);
    rect.top = translateY(rect.top);
    rect.right = translateX(rect.right);
    rect.bottom = translateY(rect.bottom);
    canvas.drawRect(rect, boxPaint);

    // Draws other object info.
    canvas.drawText(
        getCategoryName(object.getClassificationCategory()), rect.left, rect.bottom, textPaint);
    canvas.drawText("trackingId: " + object.getTrackingId(), rect.left, rect.top, textPaint);
    canvas.drawText(
        "confidence: " + object.getClassificationConfidence(), rect.right, rect.bottom, textPaint);
  }

  private static String getCategoryName(@FirebaseVisionObject.Category int category) {
    switch (category) {
      case FirebaseVisionObject.CATEGORY_UNKNOWN:
        return "Unknown";
      case FirebaseVisionObject.CATEGORY_HOME_GOOD:
        return "Home good";
      case FirebaseVisionObject.CATEGORY_FASHION_GOOD:
        return "Fashion good";
      case FirebaseVisionObject.CATEGORY_PLACE:
        return "Place";
      case FirebaseVisionObject.CATEGORY_PLANT:
        return "Plant";
      case FirebaseVisionObject.CATEGORY_FOOD:
        return "Food";
      default: // fall out
    }
    return "";
  }
}

