package com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/** Draw camera image to background. */
public class CameraImageGraphic extends GraphicOverlay.Graphic {
    private final Bitmap bitmap;

    public CameraImageGraphic(GraphicOverlay overlay, Bitmap bitmap) {
        super(overlay);
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
    }
}

