package com.hbabaran.rsketchdaily.Activity.Submission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.hbabaran.rsketchdaily.Activity.CommentActivity;
import com.hbabaran.rsketchdaily.Activity.RedditLoginActivity;
import com.hbabaran.rsketchdaily.Activity.Settings.SettingsActivity;
import com.hbabaran.rsketchdaily.Helper.PostLoader;
import com.hbabaran.rsketchdaily.Helper.RedditLogin;
import com.hbabaran.rsketchdaily.Model.Date;
import com.hbabaran.rsketchdaily.Model.Post;
import com.hbabaran.rsketchdaily.Model.Submission;
import com.hbabaran.rsketchdaily.R;

import java.util.ArrayList;
import java.util.List;

public class SubmissionSwipeActivity extends AppCompatActivity {
    public static final int PROGRESS_SAVING_IMAGE = R.string.submission_progress_saving_image;
    public static final int PROGRESS_UPLOADING_IMAGE = R.string.submission_progress_uploading_image;
    public static final int PROGRESS_POSTING_COMMENT = R.string.submission_progress_posting_comment;
    public static final int PROGRESS_SUCCESS = R.string.submission_progress_success;
    public static final int PROGRESS_FAILURE = R.string.submission_progress_failure;

    public static final int REQUEST_CAMERA_PERMISSION = 3;
    public static final int REQUEST_WRITE_PERMISSION = 4;

    private Boolean savePicsPermission;
    private RedditLogin redditLogin;


    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    SubmissionPagerAdapter mSubmissionPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_swipe);

        Bundle bundle = getIntent().getExtras();
        Date today = new Date(bundle.getLong("date"));

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mSubmissionPagerAdapter = new SubmissionPagerAdapter(getSupportFragmentManager(), today);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSubmissionPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        this.redditLogin = new RedditLogin(getApplicationContext().
                getSharedPreferences(getString(R.string.prefs_reddit_login), Context.MODE_PRIVATE));

        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            requestCameraPermissions();
        } else {
            disableCameraButton();
        }
        requestWriteFilePermissions();


    }

    private void disableCameraButton(){
        SubmissionPageFragment currPage = (SubmissionPageFragment)
                getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
        if(currPage != null) currPage.disableCameraButton();
        mSubmissionPagerAdapter.setCamPermission(false, (ViewGroup) findViewById(android.R.id.content));
        //mSubmissionPagerAdapter.notifyDataSetChanged();
    }

    private void setupCameraButton(){
        SubmissionPageFragment currPage = (SubmissionPageFragment)
                getSupportFragmentManager().findFragmentByTag(
                        "android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
        if(currPage != null) currPage.setupCameraButton();
        mSubmissionPagerAdapter.setCamPermission(true, (ViewGroup) findViewById(android.R.id.content));
        //mSubmissionPagerAdapter.notifyDataSetChanged();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.submission_menu, menu);
        return true;
    }

    public void onSettingsClick(MenuItem menuItem){
        startActivity(new Intent(this, SettingsActivity.class));
    }


    protected submit getSubmitAsync(){
        return new submit();
    }

    protected class submit extends AsyncTask<Submission, Integer, Submission> {
        protected Submission doInBackground(Submission... submission) {
            if(savePicsPermission) publishProgress(SubmissionSwipeActivity.PROGRESS_SAVING_IMAGE);
            if(!savePicsPermission || submission[0].saveImage(getApplicationContext())) {
                publishProgress(SubmissionSwipeActivity.PROGRESS_UPLOADING_IMAGE);
                if (submission[0].uploadToImgur(getApplicationContext())) {
                    publishProgress(SubmissionSwipeActivity.PROGRESS_POSTING_COMMENT);
                    submission[0].postComment(redditLogin.client());
                }
            }
            return submission[0];
        }

        protected void onProgressUpdate(Integer... progress) {
            updateSubmissionProgress(progress[0]);
        }

        protected void onPostExecute(Submission submission) {
            if(submission.submissionSuccessful()){
                updateSubmissionProgress(PROGRESS_SUCCESS);
                //sleep because reddit doesn't update immediately
                try{ Thread.sleep(2000); } catch(InterruptedException e) { e.printStackTrace(); }
                startActivity(generateCommentIntent(submission));
                //then sleep before killing submission thread
                //try{ Thread.sleep(5000); } catch(InterruptedException e) { e.printStackTrace(); }
                //finish();
            } else{
                updateSubmissionProgress(PROGRESS_FAILURE);
            }
        }
    }

    public void updateSubmissionProgress(int progress){
        Toast.makeText(this, progress, Toast.LENGTH_LONG).show();
    }

    private Intent generateCommentIntent(Submission submission){
        Bundle extras = new Bundle();
        extras.putString("url", submission.getCommentURL());
        Intent commentIntent = new Intent(this, CommentActivity.class);
        commentIntent.putExtras(extras);
        return commentIntent;
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshLogin();
    }

    void refreshLogin(){
        if(this.redditLogin.refreshLogin() == false){
            Toast.makeText(this, "Log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, RedditLoginActivity.class));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length < 1) return;
        switch(requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCameraButton();
                } else {
                    Toast.makeText(this, "Camera use disabled", Toast.LENGTH_SHORT).show();
                    disableCameraButton();
                }
                break;
            case REQUEST_WRITE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.savePicsPermission = true;
                } else {
                    Toast.makeText(this, "Camera pictures will not be saved to gallery", Toast.LENGTH_SHORT).show();
                    this.savePicsPermission = false;
                }
                break;
        }
    }



    private void requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            setupCameraButton();
        }
    }

    private void requestWriteFilePermissions() {
        this.savePicsPermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_PERMISSION);
        } else {
            this.savePicsPermission = true;
        }
    }

    public RedditLogin getRedditLogin(){
        return redditLogin;
    }


}



