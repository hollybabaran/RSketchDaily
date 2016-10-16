package com.hbabaran.rsketchdaily;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryactivity);
        Intent showToday = new Intent(this, GalleryActivity.class); //TODO later when GalleryActivity is more generic you will need to specify it's a PostGallery and put today's date in the intent
        this.startActivity(showToday);
    }
}
