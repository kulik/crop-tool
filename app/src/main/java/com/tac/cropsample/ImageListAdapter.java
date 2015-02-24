package com.tac.cropsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by kulik on 16.02.15.
 */
public class ImageListAdapter extends ArrayAdapter<String> {

    private final String[] mArray;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;

    public ImageListAdapter(Context context, String[] array) {
        super(context, R.layout.image_layout);
        mArray  = array;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageLoader = ImageLoader.getInstance();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();

        mImageLoader.init(config);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view;

        if (convertView == null) {
            view = (ImageView) mInflater.inflate(R.layout.image_layout, parent, false);
        } else {
            view = (ImageView) convertView;
        }
//        view.setImageResource(mArray[position]);
        mImageLoader.displayImage(mArray[position],view);
        return view;
    }


    @Override
    public int getCount() {
        return mArray.length;
    }
}
