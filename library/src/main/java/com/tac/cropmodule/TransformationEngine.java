package com.tac.cropmodule;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
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
    private static final String TAG = TransformationEngine.class.getSimpleName();


    private Point tl;
    private Point tr;
    private Point bl;
    private Point br;
    private Mat rect;

    public TransformationEngine() {
    }

    public TransformationEngine initArea(Point p1, Point p2, Point p3, Point p4) {

        orderMyPoints(p1, p2, p3, p4);
        rect = getMatFromPoints(tl, tr, br, bl);
        return this;
    }

    private void orderMyPoints(Point... pts) {
        Point[] ordered = orderPoints(pts);

        tl = ordered[0];
        tr = ordered[1];
        bl = ordered[2];
        br = ordered[3];
    }

    public static Point[] orderPoints(Point... pts) {
        double maxSum = Integer.MIN_VALUE;
        double minSum = Integer.MAX_VALUE;
        double maxDiff = Integer.MIN_VALUE;
        double minDiff = Integer.MAX_VALUE;
        int indexTl = 0;
        int indexBr = 1;
        int indexBl = 2;
        int indexTr = 3;
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
        Point[] sortedPoints = new Point[4];
        sortedPoints[0] = pts[indexTl];
        sortedPoints[1] = pts[indexTr];
        sortedPoints[2] = pts[indexBl];
        sortedPoints[3] = pts[indexBr];
        return sortedPoints;
    }

    public Bitmap transform(Mat image) {


        float maxWidth = 0;
        float maxHeight = 0;

        float widthA = (float) Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        float widthB = (float) Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));
        maxWidth = Math.min(widthA, widthB);

        float heightA = (float) Math.sqrt(Math.pow(tr.y - br.y, 2) + Math.pow(tr.x - br.x, 2));
        float heightB = (float) Math.sqrt(Math.pow(tl.y - bl.y, 2) + Math.pow(tl.x - bl.x, 2));
        maxHeight = Math.min(heightA, heightB);

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

    private double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;

        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    public synchronized List<MatOfPoint2f> findCorners(Bitmap bitmap, Mat imageFrame, float tolerance, int threshold, int thresholdLevel) {
        List<MatOfPoint2f> squares = new LinkedList<MatOfPoint2f>();
        // blur will enhance edge detection
        Mat blured = new Mat(imageFrame.size(), CvType.CV_8UC1);
        Imgproc.GaussianBlur(imageFrame, blured, new Size(9, 9), 2d, 2d);

        Mat gray0 = new Mat(blured.size(), CvType.CV_8UC(1));
        Mat gray = new Mat();

        Imgproc.cvtColor(blured, gray0, Imgproc.COLOR_BGR2GRAY);

        List<Mat> blurredlist = new ArrayList<Mat>();
        List<Mat> graylist = new ArrayList<Mat>();
        blurredlist.add(blured);
        graylist.add(gray0);
//        vector<vector<Point> > contours;

        // find squares in every color plane of the image
        for (int c = 0; c < 3; c++) {
            int ch[] = {c, 0};

            MatOfInt fromto = new MatOfInt(ch);

            Core.mixChannels(blurredlist, graylist, fromto);

            for (int l = 0; l < thresholdLevel; l++) {
                // Use Canny instead of zero threshold level!
                // Canny helps to catch squares with gradient shading
                if (l == 0) {
//                    Imgproc.Canny(gray0, gray, 10, threshold, 3, false); //
                    Imgproc.Canny(gray0, gray, 3, threshold); //

                    // Dilate helps to remove potential holes between edge segments
                    Imgproc.dilate(gray, gray, new Mat());
//                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1);
                } else {
//                    Imgproc.threshold(gray0, gray, thresholdLevel, threshold, Imgproc.THRESH_BINARY);
                    Imgproc.threshold(gray0, gray, (l + 1) * 255 / thresholdLevel, 255, Imgproc.THRESH_BINARY);
                }

                List<MatOfPoint> contours = new LinkedList<>();
                // Find contours and store them in a list
                Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
                Utils.matToBitmap(imageFrame, bitmap);

                // Test contours
//                vector<Point2f> approx;
                for (int i = 0; i < contours.size(); i++) {
                final MatOfPoint2f approx = new MatOfPoint2f();

                    // approximate contour with accuracy proportional
                    // to the contour perimeter
//                    vSeq result = cvApproxPoly(contours, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, cvContourPerimeter(contours) * 0.02, 0);
                    MatOfPoint2f matOfPoint = new MatOfPoint2f(contours.get(i).toArray());
                    double arcLen = Imgproc.arcLength(matOfPoint, true) * 0.02;
                    Imgproc.approxPolyDP(matOfPoint, approx, arcLen, true);

                    // Note: absolute value of an area is used because
                    // area may be positive or negative - in accordance with the
                    // contour orientation
                    double area = Math.abs(Imgproc.contourArea(approx));

                    boolean isConv = Imgproc.isContourConvex(new MatOfPoint(approx.toArray()));
                    double minArea = (gray0.size().width * gray0.size().height) * 0.20f;
                    double maxArea = (gray0.size().width * gray0.size().height) * 0.99f;
//                    if (area > minArea) {
//                        Imgproc.drawContours(imageFrame, contours, i, new Scalar(0, 0, 255));
//                        ArrayList<MatOfPoint> m = new ArrayList<MatOfPoint>() {
//                            {
//                                add(new MatOfPoint(approx.toArray()));
//                            }
//                        };
//                        Imgproc.drawContours(imageFrame, m, 0, new Scalar(0, 255, 0));
//                    }

                    if (approx.total() == 4 && area > minArea && area < maxArea && isConv) {
                        double maxCosine = 0;
                        final Point[] approxArr = approx.toArray();
                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angle(approxArr[j % 4], approxArr[j - 2], approxArr[j - 1]));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < tolerance) {
//                            ArrayList<MatOfPoint> m = new ArrayList<MatOfPoint>() {
//                                {
//                                    add(new MatOfPoint(approxArr));
//                                }
//                            };
//                            Imgproc.drawContours(imageFrame, m, 0, new Scalar(255, 0, 0));
                            squares.add(approx);
                        }
                    }
                }
            }
        }
        return squares;
    }

    public static Bitmap processToBW(Bitmap bitmap) {
        Mat imageMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(imageMat, imageMat, 5);
        Imgproc.adaptiveThreshold(imageMat, imageMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        Utils.matToBitmap(imageMat, bitmap);
        return bitmap;

    }
    public static Bitmap processToGray(Bitmap bitmap) {
        Mat imageMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, imageMat);
//        Mat gray0 = new Mat(imageMat.size(), CvType.CV_8UC(1));
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(imageMat, bitmap);
        return bitmap;

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
