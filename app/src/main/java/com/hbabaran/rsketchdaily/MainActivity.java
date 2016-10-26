package com.hbabaran.rsketchdaily;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    Intent postCacheService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        System.out.println("hello, world");

        //postCacheService = new Intent(this, PostCacheService.class);
        //startService(postCacheService);

        Bundle bundle = new Bundle();
        bundle.putLong("date", Date.getUnixMintime(Calendar.getInstance()));
        Intent startToday = new Intent(this, GalleryActivity.class);
        startToday.putExtras(bundle);
        startActivity(startToday);


    }

    public void onDestroy(){
        super.onDestroy();
        //stopService(postCacheService); //TODO I think this means PostCacheService won't persist but I can't test this right now, so figure this out / make it persist later
    }
}
