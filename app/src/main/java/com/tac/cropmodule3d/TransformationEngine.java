package com.tac.cropmodule3d;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO implement javadoc
 */
public class TransformationEngine {
    private Point tl;
    private Point tr;
    private Point bl;
    private Point br;
    private Mat rect;

    public TransformationEngine() {
    }

    public TransformationEngine initArea(Point p1, Point p2, Point p3, Point p4) {

        orderPoints(p1, p2, p3, p4);
        rect = getMatFromPoints(tl, tr, br, bl);
        return this;
    }

    private void orderPoints(Point... pts) {
        double maxSum = Integer.MIN_VALUE;
        double minSum = Integer.MAX_VALUE;
        double maxDiff = Integer.MIN_VALUE;
        double minDiff = Integer.MAX_VALUE;
        int indexTl = -1;
        int indexBr = -1;
        int indexBl = -1;
        int indexTr = -1;
        for (int i = 0; i < pts.length; i++) {
            Point p = pts[i];
            if ((p.x + p.y) > maxSum) {
                maxSum = p.x + p.y;
                indexBr = i;
            }
            if ((p.x + p.y) < minSum) {
                minSum = p.x + p.y;
                indexTl = i;
            }
            if ((p.x - p.y) > maxDiff) {
                maxDiff = p.x - p.y;
                indexTr = i;
            }
            if ((p.x - p.y) < minDiff) {
                minDiff = p.x - p.y;
                indexBl = i;
            }
        }

        tl = pts[indexTl];
        tr = pts[indexTr];
        bl = pts[indexBl];
        br = pts[indexBr];
    }

    public Bitmap transform(Mat image) {


        float maxWidth = 0;
        float maxHeight = 0;

        float widthA = (float) Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        float widthB = (float) Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));
        maxWidth = Math.max(widthA, widthB);

        float heightA = (float) Math.sqrt(Math.pow(tr.y - br.y, 2) + Math.pow(tr.x - br.x, 2));
        float heightB = (float) Math.sqrt(Math.pow(tl.y - bl.y, 2) + Math.pow(tl.x - bl.x, 2));
        maxHeight = Math.max(heightA, heightB);

        Point dst1 = new Point(0, 0);
        Point dst2 = new Point(maxWidth, 0);
        Point dst3 = new Point(maxWidth, maxHeight);
        Point dst4 = new Point(0, maxHeight);

        Mat dst = getMatFromPoints(dst1, dst2, dst3, dst4);

        //compute the perspective transform matrix and then apply it
        Mat M = Imgproc.getPerspectiveTransform(rect, dst);
        Mat outputMat = new Mat((int) maxWidth, (int) maxHeight, CvType.CV_8UC4);
        Imgproc.warpPerspective(image, outputMat, M, new Size(maxWidth, maxHeight), Imgproc.INTER_CUBIC);

        //Write transformed mat to bitmap
        Bitmap output = Bitmap.createBitmap((int) maxWidth, (int) maxHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);

        return output;
    }


    public Mat findCorners(Mat imageFrame) {
        Imgproc.cvtColor(imageFrame, imageFrame, Imgproc.COLOR_BGR2GRAY);
        //there could be some processing
        Imgproc.cvtColor(imageFrame, imageFrame, Imgproc.COLOR_GRAY2BGRA, 4);

        Mat cornerMat = Mat.eye(imageFrame.width(), imageFrame.height(), CvType.CV_32FC1);

        Mat bit = imageFrame.clone();
        bit.convertTo(bit, CvType.CV_32FC1);


//        Imgproc.Canny(bit, bit, 400, 400);
        Imgproc.cornerHarris(bit, cornerMat, 2, 3, 0.04, 1);
        for (int y = 0; y < bit.height(); y++) {
            for (int x = 0; x < bit.width(); x++) {
                double[] harris = cornerMat.get(y, x); //get the x, y value
                //check the corner detector response
                if (harris[0] > 10e-06) {
                    Log.d("Harris point", "Point found");
                    //draw a small circle on the original image
                    Core.circle(imageFrame, new Point(x,y), 2, new Scalar(0,255,0));
                }
            }
        }
        return imageFrame;
    }

    public static Mat getMatFromPoints(Point... points) {
        List<Point> list = new LinkedList<>();
        Collections.addAll(list, points);
        return Converters.vector_Point2f_to_Mat(list);
    }

    public static TransformationEngine get() {
        return new TransformationEngine();
    }
}
