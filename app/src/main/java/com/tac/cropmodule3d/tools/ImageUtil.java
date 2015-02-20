package com.tac.cropmodule3d.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kulik on 18.02.15.
 */
public class ImageUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();

    private static BitmapFactory.Options getBitmapOption(Context ctx, Uri uri, int maxImageSize) {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        ParcelFileDescriptor descriptor = null;
        try {
            descriptor = ctx.getContentResolver().openFileDescriptor(uri, "r");
            BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor(), null, bitmapOptions);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "", e);
        }
        bitmapOptions.inDither = false;
        bitmapOptions.inSampleSize = calculateInSampleSize(bitmapOptions, maxImageSize, maxImageSize);
        bitmapOptions.inTempStorage = new byte[32 * 1024];
        bitmapOptions.inJustDecodeBounds = false;
        return bitmapOptions;
    }
//
//    private enum ScalingLogic {
//        CROP, FIT
//    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    || (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap getBitmap(Context ctx, Uri originalUri, int maxImageSize) throws FileNotFoundException {
        BitmapFactory.Options bitmapOptions = getBitmapOption(ctx, originalUri, maxImageSize);
        Bitmap selectedBitmap = null;
        ParcelFileDescriptor descriptor = ctx.getContentResolver().openFileDescriptor(originalUri, "r");
        selectedBitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor(), null, bitmapOptions);
        return selectedBitmap;
    }


    public static Bitmap resizeFitMin(File in, File out, int newW, int newH) throws IOException {
//        Bitmap bmpOriginal = BitmapFactory.decodeFile(in.getPath());

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        ParcelFileDescriptor descriptor = null;
        BitmapFactory.decodeFile(in.getPath(), bitmapOptions);
        bitmapOptions.inDither = false;
        int width, height;

        height = bitmapOptions.outHeight;
        width = bitmapOptions.outWidth;
        float scale = Math.min((float) newH / height, (float) newW / width);
        int pow2 = (int) (1 / scale);
//        TODO fix power of 2
        bitmapOptions.inSampleSize = pow2;

        bitmapOptions.inTempStorage = new byte[32 * 1024];
        bitmapOptions.inJustDecodeBounds = false;

        Bitmap bmpOriginal = BitmapFactory.decodeFile(in.getPath(), bitmapOptions);

        Matrix m = new Matrix();
        m.setScale(scale, scale);
//        m.postTranslate(-(width * scale - newW) / 2, -(height * scale - newH) / 2);
        Bitmap b = Bitmap.createBitmap((int) (width*scale), (int)(height*scale), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);
        c.drawBitmap(bmpOriginal, m, new Paint());

        bmpOriginal.recycle();
        if (out != null) {
            try {
                out.createNewFile();
                FileOutputStream ostream = new FileOutputStream(out);
                b.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.close();
                b.recycle();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return b;
    }
}