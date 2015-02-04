package com.tac.cropmodule3d;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;


public class MainActivity extends Activity {


    private CropImageView mImageView;
    private Button mCrop;
    private Button mCorners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (CropImageView) findViewById(R.id.img);
        mCrop = (Button) findViewById(R.id.btn_do);
        mCrop.setEnabled(false);
        mCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crop();
            }
        });

        mCorners = (Button) findViewById(R.id.btn_corners);
        mCorners.setEnabled(false);
        mCorners.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                corners();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mCrop.setEnabled(true);
                    mCorners.setEnabled(true);

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void crop() {
        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        //get original image Mat;
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);

        Bitmap output = TransformationEngine.get().initArea(mImageView.pin1(), mImageView.pin2(), mImageView.pin3(), mImageView.pin4())
                .transform(imageMat);

        mImageView.setImageBitmap(output);
    }

    private void corners() {
        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        //get original image Mat;
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);
        Mat corners = new TransformationEngine().findCorners(imageMat);
        Utils.matToBitmap(corners, bitmap);
        mImageView.setImageBitmap(bitmap);
    }
}
