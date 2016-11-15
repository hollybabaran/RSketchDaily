package com.hbabaran.rsketchdaily.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.hbabaran.rsketchdaily.Activity.GalleryActivity;
import com.hbabaran.rsketchdaily.Model.Comment;
import com.hbabaran.rsketchdaily.Model.Date;
import com.hbabaran.rsketchdaily.Helper.PostLoader;
import com.hbabaran.rsketchdaily.Model.Post;

import java.net.URL;
import java.util.ArrayList;

import static java.lang.String.valueOf;

/**
 * Created by wren on 17/10/2016.
 */

public class GalleryService extends IntentService {
    private static final String TAG = "GalleryService";
    public static final String SEND_GALLERY_INFO = "com.hbabaran.rsketchdaily.sendgalleryinfo";
    private Intent intent;
    Post post; //TODO make this multiple posts.

    public GalleryService(){
        super("GalleryService");
        this.post = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver rec = intent.getExtras().getParcelable(GalleryActivity.GALLERY_RECEIVER_TAG);
        Bundle postinfo = new Bundle();

        //check if the post requested is the post we have already loaded; if not load it
        Date date = new Date(intent.getExtras().getLong("date"));
        if (this.post == null ||
                this.post.getDate().toPrimitive() != date.toPrimitive()) {
            this.post = PostLoader.getPostByDate(date);
        }

        postinfo.putString("title", post.getTitle());
        postinfo.putLong("date", post.getDate().toPrimitive());
        if( post.getPostURL() != null) {
            postinfo.putString("url", post.getPostURL().toString()); //TODO self text
        }
        rec.send(GalleryActivity.POST_LOADED, postinfo);

        this.post.updateComments();
        if (this.post.getComments() != null) {
            ArrayList<Comment> comments = this.post.getComments();
            //send number of comments so gallery activity can populate spinwheels
            Bundle commentCount = new Bundle();
            commentCount.putInt("count", comments.size());
            rec.send(GalleryActivity.COMMENT_COUNT, commentCount);
            //send comments
            //TODO consider making a comment serializable... especially if you want to convert back into a comment and pass the comment directly to the GalleryCommentGridImageAdapter
            for (int i = 0; i < comments.size(); i++) {
                Bundle commentInfo = new Bundle();
                commentInfo.putInt("position", i);
                URL url = comments.get(i).getImageURL();
                if(url != null) commentInfo.putString("URL", url.toString());
                byte[] img = comments.get(i).downloadThumbnailImage();
                if(img != null) commentInfo.putByteArray("img", img);

                rec.send(GalleryActivity.COMMENT_LOADED, commentInfo);
            }
        } else {
            System.err.println("Could not load comments!");
        }
    }
}
