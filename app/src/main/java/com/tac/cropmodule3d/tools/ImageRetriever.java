package com.tac.cropmodule3d.tools;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.tac.cropmodule3d.MediaAttachment;
import com.tac.cropmodule3d.PatinaFileProvider;
import com.tac.cropmodule3d.R;

import java.io.File;

/**
 * Created by kulik
 * Date: 19.01.15, 14:12
 */
public class ImageRetriever {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    private static final int SELECT_VIDEO_ACTIVITY_REQUEST_CODE = 102;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 103;
    private final Fragment mFragment;

    private Activity mActivity;
    private Uri mMediaUri;

    /**
     * @param activity
     * @param onResultFragment - if you want retrive result in activity, keep it null
     */
    public ImageRetriever(Activity activity, Fragment onResultFragment) {
        mActivity = activity;
        mFragment = onResultFragment;
    }

    public void getImageFromCamera() {
        // TODO Async
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File picsFile = PatinaFileProvider.getFileForImage();
        if (picsFile != null) {
            mMediaUri = Uri.fromFile(picsFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
            if (mFragment == null) {
                mActivity.startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            } else {
                mFragment.startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }

        } else {
            Toast.makeText(mActivity, R.string.sdcard_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    public void getVideoFromCamera() {
        // TODO Async
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File picsFile = PatinaFileProvider.getFileForVideo();
        if (picsFile != null) {
            mMediaUri = Uri.fromFile(picsFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
            if (mFragment == null) {
                mActivity.startActivityForResult(cameraIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
            } else {
                mFragment.startActivityForResult(cameraIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
            }

        } else {
            Toast.makeText(mActivity, R.string.sdcard_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    public void getImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");

        if (mFragment == null) {
            mActivity.startActivityForResult(photoPickerIntent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
        } else {
            mFragment.startActivityForResult(photoPickerIntent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
        }

    }

    public void getVideoFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("video/*");

        if (mFragment == null) {
            mActivity.startActivityForResult(photoPickerIntent, SELECT_VIDEO_ACTIVITY_REQUEST_CODE);
        } else {
            mFragment.startActivityForResult(photoPickerIntent, SELECT_VIDEO_ACTIVITY_REQUEST_CODE);
        }

    }


    public MediaAttachment processResultImage(int requestCode, int resultCode, Intent data) {
        File mediaFile = null;
        MediaAttachment attachment = null;
        if ((requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE)
                && (resultCode == Activity.RESULT_OK)
                && (mMediaUri != null)) {
            mediaFile = new File(mMediaUri.getPath());
            mMediaUri = null;
        }
        if ((requestCode == SELECT_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == SELECT_VIDEO_ACTIVITY_REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            if (selectedImage == null) {
                return null;
            }
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = mActivity.getContentResolver().query(
                    selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            if (filePath == null) {
                Toast.makeText(mActivity, R.string.msg_cant_attach_image, Toast.LENGTH_LONG).show();
                return null;
            }
            mediaFile = new File(filePath);
        }
        if (mediaFile == null) {
            if (resultCode != Activity.RESULT_CANCELED) {
                Toast.makeText(mActivity, R.string.msg_cant_attach_image, Toast.LENGTH_LONG).show();
            }
            return null;
        }
        attachment = MediaAttachment.fromFile(mediaFile);
        return attachment;
    }

    private boolean delete(String path) {
        File file = new File(path);
        return file.exists() && file.delete();
    }

    public boolean isMineRequestCode(int requestCode) {
        return requestCode == SELECT_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE ||
                requestCode == SELECT_VIDEO_ACTIVITY_REQUEST_CODE || requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE;
    }

    public static Bitmap getVideoThumbnail(String path) {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        return thumb;
    }
}
