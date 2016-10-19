package com.hbabaran.rsketchdaily;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.net.URL;

/**
 * Created by wren on 19/10/2016.
 */

public class Comment { //TODO make this abstract and differentiate ImageComment vs ChildComment
    Bitmap image;
    URL imageURL;

    String id;

    byte[] getImgByteArray(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

}
