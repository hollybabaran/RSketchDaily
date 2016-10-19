package com.hbabaran.rsketchdaily;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by wren on 10/14/2016.
 */
/* For now, this will be the gallery for Today's Post.
 * Later make it more generic, initializing it either as a UserGallery or a PostGallery
*/

public class GalleryActivity extends AppCompatActivity {

    private Intent cacheIntent;
    public static final String GALLERY_RECEIVER_TAG = "com.hbabaran.rsketchdaily.gallery_receiver";

    public GalleryReceiver mReceiver;

    public class GalleryReceiver extends ResultReceiver{
        public GalleryReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData)  {
            System.out.println("galleryactivity received post info send ");
            setActionBarText(resultData.getString("title"));
        }
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryactivity);

        mReceiver = new GalleryReceiver(new Handler());
        cacheIntent = new Intent(this, PostCacheService.class);
        //send an intent to PostCacheService requesting the date that this activity was started with
        Bundle bundle = getIntent().getExtras();
        bundle.putParcelable(GALLERY_RECEIVER_TAG, mReceiver);
        cacheIntent.putExtras(bundle);
        startService(cacheIntent);

        gridViewSetup();
        actionBarSetup();

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallerymenu, menu);
        return true;
    }

    private void gridViewSetup(){
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(GalleryActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void actionBarSetup() {
        ActionBar ab = getSupportActionBar();
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar currtime = Calendar.getInstance(timeZone);
        ab.setTitle(R.string.loading);
        //new PostLoader(currtime, this).execute();
    }

    protected void setActionBarText(String text){
        ActionBar ab = getSupportActionBar();
        ab.setTitle(text);
    }



}
