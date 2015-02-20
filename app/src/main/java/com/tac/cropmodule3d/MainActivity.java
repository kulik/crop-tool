package com.tac.cropmodule3d;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tac.cropmodule3d.tools.ImageRetriever;
import com.tac.cropmodule3d.tools.ImageUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "some error");
            // Handle initialization error
        }
    }

    private SCropImageView mImageView;
    private Button mCrop;
    private ImageRetriever mMediaRetriver;
    private ImageLoader mImageLoader;
    private String[] mArray = new String[]{
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img1.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img2.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img3.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img4.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img5.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img6.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img7.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img8.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img9.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img10.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img11.jpg")).toString(),
            Uri.fromFile(new File(FileUtil.getCacheDir() + "/img12.jpg")).toString(),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaRetriver = new ImageRetriever(this, null);

        mImageLoader = ImageLoader.getInstance();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();

        mImageLoader.init(config);


//        mImageView = (CropImageView) findViewById(R.id.img);
        mImageView = (SCropImageView) findViewById(R.id.img1);
        mCrop = (Button) findViewById(R.id.btn_do);
//        mCrop.setEnabled(false);
        mCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.crop();
            }
        });

        Button v = (Button) findViewById(R.id.btn_corners);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setupAutoPins();
            }
        });

        findViewById(R.id.btn_bw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bw();
            }
        });

        findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaRetriver.getImageFromCamera();
            }
        });
        findViewById(R.id.galery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaRetriver.getImageFromGallery();
            }
        });
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(new ImageListAdapter(this, mArray));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mImageLoader.loadImage(mArray[position], new SimpleImageLoadingListener() {
//                    @Override
//                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                        super.onLoadingComplete(imageUri, view, loadedImage);
//                        mImageView.setBitmap(loadedImage);
//                    }
//                });
                mImageView.loadUri(mArray[position]);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mMediaRetriver.isMineRequestCode(requestCode)) {
            MediaAttachment ma = mMediaRetriver.processResultImage(requestCode, resultCode, data);
//            mImageLoader.displayImage(ma.getUri().toString(), mImageView);
//            Bitmap icon = BitmapFactory.decodeFile(ma.getFile().getPath());
//            mImageView.setBitmap(icon);
            mImageView.loadUri(ma.getFile().getPath());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

//    private void crop() {
//        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
//        //get original image Mat;
//        Mat imageMat = new Mat();
//        Utils.bitmapToMat(bitmap, imageMat);
//
//        Bitmap output = TransformationEngine.get().initArea(mImageView.pin1().point(), mImageView.pin2().point(), mImageView.pin3().point(), mImageView.pin4().point())
//                .transform(imageMat);
//
//        mImageView.setImageBitmap(output);
//    }

    private void bw() {
//        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
//        //get original image Mat;
//        Bitmap output = TransformationEngine.get().processToBW(bitmap);
//
//        mImageView.setImageBitmap(output);
    }


}
