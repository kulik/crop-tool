package com.tac.cropsample;

import com.tac.cropsample.tools.FileUtil;

import java.io.File;

/**
 * Created by kulik on 18.01.15.
 */
public class AppFileProvider {

    public static final String TAG = AppFileProvider.class.getSimpleName();

    public static final String VIDEO_ATTACH = ".mp4";
    public static final String IMAGE_ATTACH = ".iattach";

    public static File getFileForImage() {
        final File cacheDir = FileUtil.getAppFilesDir(".photos");
        File file = new File(cacheDir, System.currentTimeMillis() + IMAGE_ATTACH);
        return file;
    }

    public static File getFileForVideo() {
        final File cacheDir = FileUtil.getAppFilesDir(".video");
        File file = new File(cacheDir, System.currentTimeMillis() + VIDEO_ATTACH);
        return file;
    }

}
