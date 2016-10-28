package com.hbabaran.rsketchdaily;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import static java.lang.String.valueOf;

/**
 * Created by wren on 10/16/2016.
 */

//static class containing BLOCKING methods that connect to the internet
public class PostLoader {

    public static Post getPostByDate(Date date) {
        return new Post(date, getPostJSONByDate(date));
    }

    private static final String TUMBLR_OAUTH_CONSUMER_KEY = "SSqgosrC2vc9r4t8eI0OiUL3F9Y9yprIbfM4uaJScEa6dDcj9W";

    //TODO figure out what happens if there are two posts on one day (eg someone made a sticky) and handle that case
    //perhaps ensure that the url list is sorted by date and then get the first one (posted at 3am typically)
    private static JSONObject getPostJSONByDate(Date date) {
        URL url;
        String redditJSONStr;
        JSONObject frontJson;
        JSONObject post;
        try {
            url = buildPostURLByDate(date);
            redditJSONStr = downloadJSONStr(url);
            frontJson = new JSONObject(redditJSONStr);
            post = frontJson.getJSONObject("data").getJSONArray("children").getJSONObject(0);
        } catch (MalformedURLException e) {
            System.err.println(e);
            return null;
        } catch (IOException e) {
            System.err.println(e);
            return null;
        } catch (JSONException e) {
            System.err.println(e);
            return null;
        }
        return post;
    }

    private static URL buildPostURLByDate(Date date) throws MalformedURLException {
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

    private static String downloadJSONStr(URL url) throws IOException {
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
                        sb.append(line + "\n");
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

    private static JsonArray downloadJson(URL url) throws IOException{
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        return root.getAsJsonArray(); //May be an array, may be an object.
    }


    public static ArrayList<Comment> parseCommentsFromPost(URL postUrl){
        JsonArray postJson;
        ArrayList<Comment> comments = new ArrayList<>();
        Type listType = new TypeToken<List<JsonElement>>() {}.getType();
        try {
            postJson = downloadJson(new URL(postUrl + ".json"));
            List<JsonElement> commentList = new Gson().fromJson(
                    postJson.get(1).getAsJsonObject()
                            .getAsJsonObject("data")
                            .getAsJsonArray("children"), listType);
            for(JsonElement element : commentList){
                comments.add(new Comment(element.getAsJsonObject()));
            }
        } catch (MalformedURLException e){
            System.err.println(e);
            return null;
        } catch(IOException e) {
            System.err.println(e);
            return null;
        } /*catch(JSONException e) {
            System.err.println(e);
            return null;
        }*/
        return comments;
    }

    public static Bitmap getCommentImage(URL url){
        ImageLoader imageLoader = ImageLoader.getInstance();
        return imageLoader.loadImageSync(url.toString());
    }
}