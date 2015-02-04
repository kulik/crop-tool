package com.tac.cropmodule3d;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.opencv.core.Point;

/**
 * TODO implement javadoc
 */
public class CropImageView extends ImageView {
    private Pin[] mPins;
    private Paint red;
    private Paint textWhite;
    private Pin mActivePin;

    public CropImageView(Context context) {
        super(context);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        red = new Paint();
        red.setColor(Color.RED);
        red.setStrokeWidth(3);
        red.setTextSize(24);
        red.setAntiAlias(true);

        textWhite = new Paint();
        textWhite.setColor(Color.WHITE);
        textWhite.setTextSize(24);
        textWhite.setFakeBoldText(true);
        textWhite.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                findTouchedPin(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePin != null) {
                    int r = mActivePin.radius;
                    //don't go over view bounds
                    if (r + x > getWidth() ||
                            x - r < 0 ||
                            y - r < 0 ||
                            y + r > getHeight()) {
                        return true;
                    }
                    mActivePin.x = (int) x;
                    mActivePin.y = (int) y;
                    mActivePin.updateTouch();
                }
                break;
        }
        invalidate();
        return true;
    }

    public boolean findTouchedPin(float x, float y) {
        boolean isTouched = false;
        for (Pin p : mPins) {
            isTouched = p.touchRect.contains((int) x, (int) y);
            mActivePin = isTouched ? p : null;
            if (isTouched) {
                break;
            }
        }
        return isTouched;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPins == null) {
            initDefaultPoints();
        }
        for (int i = 0; i < mPins.length; i++) {
            Pin p = mPins[i];
            canvas.drawCircle(p.x, p.y, p.radius, red);
            canvas.drawText(String.valueOf(i + 1), p.x, p.y, textWhite);
            canvas.drawText("x:" + p.x + " y:" + p.y, p.x - p.radius * 2, p.y + p.radius * 2, red);
        }

        //1 - 2
        canvas.drawLine(mPins[0].x, mPins[0].y, mPins[1].x, mPins[1].y, red);

        //2 - 4
        canvas.drawLine(mPins[1].x, mPins[1].y, mPins[3].x, mPins[3].y, red);

        //3 - 4
        canvas.drawLine(mPins[2].x, mPins[2].y, mPins[3].x, mPins[3].y, red);

        //1 - 3
        canvas.drawLine(mPins[2].x, mPins[2].y, mPins[0].x, mPins[0].y, red);
    }

    private void initDefaultPoints() {
        int width = getWidth();
        int height = getHeight();

        mPins = new Pin[4];
        mPins[0] = new Pin(width / 4, height / 4, 40);
        mPins[1] = new Pin(width * 3 / 4, height / 4, 40);
        mPins[2] = new Pin(width / 4, height * 3 / 4, 40);
        mPins[3] = new Pin(width * 3 / 4, height * 3 / 4, 40);
    }

    public Point pin1() {
        return mPins[0].point();
    }

    public Point pin2() {
        return mPins[1].point();
    }

    public Point pin3() {
        return mPins[2].point();
    }

    public Point pin4() {
        return mPins[3].point();
    }

    private static class Pin {
        int x;
        int y;
        int radius;
        Rect touchRect;

        public Pin(int x, int y, int r) {
            this.x = x;
            this.y = y;
            this.radius = r;
            this.touchRect = new Rect(x - radius / 2, y - radius / 2, x + radius / 2, y + radius / 2);
        }

        public Point point() {
            return new Point(x, y);
        }

        public void updateTouch() {
            touchRect.set(x - radius / 2, y - radius / 2, x + radius / 2, y + radius / 2);
        }
    }
}
