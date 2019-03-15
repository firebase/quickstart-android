// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.samples.apps.mlkit.java.cloudlandmarkrecognition;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay.Graphic;

/** Graphic instance for rendering detected landmark. */
public class CloudLandmarkGraphic extends Graphic {
  private static final int TEXT_COLOR = Color.WHITE;
  private static final float TEXT_SIZE = 54.0f;
  private static final float STROKE_WIDTH = 4.0f;

  private final Paint rectPaint;
  private final Paint landmarkPaint;
  private FirebaseVisionCloudLandmark landmark;

  CloudLandmarkGraphic(GraphicOverlay overlay, FirebaseVisionCloudLandmark landmark) {
    super(overlay);

    this.landmark = landmark;
    rectPaint = new Paint();
    rectPaint.setColor(TEXT_COLOR);
    rectPaint.setStyle(Paint.Style.STROKE);
    rectPaint.setStrokeWidth(STROKE_WIDTH);

    landmarkPaint = new Paint();
    landmarkPaint.setColor(TEXT_COLOR);
    landmarkPaint.setTextSize(TEXT_SIZE);
  }

  /**
   * Draws the landmark block annotations for position, size, and raw value on the supplied canvas.
   */
  @Override
  public void draw(Canvas canvas) {
    if (landmark == null) {
      throw new IllegalStateException("Attempting to draw a null landmark.");
    }
    if (landmark.getLandmark() == null || landmark.getBoundingBox() == null) {
      return;
    }

    // Draws the bounding box around the LandmarkBlock.
    RectF rect = new RectF(landmark.getBoundingBox());
    rect.left = translateX(rect.left);
    rect.top = translateY(rect.top);
    rect.right = translateX(rect.right);
    rect.bottom = translateY(rect.bottom);
    canvas.drawRect(rect, rectPaint);

    // Renders the landmark at the bottom of the box.
    canvas.drawText(landmark.getLandmark(), rect.left, rect.bottom, landmarkPaint);
  }
}
