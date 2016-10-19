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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static java.lang.String.valueOf;

/**
 * Created by wren on 10/16/2016.
 */

public class PostLoader{

    public static Post getPostByDate(Date date){
        return new Post(date, getPostJSONByDate(date));
    }

    //TODO figure out what happens if there are two posts on one day (eg someone made a sticky) and handle that case
    //perhaps ensure that the url list is sorted by date and then get the first one (posted at 3am typically)
    private static JSONObject getPostJSONByDate(Date date){
        URL url;
        String redditJSONStr;
        JSONObject frontJson;
        JSONObject post;
        try {
            url = buildPostURLByDate(date);
            redditJSONStr = downloadJSONStr(url);
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

    private static URL buildPostURLByDate(Date date) throws MalformedURLException{
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

    private static String downloadJSONStr(URL url) throws IOException{
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

    public static ArrayList<Comment> parseCommentsFromPost(URL postUrl){
        String redditJSONStr;
        JSONObject postJson;
        try {
            redditJSONStr = downloadJSONStr(new URL(postUrl + ".json"));
            postJson = new JSONObject(redditJSONStr);
            //TODO json stream parsing the postJSON for comments
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

        ArrayList<Comment> comments = new ArrayList<Comment>();
        return comments;
    }
}