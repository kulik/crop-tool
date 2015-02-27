package com.tac.cropmodule;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tac.cropmodule.tools.ImageUtil;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kulik on 17.02.15.
 */
public class SCropImageView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = SCropImageView.class.getSimpleName();

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "some error");
            // Handle initialization error
        }
    }

    private static final String URI_KEY = "URI_KEY";
    private com.tac.cropmodule.CroppingTread mThread;
    private Handler mHandler;
    private Looper mServiceLooper;
    private CropViewListener mCropListener;
    private PinsSettingListener mPinsListener;
    private CropViewLoadListener mLoadListener;
    private String mUri;
    private Configuration mConf;

    public SCropImageView(Context context) {
        super(context);
        init(context, null);
    }

    public SCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SCropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SCropImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    private void init(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CropView,
                0, 0
        );

        Configuration conf = new Configuration();
        try {
            // Retrieve the values from the TypedArray and store into
            // fields of this class.
            conf.pinDrawable = a.getDrawable(R.styleable.CropView_crPinDrawable);
            conf.lineColor = a.getColor(R.styleable.CropView_crLineColor, getColor(android.R.color.holo_red_dark));
            conf.lineThinknes = a.getDimension(R.styleable.CropView_crLineThinkess, 2f);
            conf.pinTextColor = a.getColor(R.styleable.CropView_crTextColor, getColor(android.R.color.white));
            conf.backgroundColor = a.getColor(R.styleable.CropView_crBackgroundColor, getColor(android.R.color.white));
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }
        mThread = new CroppingTread(conf);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        HandlerThread thread = new HandlerThread("CropView[" + this + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mHandler = new Handler(mServiceLooper);
        mConf = conf;
    }

    private int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread.setHolder(holder);
        setOnTouchListener(mThread);
        mThread.setRunning(true);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mThread.setDimentions(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        mThread.setRunning(false);
        while (retry) {
            try {
                mThread.join();
                mThread = new CroppingTread(mConf);
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "Attached");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "Detached");
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        Log.i(TAG, "SavedState");
        Bundle state = null;
        if (mThread != null) {
            state = new Bundle();
            mThread.onSaveInstanceState(state);
            if (mUri != null) {
                state.putString(URI_KEY, mUri);
            }
        }
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(null);
        Log.i(TAG, "RestoreState");
        if (state != null && state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            String uri = bundle.getString(URI_KEY, null);
            if (uri != null) {
                mUri = uri;
                loadPrivate(uri, true, true);
            }
            mThread.onRestoreInstanceState(bundle);
        }
    }

    /**
     * Start cropping assinchronusl
     *
     * @param fileToSave
     */
    public void cropInBackgroung(final File fileToSave) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap b = mThread.crop();
                try {
                    fileToSave.createNewFile();
                    FileOutputStream ostream = new FileOutputStream(fileToSave);
                    b.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                    ostream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                pow2less();
                //TODO save bitmap to file
                onCropped(fileToSave);
            }
        });
    }

    private void onCropped(final File tmpFile) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mCropListener != null) {
                    mCropListener.onImageCropped(tmpFile);
                }
            }
        });
    }

    private void onPinsSettedUp() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mPinsListener != null) {
                    mPinsListener.onPinsSettedUp();
                }
            }
        });
    }

    private void onImageLoaded(final boolean successful) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mLoadListener != null) {
                    mLoadListener.onImageLoaded(successful);
                }
            }
        });
    }

    public void setupAutoPins() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mThread.setupAutoPins();
                onPinsSettedUp();
            }
        });
    }

    public void loadUri(final String uri, boolean setupAutopins) {
        loadPrivate(uri, setupAutopins, false);
    }

    private void loadPrivate(final String uri, final boolean setupAutopins, final boolean innerCall) {
        mUri = uri;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
//                    Bitmap b = ImageUtil.resizeFitMin(new File(Uri.parse(uri).getPath()), null, 2560, 2560);
                    Bitmap b = ImageUtil.resizeFitMin(new File(Uri.parse(uri).getPath()), null, 1200, 1200);
                    setBitmap(b);
                    if (!innerCall) {
                        onImageLoaded(true);
                    }
                    if (setupAutopins) {
                        setupAutoPins();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                    if (!innerCall) {
                        onImageLoaded(false);
                    }
                }
            }
        });
    }

    public void setBitmap(Bitmap bitmap) {
        mThread.setBitmapToCrop(bitmap);
    }

    public void setCropListener(CropViewListener cropListener) {
        mCropListener = cropListener;
    }

    public void setLoadListener(CropViewLoadListener loadListener) {
        mLoadListener = loadListener;
    }

    public void setPinsListener(PinsSettingListener pinsListener) {
        mPinsListener = pinsListener;
    }

    public static interface CropViewListener {
        void onImageCropped(File tmpFile);
    }

    public static interface CropViewLoadListener {
        void onImageLoaded(boolean successful);
    }

    public static interface PinsSettingListener {
        void onPinsSettedUp();
    }
}
