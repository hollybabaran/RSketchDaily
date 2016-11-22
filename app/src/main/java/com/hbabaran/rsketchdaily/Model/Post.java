package com.hbabaran.rsketchdaily.Model;

import com.hbabaran.rsketchdaily.Helper.PostLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by wren on 17/10/2016.
 */

public class Post {
    public enum SortMethod{
        NEW,
        RANDOM,
        TOP
    }

    private String title;
    private String selftext;
    private Date date; //NOT the direct Unix "date" in the object.
    private URL postURL;
    private String id;
    ArrayList<Comment> comments;
    private SortMethod sortMethod;


    public Post(Date date, JSONObject post){
        this.sortMethod = SortMethod.NEW; //TODO allow for user option for sorting method
        this.date = date;
        this.title = "Error loading post";
        this.selftext = "Error loading post";
        this.postURL = null;
        this.comments = new ArrayList<Comment>();
        if (post != null) {
            try {
                this.title = post.getJSONObject("data").getString("title");
                this.selftext = post.getJSONObject("data").getString("selftext");
                this.id = post.getJSONObject("data").getString("id");
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
    public URL getPostURL() {
        return postURL;
    }
    public String getID() {
        return this.id;
    }




    //TODO: some sort of caching of the comments / "updatecomments"
    //For now updateComments just loads them all anew
    public void updateComments(){
        this.comments = PostLoader.parseCommentsFromPost(getPostURL());
        if(this.comments!= null) sortComments();
    }

    private class SortByNew implements Comparator<Comment> {
        @Override
        public int compare(Comment c1, Comment c2) {
            return -(c1.getTime().compareTo(c2.getTime()));
        }
    }

    public void sortComments(){
        Comparator<Comment> sorter;
        switch(this.sortMethod){
            case NEW: sorter = new SortByNew(); break;
            //TODO other cases
            default: sorter = new SortByNew();
        }
        Collections.sort(this.comments, sorter);
    }

    public ArrayList<Comment> getComments(){
        return this.comments;
    }


}
