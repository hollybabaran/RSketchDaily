package com.hbabaran.rsketchdaily;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.valueOf;

/**
 * Created by wren on 17/10/2016.
 */

class PostCacheService extends IntentService {
    private static final String TAG = "PostCacheService";
    public static final String SEND_GALLERY_INFO = "com.hbabaran.rsketchdaily.sendgalleryinfo";
    private Intent intent;
    Post post; //TODO make this multiple posts.

    public PostCacheService(){
        super("PostCacheService");
        this.post = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver rec = intent.getExtras().getParcelable(GalleryActivity.GALLERY_RECEIVER_TAG);
        Bundle postinfo =new Bundle();

        Date date = new Date(intent.getExtras().getLong("date"));
        if(this.post == null ||
                this.post.getDate().toPrimitive() != date.toPrimitive()){
            this.post = PostLoader.getPostByDate(date);
        }

        postinfo.putString("title", post.getTitle()); //TODO self text
        rec.send(0, postinfo);

        if(this.post.getComments().isEmpty()){
            //load the comments and send each one as loaded 
        } else {
            for (Comment comment: this.post.getComments()) {
                Bundle commentInfo = new Bundle();
                commentInfo.putByteArray("image", comment.getImgByteArray()); //TODO send other info
                rec.send(1, commentInfo);
            }
        }
    }


}
