package com.tac.cropmodule3d;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

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

    public void setupAutoPins() {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        //get original image Mat;
        Mat imageMat = new Mat(new Size(bitmap.getWidth(), bitmap.getHeight()), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, imageMat);
        List<MatOfPoint2f> corners = new TransformationEngine().findCorners(bitmap, imageMat, 0.3f, 20, 2);
        MatOfPoint2f square = rectangleCornersFrom(corners);
//        MatOfPoint2f square = corners.get(0);

//        Utils.matToBitmap(corners, bitmap);
        if (square != null) {
            Point[] points = square.toArray();
//            corners.add(points[0]);
            Size size = imageMat.size();
            double v = getWidth() / size.width;
            double h = getHeight() / size.height;
            Point[] sorted = TransformationEngine.orderPoints(points);
            mPins[0].x = (int) (sorted[0].x * v);
            mPins[0].y = (int) (sorted[0].y * h);
            mPins[1].x = (int) (sorted[1].x * v);
            mPins[1].y = (int) (sorted[1].y * h);
            mPins[2].x = (int) (sorted[2].x * v);
            mPins[2].y = (int) (sorted[2].y * h);
            mPins[3].x = (int) (sorted[3].x * v);
            mPins[3].y = (int) (sorted[3].y * h);
        }
        setImageBitmap(bitmap);
    }

    private MatOfPoint2f rectangleCornersFrom(List<MatOfPoint2f> squares) {
        if (squares.size() > 0) {
            double maxSquareArea = -1.0;
            int maxSquareAreaIndex = -1;

            for (int index = 0; index < squares.size(); index++) {
                //TODO use abs value
                double squareArea = Imgproc.contourArea(squares.get(index));
                if (squareArea > maxSquareArea) {
                    maxSquareArea = squareArea;
                    maxSquareAreaIndex = index;
                }
            }
            MatOfPoint2f square = squares.get(maxSquareAreaIndex);
            return square;

        }
        return null;
    }

    public Pin pin1() {
        return mPins[0];
    }

    public Pin pin2() {
        return mPins[1];
    }

    public Pin pin3() {
        return mPins[2];
    }

    public Pin pin4() {
        return mPins[3];
    }

    public void crop() {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        //get original image Mat;
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);

        Bitmap output = TransformationEngine.get().initArea(pin1().point(), pin2().point(), pin3().point(), pin4().point())
                .transform(imageMat);

        setImageBitmap(output);
    }

    public static class Pin {
        public volatile int x;
        public volatile int y;
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
