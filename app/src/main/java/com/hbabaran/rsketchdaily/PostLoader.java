package com.hbabaran.rsketchdaily;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static java.lang.String.valueOf;

/**
 * Created by wren on 10/16/2016.
 */

public class PostLoader extends AsyncTask<Void, String, String> {

    private Date date;
    private Intent sendInfo;

    public PostLoader(Date date, Post post){
        this.date = date;
    }

    protected void onPreExecute() {
        //gallery.setActionBarText(gallery.getResources().getString(R.string.loading));
    }

    public String doInBackground(Void... v) { //TODO use "date" to get post data per date
        return getPostTitle();
    }

    protected void onPostExecute(String result) {
        //sendInfo = new Intent()
    }


    private String getPostTitle(){
        String title = "Error parsing title";
        JSONObject post = getPostByDate();
        if (post != null) {
            try {
                title = post.getJSONObject("data").getString("title");
            } catch (JSONException e) {
                System.err.println("error parsing post title: " + e);
            }
        }
        return title;
    }

    private JSONObject getPostByDate(){
        URL url;
        String redditJSONStr;
        JSONObject frontJson;
        JSONObject post;
        try {
            url = buildPostURLByDate();
            redditJSONStr = getJSONStr(url);
            frontJson = new JSONObject(redditJSONStr);
            post = frontJson.getJSONObject("data").getJSONArray("children").getJSONObject(0);
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

    private URL buildPostURLByDate() throws MalformedURLException{
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