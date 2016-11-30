package com.hbabaran.rsketchdaily.Model;

import android.graphics.Bitmap;

import com.google.gson.JsonObject;
import com.hbabaran.rsketchdaily.Helper.CommentLoader;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wren on 19/10/2016.
 */

public class Comment { //TODO make this abstract and differentiate ImageComment vs ChildComment
    private String commentID;
    private float time; //time comment was posted
    private String bodyText;

    private URL imageURL;
    private Boolean parsedURL;

    public enum LinkType{
        Direct,
        Tumblr,
        Insta,
        ImgurAlbum,
        DeviantArt,
        Other
    }
    private LinkType linkType;

    //constructor does NOT download the image or anything else
    public Comment(JsonObject comment){
        this.commentID = "";
        this.time = 0;
        this.imageURL = null;
        this.parsedURL = false;
        this.bodyText = "";
        this.linkType = null;
        if (comment != null) {
            JsonObject data = comment.getAsJsonObject("data");
            this.commentID = data.get("id").toString().replace("\"","");
            this.time = Float.parseFloat(data.get("created").toString());
            this.bodyText = data.get("body").toString();
        }
    }

    //blocking-- may query tumblr/insta/other sites for the bare image url
    public URL getImageURL(){
        if(!this.parsedURL){
            this.parsedURL = true;
            this.imageURL = parseCommentURL();
            if(this.imageURL != null) {
                Matcher directImageLink = Pattern.compile(".*?\\.(jpg|jpeg|png|bmp|gif)$").matcher(this.imageURL.toString());
                if (!directImageLink.matches()) {
                    try {
                        if (this.imageURL.toString().contains("tumblr")) {
                            this.linkType = LinkType.Tumblr;
                            this.imageURL = CommentLoader.queryTumblrURL(this.imageURL);
                        } else if (this.imageURL.toString().contains("imgur")){
                            this.linkType = LinkType.ImgurAlbum;
                            this.imageURL = CommentLoader.queryImgurURL(this.imageURL);
                        }
                    } catch (Exception e) {
                        System.err.println("Could not refine comment link to image URL: " + this.imageURL + "\n" + e);
                    }
                } else {
                    this.linkType = LinkType.Direct;
                }
            }
        }
        return this.imageURL;
    }

    private URL parseCommentURL(){
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
                throw new Exception("Couldn't find a link in the reddit comment text");
            }
        } catch(Exception e) {
            System.err.println("Could not parse URL from comments: " + e + "\nBodytext was:\n" + this.bodyText);
        }
        return imageURL;
    }

    public String getCommentID(){
        return commentID;
    }
    Float getTime(){ return time; }


    public byte[] downloadThumbnailImage(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap image = null;
        image = CommentLoader.getCommentImage(getImageURL());
        if(image != null){
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }


}
