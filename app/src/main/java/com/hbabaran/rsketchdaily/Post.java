package com.hbabaran.rsketchdaily;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by wren on 17/10/2016.
 */

class Post {
    private String title;
    private String selftext;
    private Date date; //NOT the direct Unix "date" in the object.
    private URL postURL;
    ArrayList<Comment> comments;


    public Post(Date date, JSONObject post){
        this.date = date;
        this.title = "Error loading post";
        this.selftext = "Error loading post";
        this.postURL = null;
        this.comments = new ArrayList<Comment>();
        if (post != null) {
            try {
                this.title = post.getJSONObject("data").getString("title");
                this.selftext = post.getJSONObject("data").getString("selftext");
                this.postURL = new URL(post.getJSONObject("data").getString("url").replaceAll("\\\\",""));
            } catch (JSONException e) {
                System.err.println("error parsing post JSON: " + e);
            } catch (MalformedURLException e) {
                System.err.println("error parsing post URL: " + e);
            }
        }
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getSelftext() {
        return selftext;
    }
    public void setSelftext(String selftext) {
        this.selftext = selftext;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public URL getPostURL() {
        return postURL;
    }
    public void setPostURL(URL postURL) {
        this.postURL = postURL;
    }

    public void addComment(Comment comment){
        this.comments.add(comment);
    }
    public ArrayList<Comment> getComments(){
        return this.comments;
    }
}
