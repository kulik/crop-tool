package com.tac.cropmodule3d;

import android.net.Uri;

import java.io.File;

/**
 * Created by kulik on 18.01.15.
 */
public class MediaAttachment {
    private final String mPath;

    public MediaAttachment(String path) {
        mPath = path;
    }

    public Uri getUri() {
        return Uri.fromFile(new File(mPath));
    }

    public File getFile() { return new File(mPath);}


    public static MediaAttachment fromFile(File imageFile) {
        return new MediaAttachment(imageFile.getPath());
    }


}
