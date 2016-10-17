package com.hbabaran.rsketchdaily;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Created by wren on 10/14/2016.
 */
/* For now, this will be the gallery for Today's Post.
 * Later make it more generic, initializing it either as a UserGallery or a PostGallery
*/

public class GalleryActivity extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryactivity);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(GalleryActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });

        actionBarSetup();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallerymenu, menu);
        return true;
    }

    private void actionBarSetup() {
        ActionBar ab = getSupportActionBar();
        new PostLoader("placeholderdate", this).execute(); //TODO pass date in (utc??) and get various dates (for now just take top result, ie today)
    }

    protected void setActionBarText(String text){
        ActionBar ab = getSupportActionBar();
        ab.setTitle(text);
    }



}
