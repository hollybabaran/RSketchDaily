package com.hbabaran.rsketchdaily.Helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.hbabaran.rsketchdaily.Activity.Submission.SubmissionPageFragment;
import com.hbabaran.rsketchdaily.Model.Comment;
import com.hbabaran.rsketchdaily.Model.Date;
import com.hbabaran.rsketchdaily.Model.Post;
import com.hbabaran.rsketchdaily.R;

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
import java.util.List;

import static java.lang.String.valueOf;

/**
 * Created by wren on 10/16/2016.
 */

//static class containing BLOCKING methods that connect to the internet
public class PostLoader {
    public static class RedditHeavyLoadException extends IOException {
        private String message = "Reddit servers are under heavy load right now.";
        public String toString(){
            return message;
        }
    }

    private static String REDDIT_URL = "https://www.reddit.com/r/";
    //private static String SUBREDDIT_NAME = "wrentestsapps";
    private static String SUBREDDIT_NAME = "SketchDaily";
    private static String REDDIT_DATE_REQUEST_SUFFIX = "/search.json?q=timestamp%3A";
    private static String URL_MIDFIX = "..";
    private static String URL_SUFFIX = "&sort=new&restrict_sr=on&syntax=cloudsearch";

    public static Post getPostByDate(Date today){
        Post post;
        try {
            post = new Post(today, getPostJSONByDate(today));
            if (post.getID() == "") { //could not load today probably because it's midnight - 3am; try yesterday
                Date yesterday = Date.getPastDate(today, 1);
                post = new Post(yesterday, getPostJSONByDate(yesterday));
            }
        }catch (RedditHeavyLoadException e){
            post = new Post(today, null);
            post.warnHeavyLoad();
        }
        return post;
    }

    private static JSONObject getPostJSONByDate(Date date) throws RedditHeavyLoadException{
        URL url;
        String redditJSONStr;
        JSONObject frontJson;
        JSONObject post = null;
        try {
            url = buildPostURLByDate(date);
            redditJSONStr = downloadJSONStr(url);
            frontJson = new JSONObject(redditJSONStr);
            JSONArray posts = frontJson.getJSONObject("data").getJSONArray("children");
            if(posts.length() > 0){ //always get the oldest post of the day
                post = posts.getJSONObject(posts.length()-1);
            }
        } catch (MalformedURLException e) {
            System.err.println(e);
            return null;
        } catch (IOException e) {
            System.err.println(e);
            if(e.toString().contains("503")){
                throw new RedditHeavyLoadException();
            }
            return null;
        } catch (JSONException e) {
            System.err.println(e);
            return null;
        }
        return post;
    }
    private static URL buildPostURLByDate(Date date) throws MalformedURLException {
        String url = REDDIT_URL + SUBREDDIT_NAME + REDDIT_DATE_REQUEST_SUFFIX +
                date.getUnix_mintime() +
                URL_MIDFIX +
                date.getUnix_maxtime() +
                URL_SUFFIX;

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

    public static JsonElement downloadJson(URL url) throws IOException{
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        return root; //May be an array, may be an object.
    }

    public static JsonElement downloadJson(URL url, String propName, String propVal) throws IOException{
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestProperty(propName, propVal);
        request.connect();
        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        return root; //May be an array, may be an object.
    }


    public static ArrayList<Comment> parseCommentsFromPost(URL postUrl){
        JsonArray postJson;
        ArrayList<Comment> comments = new ArrayList<>();
        Type listType = new TypeToken<List<JsonElement>>() {}.getType();
        try {
            postJson = downloadJson(new URL(postUrl + ".json")).getAsJsonArray();
            List<JsonElement> commentList = new Gson().fromJson(
                    postJson.get(1).getAsJsonObject()
                            .getAsJsonObject("data")
                            .getAsJsonArray("children"), listType);
            for(JsonElement element : commentList){
                comments.add(new Comment(element.getAsJsonObject()));
            }
            //TODO we're truncating comments at 30 for now
            if(comments.size() > 30) comments.subList(30,comments.size()).clear();
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

}