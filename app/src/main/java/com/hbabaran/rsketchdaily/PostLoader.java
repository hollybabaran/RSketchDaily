package com.hbabaran.rsketchdaily;

import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.String.valueOf;

/**
 * Created by wren on 10/16/2016.
 */

public class PostLoader extends AsyncTask<Void, String, String> {

    private String date; //TODO change this to a date type
    private GalleryActivity gallery;

    public PostLoader(String date, GalleryActivity gallery){
        this.date = date;
        this.gallery = gallery;
    }

    protected void onPreExecute() {
        gallery.setActionBarText(gallery.getResources().getString(R.string.loading));
    }

    public String doInBackground(Void... v) { //TODO use "date" to get post data per date
        return getPostTitle();
    }

    protected void onPostExecute(String result) {
        gallery.setActionBarText(result);
    }


    private String getPostTitle(){
        String resultString = getJSONStr(gallery.getResources().getString(R.string.subreddit_url));
        String title = "Error parsing title;";
        JSONObject frontJson;
        try{
            frontJson = new JSONObject(resultString);
            title = frontJson
                    .getJSONObject("data")
                    .getJSONArray("children")
                    .getJSONObject(0) //TODO eventually parse listing by date; for now just look at first listing
                    .getJSONObject("data")
                    .getString("title");
        } catch(JSONException e) {
            System.err.println("error parsing reddit page to json:\n" +
                    gallery.getResources().getString(R.string.subreddit_url) + "\n" +
                    e);
        }
        return title; //TODO change
    }

    private String getJSONStr(String url) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
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
                    return "Error: " + valueOf(status);
            }

        } catch (Exception ex) {
            return ex.toString();
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    //disconnect error
                }
            }
        }
        //return null;
    }
}