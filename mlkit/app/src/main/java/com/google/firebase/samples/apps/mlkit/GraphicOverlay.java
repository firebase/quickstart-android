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
package com.google.firebase.samples.apps.mlkit;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.camera2.CameraCharacteristics;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.CameraSource;

import java.util.HashSet;
import java.util.Set;

/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview). The creator can add graphics objects, update the objects, and remove
 * them, triggering the appropriate drawing and invalidation within the view.
 *
 * <p>Supports scaling and mirroring of the graphics relative the camera's preview properties. The
 * idea is that detection items are expressed in terms of a preview size, but need to be scaled up
 * to the full view size, and also mirrored in the case of the front-facing camera.
 *
 * <p>Associated {@link Graphic} items should use the following methods to convert to view
 * coordinates for the graphics that are drawn:
 *
 * <ol>
 *   <li>{@link Graphic#scaleX(float)} and {@link Graphic#scaleY(float)} adjust the size of the
 *       supplied value from the preview scale to the view scale.
 *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
 *       coordinate from the preview's coordinate system to the view coordinate system.
 * </ol>
 */
public class GraphicOverlay extends View {
  private final Object lock = new Object();
  private int previewWidth;
  private float widthScaleFactor = 1.0f;
  private int previewHeight;
  private float heightScaleFactor = 1.0f;
  private int facing = CameraSource.CAMERA_FACING_BACK;
  private Set<Graphic> graphics = new HashSet<>();

//  private int mRatioWidth = 0;
//  private int mRatioHeight = 0;

  /**
   * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
   * this and implement the {@link Graphic#draw(Canvas)} method to define the graphics element. Add
   * instances to the overlay using {@link GraphicOverlay#add(Graphic)}.
   */
  public abstract static class Graphic {
    private GraphicOverlay overlay;

    public Graphic(GraphicOverlay overlay) {
      this.overlay = overlay;
    }

    /**
     * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
     * to view coordinates for the graphics that are drawn:
     *
     * <ol>
     *   <li>{@link Graphic#scaleX(float)} and {@link Graphic#scaleY(float)} adjust the size of the
     *       supplied value from the preview scale to the view scale.
     *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
     *       coordinate from the preview's coordinate system to the view coordinate system.
     * </ol>
     *
     * @param canvas drawing canvas
     */
    public abstract void draw(Canvas canvas);

    /**
     * Adjusts a horizontal value of the supplied value from the preview scale to the view scale.
     */
    public float scaleX(float horizontal) {
      return horizontal * overlay.widthScaleFactor;
    }

    /** Adjusts a vertical value of the supplied value from the preview scale to the view scale. */
    public float scaleY(float vertical) {
      return vertical * overlay.heightScaleFactor;
    }

    /** Returns the application context of the app. */
    public Context getApplicationContext() {
      return overlay.getContext().getApplicationContext();
    }

    /**
     * Adjusts the x coordinate from the preview's coordinate system to the view coordinate system.
     */
    public float translateX(float x) {
      if (overlay.facing == CameraSource.CAMERA_FACING_FRONT) {
        return overlay.getWidth() - scaleX(x);
      } else {
        return scaleX(x);
      }
    }

    /**
     * Adjusts the y coordinate from the preview's coordinate system to the view coordinate system.
     */
    public float translateY(float y) {
      return scaleY(y);
    }

    public void postInvalidate() {
      overlay.postInvalidate();
    }
  }

  public GraphicOverlay(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /** Removes all graphics from the overlay. */
  public void clear() {
    synchronized (lock) {
      graphics.clear();
    }
    postInvalidate();
  }

  /** Adds a graphic to the overlay. */
  public void add(Graphic graphic) {
    synchronized (lock) {
      graphics.add(graphic);
    }
    postInvalidate();
  }

  /** Removes a graphic from the overlay. */
  public void remove(Graphic graphic) {
    synchronized (lock) {
      graphics.remove(graphic);
    }
    postInvalidate();
  }

  /**
   * Sets the camera attributes for size and facing direction, which informs how to transform image
   * coordinates later.
   */
  public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
    synchronized (lock) {
      this.previewWidth = previewWidth;
      this.previewHeight = previewHeight;
      this.facing = facing;
    }
    postInvalidate();
  }

  /** Draws the overlay with its associated graphic objects. */
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    synchronized (lock) {
      if ((previewWidth != 0) && (previewHeight != 0)) {
        widthScaleFactor = (float) canvas.getWidth() / (float) previewWidth;
        heightScaleFactor = (float) canvas.getHeight() / (float) previewHeight;
      }

      for (Graphic graphic : graphics) {
        graphic.draw(canvas);
      }
    }
  }

//  /**
//   * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
//   * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
//   * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
//   *
//   * @param width  Relative horizontal size
//   * @param height Relative vertical size
//   */
//  public void setAspectRatio(int width, int height) {
//    if (width < 0 || height < 0) {
//      throw new IllegalArgumentException("Size cannot be negative.");
//    }
//    mRatioWidth = width;
//    mRatioHeight = height;
//    requestLayout();
//  }
//
//  @Override
//  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    int width = MeasureSpec.getSize(widthMeasureSpec);
//    int height = MeasureSpec.getSize(heightMeasureSpec);
//    if (0 == mRatioWidth || 0 == mRatioHeight) {
//      setMeasuredDimension(width, height);
//    } else {
//      if (width < height * mRatioWidth / mRatioHeight) {
//        setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
//      } else {
//        setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
//      }
//    }
//  }
}
