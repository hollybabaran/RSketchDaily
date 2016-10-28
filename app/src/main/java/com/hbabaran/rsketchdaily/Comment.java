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
        try {
            //1 [..X..](/critique) and pull out image from Y
            Matcher markers = Pattern.compile(("^.*?\\[.*?(http[s]?://.*?)(\\s.*)?\\]\\s*?\\(/(critique|help|nostreak)\\).*\"$")).matcher(this.bodyText);
            //2 [..](X)
            Matcher link = Pattern.compile("^.*?\\[.*?\\]\\s*?\\((http[s]?://.*?)\\).*\"$").matcher(this.bodyText);
            //3 ..X..
            Matcher url = Pattern.compile("^.*?(http[s]?://.*?)[\\s\\)\\\\\"].*?$").matcher(this.bodyText);
            if(markers.matches()){
                imageURL = new URL(markers.group(1));
            }else if(link.matches()) {
                imageURL = new URL(link.group(1));
            } else if (url.matches()){
                imageURL = new URL(url.group(1));
            } else {
                throw new Exception("Couldn't find a link in the text");
            }
        } catch(Exception e) {
            System.err.println("Could not parse image URL from comments: " + e + "\nBodytext was:\n" + this.bodyText);
        }
        return imageURL;
    }

    String getCommentID(){
        return commentID;
    }
    Float getTime(){ return time; }


    public byte[] downloadThumbnailImage(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap image = PostLoader.getCommentImage(getImageURL());
        if(image != null){
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }


}
