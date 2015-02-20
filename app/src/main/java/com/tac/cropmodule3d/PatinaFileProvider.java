package com.tac.cropmodule3d;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kulik on 18.01.15.
 */
public class PatinaFileProvider {

    public static final String TAG = PatinaFileProvider.class.getSimpleName();

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
