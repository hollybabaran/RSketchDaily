package com.hbabaran.rsketchdaily;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
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

public class GalleryImageAdapter extends BaseAdapter {
    private Context mContext;
    private int commentCount;
    //"drawable://" +
    private static final int DEFAULT_IMAGE_ERROR =  R.drawable.ic_menu_close_clear_cancel; //TODO get a non-chihuahua default error image
    private static final int DEFAULT_IMAGE_LOADING = R.drawable.spinner_white_48; //TODO get a non-chihuahua default loading image
    private Bitmap IMAGE_ERROR;
    private Bitmap IMAGE_LOADING;
    private Bitmap[] images;

    public GalleryImageAdapter(Context context, int commentCount) {
        mContext = context;
        this.commentCount = commentCount;
        images = new Bitmap[this.commentCount];

        IMAGE_ERROR = BitmapFactory.decodeResource(mContext.getResources(), DEFAULT_IMAGE_ERROR);
        IMAGE_LOADING = BitmapFactory.decodeResource(mContext.getResources(), DEFAULT_IMAGE_LOADING);
    }

    public void setCommentImage(int position, byte[] img){
        //TODO handle URLs with % (throws a FileNotFound error)
        /*if(url != null || url.contains("%")) {
            images[position] = url;
            System.out.println("Setting position " + position + " with url "+ url);
        } else {
            images[position] = DEFAULT_IMAGE_ERROR;
        }*/
        if(img != null){
            images[position] = BitmapFactory.decodeByteArray(img, 0, img.length);
        } else {
            images[position] = IMAGE_ERROR;
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
            imageView.setLayoutParams(new GridView.LayoutParams(400, 380));
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setBackgroundResource(R.drawable.ab_stacked_solid_inverse_holo);
        if(images[position] != null) {
            imageView.setImageBitmap(images[position]);
        } else {
            imageView.setImageBitmap(IMAGE_LOADING);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setElevation(10);
        }


        return imageView;
    }


}