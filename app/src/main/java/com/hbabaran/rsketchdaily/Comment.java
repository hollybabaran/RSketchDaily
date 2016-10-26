package com.hbabaran.rsketchdaily;

import android.graphics.Bitmap;

import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wren on 19/10/2016.
 */

public class Comment { //TODO make this abstract and differentiate ImageComment vs ChildComment
    private String commentID;
    private String id;
    private float time; //time comment was posted
    private String bodyText;

    private URL imageURL;

    //constructor does NOT download the image or anything else
    public Comment(JsonObject comment){
        this.commentID = "";
        this.time = 0;
        this.imageURL = null;
        this.bodyText = "";
        if (comment != null) {
            JsonObject data = comment.getAsJsonObject("data");
            this.commentID = data.get("id").toString();
            this.time = Float.parseFloat(data.get("created").toString());
            this.bodyText = data.get("body").toString();
            this.imageURL = parseImageURL(); //TODO parse comment text for image url
        }
    }


    public URL getImageURL(){
        if(this.imageURL == null){
            this.imageURL = parseImageURL();
        }
        return this.imageURL;
    }

    private URL parseImageURL(){
        URL imageURL = null;
        try{
            //algorithm 1: look for the first instance of [X](Y) and pull out image from Y
            Pattern p = Pattern.compile("^.*?\\[.*?\\]\\s*?\\((.*?)\\).*$");
            Matcher m = p.matcher(this.bodyText);
            if(m.matches()) {
                imageURL = new URL(m.group(1));
            } else {
                throw new Exception("Couldn't find a link in the text, text was \n" + this.bodyText);
            }
        } catch(Exception e) {
            System.err.println("Could not parse image URL from comments: " + e);
        }
        return imageURL;
    }

    String getCommentID(){
        return commentID;
    }
    Float getTime(){ return time; }

}
