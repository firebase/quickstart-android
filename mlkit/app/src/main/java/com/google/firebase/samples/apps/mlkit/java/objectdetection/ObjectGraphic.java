package com.google.firebase.samples.apps.mlkit.java.objectdetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import com.google.firebase.ml.vision.object.FirebaseVisionObject;
import com.google.firebase.ml.vision.object.FirebaseVisionObject.Category;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay.Graphic;

/** Draw the detected object info in preview. */
public class ObjectGraphic extends Graphic {

  private static final int TEXT_COLOR = Color.WHITE;
  private static final float TEXT_SIZE = 54.0f;
  private static final float STROKE_WIDTH = 4.0f;

  private final Paint rectPaint;
  private final Paint labelPaint;
  private final Paint idPaint;
  private final Paint scorePaint;
  private final Paint entityIdPaint;
  private final FirebaseVisionObject object;

  ObjectGraphic(GraphicOverlay overlay, FirebaseVisionObject object) {
    super(overlay);

    this.object = object;

    rectPaint = new Paint();
    rectPaint.setColor(TEXT_COLOR);
    rectPaint.setStyle(Style.STROKE);
    rectPaint.setStrokeWidth(STROKE_WIDTH);

    labelPaint = new Paint();
    labelPaint.setColor(TEXT_COLOR);
    labelPaint.setTextSize(TEXT_SIZE);

    idPaint = new Paint();
    idPaint.setColor(TEXT_COLOR);
    idPaint.setTextSize(TEXT_SIZE);

    scorePaint = new Paint();
    scorePaint.setColor(TEXT_COLOR);
    scorePaint.setTextSize(TEXT_SIZE);

    entityIdPaint = new Paint();
    entityIdPaint.setColor(TEXT_COLOR);
    entityIdPaint.setTextSize(TEXT_SIZE);
  }

  @Override
  public void draw(Canvas canvas) {
    // Draws the bounding box.
    RectF rect = new RectF(object.getBoundingBox());
    rect.left = translateX(rect.left);
    rect.top = translateY(rect.top);
    rect.right = translateX(rect.right);
    rect.bottom = translateY(rect.bottom);
    canvas.drawRect(rect, rectPaint);

    // Renders the label at the bottom of the box.

    canvas.drawText(
        getLabel(object.getClassificationCategory()), rect.left, rect.bottom, labelPaint);
    canvas.drawText("id: " + object.getTrackingId(), rect.left, rect.top, idPaint);
    canvas.drawText(
        "confidence: " + object.getClassificationConfidence(), rect.right, rect.bottom, idPaint);
    canvas.drawText("eid:" + object.getEntityId(), rect.right, rect.top, labelPaint);
  }

  private static String getLabel(@Category int category) {
    switch (category) {
      case FirebaseVisionObject.CATEGORY_UNKNOWN:
        return "Unknown";
      case FirebaseVisionObject.CATEGORY_HOME_GOOD:
        return "Home good";
      case FirebaseVisionObject.CATEGORY_FASHION_GOOD:
        return "Fashion good";
      case FirebaseVisionObject.CATEGORY_PLACE:
        return "Place";
      case FirebaseVisionObject.CATEGORY_ANIMAL:
        return "Animal";
      case FirebaseVisionObject.CATEGORY_PLANT:
        return "Plant";
      case FirebaseVisionObject.CATEGORY_FOOD:
        return "Food";
      default: // fall out
    }
    return "";
  }
}

