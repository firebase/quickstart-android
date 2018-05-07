package com.google.firebase.samples.apps.mlkit.cloudtextrecognition;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay.Graphic;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class CloudTextGraphic extends Graphic {
  private static final int TEXT_COLOR = Color.WHITE;
  private static final float TEXT_SIZE = 54.0f;
  private static final float STROKE_WIDTH = 4.0f;

  private final Paint rectPaint;
  private final Paint textPaint;
  private final FirebaseVisionCloudText text;
  private final GraphicOverlay overlay;

  CloudTextGraphic(GraphicOverlay overlay, FirebaseVisionCloudText text) {
    super(overlay);

    this.text = text;
    this.overlay = overlay;

    rectPaint = new Paint();
    rectPaint.setColor(TEXT_COLOR);
    rectPaint.setStyle(Paint.Style.STROKE);
    rectPaint.setStrokeWidth(STROKE_WIDTH);

    textPaint = new Paint();
    textPaint.setColor(TEXT_COLOR);
    textPaint.setTextSize(TEXT_SIZE);
    // Redraw the overlay, as this graphic has been added.
    postInvalidate();
  }

  /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
  @Override
  public void draw(Canvas canvas) {
    if (text == null) {
      throw new IllegalStateException("Attempting to draw a null text.");
    }

    float x = overlay.getWidth() / 4.0f;
    float y = overlay.getHeight() / 4.0f;

    canvas.drawText(text.getText(), x, y, textPaint);
  }
}
