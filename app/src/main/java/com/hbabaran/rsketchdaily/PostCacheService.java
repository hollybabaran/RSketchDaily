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

    Post post; //TODO make this multiple posts

    public PostCacheService(){
        super("PostCacheService");
        this.post = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("received request");
        Date date = new Date(intent.getExtras().getLong("date"));
        if(this.post == null ||
                this.post.getDate().toPrimitive() != date.toPrimitive()){
            this.post = new Post(date, getPostByDate(date));
        }
        Bundle postinfo =new Bundle();
        postinfo.putString("title", post.getTitle()); //TODO populate postinfo with info from Post... TODO figure out how to handle eg images??
        System.out.println("sending info");
        ResultReceiver rec = intent.getExtras().getParcelable(GalleryActivity.GALLERY_RECEIVER_TAG);
        rec.send(0, postinfo);
    }

    //TODO figure out what happens if there are two posts on one day (eg someone made a sticky) and handle that case
    //perhaps ensure that the url list is sorted by date and then get the first one (posted at 3am typically)
    private JSONObject getPostByDate(Date date){
        URL url;
        String redditJSONStr;
        JSONObject frontJson;
        JSONObject post;
        try {
            url = buildPostURLByDate(date);
            redditJSONStr = getJSONStr(url);
            frontJson = new JSONObject(redditJSONStr);
            post = frontJson.getJSONObject("data").getJSONArray("children").getJSONObject(0); //TODO here
        } catch (MalformedURLException e){
            System.err.println(e);
            return null;
        } catch(IOException e) {
            System.err.println(e);
            return null;
        } catch(JSONException e) {
            System.err.println(e);
            return null;
        }
        return post;
    }

    private URL buildPostURLByDate(Date date) throws MalformedURLException{
        //TODO pull out this hardcoding
        String url_prefix = "https://www.reddit.com/r/SketchDaily/search.json?q=timestamp%3A";
        String url_midfix = "..";
        String url_suffix = "&sort=new&restrict_sr=on&syntax=cloudsearch";

        String url = url_prefix +
                date.getUnix_mintime() +
                url_midfix +
                date.getUnix_maxtime() +
                url_suffix;

        System.out.println("Loading " + url);
        return new URL(url);
    }

    private String getJSONStr(URL url) throws IOException{
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) url.openConnection();
            c.connect();
            int status = c.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
                default:
                    throw new IOException("Internet Error: " + valueOf(status));
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }

}
