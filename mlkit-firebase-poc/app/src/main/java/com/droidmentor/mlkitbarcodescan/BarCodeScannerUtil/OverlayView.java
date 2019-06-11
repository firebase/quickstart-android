package com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Jaison.
 */

public class OverlayView extends View {

    Paint paint;
    Canvas canvas;
    Context context;

    int viewPortHeight = 0;
    int viewPortWidth = 0;

    int viewHeight = 800;
    int viewWidth = 500;

    int horizontalPadding = 100;
    int verticalPadding = 50;

    Rect overlayRect;
    Region overlayRegion;
    int initialYPosition = 0;

    boolean initialDraw = false;
    boolean canImageMove = false;

    int prevX;
    int prevY;

    int downY = 0;
    int downX = 0;
    int currentX = 0;
    int currentY = 0;

    Rect oveylayRect=new Rect();

    boolean isMovableView;

    public static String TAG = "OverlayView";

    public OverlayView(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public OverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
    }

    public boolean isMovableView() {
        return isMovableView;
    }

    public void setMovableView(boolean movableView) {
        isMovableView = movableView;
    }

    private void init(Context context) {

        this.context = context;
        setFocusable(true); // necessary for getting the touch events

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.TRANSPARENT);
        paint.setColor(Color.parseColor("#AA000000"));

        canvas = new Canvas();
        overlayRect = new Rect(0, 0, 0, 0);
        overlayRegion = new Region(overlayRect);
    }

    public Rect getOverlayRect()
    {
        return overlayRect;
    }

    public void moveOverlay(final int top) {
        if (top >= verticalPadding) {

            if((top + viewHeight)<=(viewPortHeight-verticalPadding))
            {
                overlayRect.top = top;
                overlayRect.bottom = top + viewHeight;
                overlayRegion.set(overlayRect);
                prevY = top;
                invalidate();
                downY = top;
            }
            else
            {
                overlayRect.top = (viewPortHeight-verticalPadding)-viewHeight;
                overlayRect.bottom = viewPortHeight-verticalPadding;
                overlayRegion.set(overlayRect);
                prevY = overlayRect.top;
                invalidate();
                downY = overlayRect.top;
            }

        }
        else
        {
            overlayRect.top = verticalPadding;
            overlayRect.bottom = verticalPadding + viewHeight;
            overlayRegion.set(overlayRect);
            prevY = verticalPadding;
            invalidate();
            downY = verticalPadding;

        }
    }
    

    @Override
    protected void onDraw(Canvas canvas) {

        if (viewHeight >= viewPortWidth) {
            if (verticalPadding > 0)
                viewHeight = viewPortHeight - (2 * verticalPadding);
            else
                viewHeight = viewPortHeight;
        }

        canvas.save();
        if (!initialDraw) {
            initialYPosition = (viewPortHeight / 2) - (viewHeight / 2);
            initialDraw = true;
            overlayRect.set(horizontalPadding, initialYPosition, (viewPortWidth - horizontalPadding), (initialYPosition + viewHeight));
            overlayRegion.set(overlayRect);
        }

        canvas.clipRect(overlayRect);   //, Region.Op.XOR);
        canvas.drawPaint(paint);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        viewPortWidth = xNew;
        viewPortHeight = yNew;
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event) {

        if(isMovableView)
        {
            int eventaction = event.getAction();

            int positionX = (int) event.getX();
            int positionY = (int) event.getY();

            switch (eventaction) {
                case MotionEvent.ACTION_DOWN:
                    // touch down so check if the finger is on the rect or not
                    Log.d(TAG, "onTouchEvent: DOWN");

                    downX = (int) event.getX();
                    downY = (int) event.getY();

                    if (overlayRegion.contains(positionX, positionY)) {
                        prevX = positionX;
                        prevY = positionY;
                        canImageMove = true;
                        Log.d(TAG, "onTouchEvent: posY" + positionY);
                        Log.d(TAG, "onTouchEvent: DOWN valid");
                    }
                    break;
                case MotionEvent.ACTION_MOVE:

                    currentX = (int) event.getX();
                    currentY = (int) event.getY();

                    if (canImageMove) {
                        final int distY = Math.abs(positionY - prevY);
                        final int distX = Math.abs(positionX - prevX);

                        int updatedYPos = 0;

                        if (currentY > downY) {
                            //drag down
                            updatedYPos = overlayRect.bottom + distY;
                            //Log.d(TAG+(viewPortHeight-verticalPadding), "onTouchEvent: drag down"+updatedYPos);
                            if (updatedYPos <= (viewPortHeight - verticalPadding)) {
                                overlayRect.bottom = updatedYPos;
                                overlayRect.top = updatedYPos - viewHeight;
                                overlayRegion.set(overlayRect);
                                prevX = positionX;
                                prevY = positionY;
                                invalidate();
                                downY = currentY;
                            }
                        } else {
                            //drag up
                            updatedYPos = overlayRect.top - distY;
                            //Log.d(TAG+verticalPadding, "onTouchEvent: drag up"+updatedYPos);
                            if (updatedYPos > verticalPadding) {
                                overlayRect.top = updatedYPos;
                                overlayRect.bottom = updatedYPos + viewHeight;
                                overlayRegion.set(overlayRect);
                                prevX = positionX;
                                prevY = positionY;
                                invalidate();
                                downY = currentY;
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    // touch drop - just do things here after dropping
                    canImageMove = false;
                    break;

            }
            // redraw the canvas

            return true;
        }
        else
            return false;

    }
}
