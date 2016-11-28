package com.hbabaran.rsketchdaily.Helper;

import android.graphics.Bitmap;

import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.PhotoPost;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hbabaran.rsketchdaily.Helper.AuthConstants.IMGUR_CLIENT_ID;
import static com.hbabaran.rsketchdaily.Helper.AuthConstants.TUMBLR_OAUTH_CONSUMER_KEY;
import static com.hbabaran.rsketchdaily.Helper.AuthConstants.TUMBLR_SECRET_KEY;

/**
 * Created by wren on 28/10/2016.
 */

public class CommentLoader {

    public static Bitmap getCommentImage(URL url){
        if(url != null) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            return imageLoader.loadImageSync(url.toString());
        }
        return null;
    }

    public static URL queryTumblrURL(URL tumblrLink) throws MalformedURLException{
        Matcher tumblrPost = Pattern.compile("http[s]?://(.*?\\.tumblr\\.com)/post/([0-9]+)(/.*)?$").matcher(tumblrLink.toString());
        //TODO parse format http://<blog-id>/image\<post-id>
        if(tumblrPost.matches()) {
            JumblrClient client = new JumblrClient(TUMBLR_OAUTH_CONSUMER_KEY, TUMBLR_SECRET_KEY);
            PhotoPost post = (PhotoPost) client.blogPost(tumblrPost.group(1), Long.valueOf(tumblrPost.group(2)));
            return new URL(post.getPhotos().get(0).getOriginalSize().getUrl());
        }
        System.err.println("Couldn't decode tumblr link: " + tumblrLink.toString());
        return null;
    }

    public static URL queryImgurURL(URL link) throws MalformedURLException, IOException{
        Matcher image = Pattern.compile("http[s]?://([m\\.]*?)imgur\\.com/([a-zA-z0-9]+)[_[0-9]+x[0-9]+]?").matcher(link.toString());
        Matcher album = Pattern.compile("http[s]?://([m\\.]*?)imgur\\.com/(a|gallery)/([a-zA-z0-9]+)").matcher(link.toString());
        //TODO match gallery
        JsonObject imageInfo;
        String imgURL;
        if(image.matches()) {
            imageInfo = PostLoader.downloadJson(new URL("https://" + IMGUR_CLIENT_ID + "@api.imgur.com/3/image/" + image.group(2)), "Authorization", "Client-ID "+IMGUR_CLIENT_ID).getAsJsonObject();
            imgURL = imageInfo.getAsJsonObject("data")
                                .get("link")
                                .toString().replaceAll("\\\\","").replaceAll("\"","");
            return new URL(imgURL);
        } else if(album.matches()){
            imageInfo = PostLoader.downloadJson(new URL("https://" + IMGUR_CLIENT_ID + "@api.imgur.com/3/album/" + album.group(3)), "Authorization", "Client-ID "+IMGUR_CLIENT_ID).getAsJsonObject();
            imgURL = imageInfo.getAsJsonObject("data")
                                .getAsJsonArray("images")
                                .get(0).getAsJsonObject()
                                .get("link")
                                .toString().replaceAll("\\\\","").replaceAll("\"","");
            return new URL(imgURL);
        }
        System.err.println("Couldn't decode imgur link: " + link.toString());
        return null;
    }

}
