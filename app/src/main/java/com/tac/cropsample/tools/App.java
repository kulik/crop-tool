package com.tac.cropsample.tools;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.tac.cropsample.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kulik on 15.02.15.
 */
public class App extends Application {
    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    public static File getStubDir() {
        return FileUtil.getCacheDir();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        copyStubFiles(sContext);
    }

    public static void copyStubFiles(Context ctx) {

        Resources res = ctx.getResources();
        try {
            FileUtil.copyFile(res.openRawResource(R.raw.img1), new FileOutputStream(new File(getStubDir(), "img1.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img2), new FileOutputStream(new File(getStubDir(), "img2.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img3), new FileOutputStream(new File(getStubDir(), "img3.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img4), new FileOutputStream(new File(getStubDir(), "img4.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img5), new FileOutputStream(new File(getStubDir(), "img5.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img6), new FileOutputStream(new File(getStubDir(), "img6.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img7), new FileOutputStream(new File(getStubDir(), "img7.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img8), new FileOutputStream(new File(getStubDir(), "img8.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img9), new FileOutputStream(new File(getStubDir(), "img9.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img10), new FileOutputStream(new File(getStubDir(), "img10.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img11), new FileOutputStream(new File(getStubDir(), "img11.jpg")));
            FileUtil.copyFile(res.openRawResource(R.raw.img12), new FileOutputStream(new File(getStubDir(), "img12.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
