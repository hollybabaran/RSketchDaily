package com.hbabaran.rsketchdaily.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.hbabaran.rsketchdaily.Activity.View.GalleryImageAdapter;
import com.hbabaran.rsketchdaily.Service.GalleryService;
import com.hbabaran.rsketchdaily.R;

/**
 * Created by wren on 10/14/2016.
 */
/* For now, this will be the gallery for Today's Post.
 * Later make it more generic, initializing it either as a UserGallery or a PostGallery
*/

public class GalleryActivity extends AppCompatActivity {

    private Intent cacheIntent;
    public static final String GALLERY_RECEIVER_TAG = "com.hbabaran.rsketchdaily.gallery_receiver";
    public static final int POST_LOADED = 0;
    public static final int COMMENT_COUNT = 1;
    public static final int COMMENT_LOADED = 2;
    public GalleryReceiver mReceiver;


    GridView gridview;
    GalleryImageAdapter adapter;
    FloatingActionButton submission_fab;

    public class GalleryReceiver extends ResultReceiver{
        public GalleryReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData)  {
            switch(resultCode){
                case POST_LOADED:
                    setPostInfo(resultData.getString("title"),
                                resultData.getString("url"),
                                resultData.getLong("date"));
                    //TODO self text
                    break;
                case COMMENT_COUNT:
                    setupGridView(resultData.getInt("count")); //TODO use static strings for bundle keys
                    System.out.println("initializing gridview of length" + resultData.getInt("count"));
                    break;
                case COMMENT_LOADED:
                    sendCommentImage(resultData.getInt("position"), resultData.getByteArray("img"));
                    break;
                default:
                    //throw an error?
            }

        }
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mReceiver = new GalleryReceiver(new Handler());
        cacheIntent = new Intent(this, GalleryService.class);

        //send an intent to GalleryService requesting the date that this activity was started with
        Bundle bundle = getIntent().getExtras();
        bundle.putParcelable(GALLERY_RECEIVER_TAG, mReceiver);
        cacheIntent.putExtras(bundle);
        startService(cacheIntent);

        setupActionBar();
        setupSubmissionButton(bundle.getLong("date"));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallerymenu, menu);
        return true;
    }

    private void setupGridView(int commentCount){
        this.gridview = (GridView) findViewById(R.id.gridview);
        this.adapter = new GalleryImageAdapter(this, commentCount);
        this.gridview.setAdapter(this.adapter);
        this.gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(GalleryActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class submissionListener implements View.OnClickListener{
        private Intent submissionIntent;
        public submissionListener(Intent submissionIntent){ this.submissionIntent = submissionIntent;}
        public void onClick(View v) {
            startActivity(this.submissionIntent);
        }
    }
    private void setupSubmissionButton(long post_date){
        Bundle bundle = new Bundle();
        bundle.putLong("date", post_date);
        setSubmissionButtonIntent(bundle);
    }
    private void updateSubmissionButton(String title, String url, long post_date){
        Bundle bundle = new Bundle();
        bundle.putLong("date", post_date);
        bundle.putString("title", title);
        bundle.putString("url", url);
        setSubmissionButtonIntent(bundle);
    }

    private void setSubmissionButtonIntent(Bundle extras){
        Intent submissionIntent = new Intent(this, SubmissionActivity.class);
        submissionIntent.putExtras(extras);
        this.submission_fab = (FloatingActionButton) findViewById(R.id.submission_fab);
        this.submission_fab.setOnClickListener(new submissionListener(submissionIntent));
    }

    private void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.loading);
    }

    protected void updateActionBar(String text){
        ActionBar ab = getSupportActionBar();
        ab.setTitle(text);
    }

    protected void sendCommentImage(int position, byte[] img){
        this.adapter.setCommentImage(position, img);
        this.adapter.notifyDataSetChanged();
    }

    protected void setPostInfo(String title, String url, long date){
        updateActionBar(title);
        updateSubmissionButton(title, url, date);
    }


}
