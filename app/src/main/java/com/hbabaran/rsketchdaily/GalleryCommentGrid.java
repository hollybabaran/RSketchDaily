package com.hbabaran.rsketchdaily;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by wren on 10/14/2016.
 */

public class GalleryCommentGrid extends BaseAdapter {
    private Context mContext;
    private int commentCount;
    private static final String DEFAULT_IMAGE_ERROR = "drawable://" + R.drawable.sample_2; //TODO get a non-chihuahua default error image
    private static final String DEFAULT_IMAGE_LOADING = "drawable://" + R.drawable.sample_7; //TODO get a non-chihuahua default loading image
    private String[] images;

    public GalleryCommentGrid(Context context, int commentCount) {
        mContext = context;
        this.commentCount = commentCount;
        images = new String[this.commentCount];
        for(int i = 0; i < this.commentCount; i++){
            images[i] = DEFAULT_IMAGE_LOADING;
        }
    }

    public void setCommentImageURL(int position, String url){
        //TODO render byte[] image as a bitmap, save it in images[] position
        //if image is null then load the error image instead
        if(url != null) {
            images[position] = url;
        } else {
            images[position] = DEFAULT_IMAGE_ERROR;
        }
    }

    public int getCount() {
        return this.commentCount;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(images[position], imageView);
        return imageView;
    }


}