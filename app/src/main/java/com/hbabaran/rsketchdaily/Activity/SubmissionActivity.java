package com.hbabaran.rsketchdaily.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.hbabaran.rsketchdaily.Helper.PostLoader;
import com.hbabaran.rsketchdaily.Helper.RedditLogin;
import com.hbabaran.rsketchdaily.Model.Date;
import com.hbabaran.rsketchdaily.Model.Post;
import com.hbabaran.rsketchdaily.Model.Submission;
import com.hbabaran.rsketchdaily.R;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubmissionActivity extends AppCompatActivity {


    public static final int PHOTO_FROM_GALLERY = 1;
    public static final int PHOTO_FROM_CAMERA = 2;
    public static final int REQUEST_CAMERA_PERMISSION = 3;
    public static final int REQUEST_WRITE_PERMISSION = 4;
    public static final int SUBMISSION_VALID = -1;

    public static final int PROGRESS_SAVING_IMAGE = R.string.submission_progress_saving_image;
    public static final int PROGRESS_UPLOADING_IMAGE = R.string.submission_progress_uploading_image;
    public static final int PROGRESS_POSTING_COMMENT = R.string.submission_progress_posting_comment;
    public static final int PROGRESS_SUCCESS = R.string.submission_progress_success;
    public static final int PROGRESS_FAILURE = R.string.submission_progress_failure;

    public static final int MESSAGE_MISSING_POST = R.string.submission_missing_post;
    public static final int MESSAGE_MISSING_IMAGE = R.string.submission_missing_image;
    public static final int MESSAGE_MISSING_COMMENT = R.string.submission_missing_comment;

    private ImageButton galleryPhotoChooserButton;
    private ImageButton cameraButton;
    private EditText commentBox;

    private String postTitle;
    private long postDate;
    private File cameraFile;
    private Uri cameraURI;
    private Submission submission;

    private RedditLogin redditLogin;

    private Boolean savePicsPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        setupCommentBox();
        this.submission = new Submission();

        Bundle bundle = getIntent().getExtras();
        this.postDate = new Date(bundle.getLong("date")).toPrimitive();
        this.submission.setPost(bundle.getString("id"));
        this.postTitle = bundle.getString("title");
        if (this.postTitle == null) {
            new downloadPostInfo().execute(this.postDate);
        }

        setupGalleryPhotoChooserButton();
        setupActionBar();
        updateSubmissionButton();
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            requestCameraPermissions();
        } else {
            disableCameraButton();
        }
        requestWriteFilePermissions();
        this.redditLogin = new RedditLogin(getApplicationContext().
                getSharedPreferences(getString(R.string.prefs_reddit_login), Context.MODE_PRIVATE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.redditLogin.refreshLogin(getApplicationContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_FROM_GALLERY:
                    this.submission.setImageURI(data.getData());
                    this.submission.setImageFile(null); //TODO maybe copy gallery pictures later?
                    break;
                case PHOTO_FROM_CAMERA:
                    this.submission.setImageURI(this.cameraURI);
                    this.submission.setImageFile(this.cameraFile);
                    break;
                default:
                    System.err.println("ERROR: photo picker, activity result not recognized");
            }
            updateSubmissionButton();
            updateThumbnail();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    private void setupGalleryPhotoChooserButton() {
        this.galleryPhotoChooserButton = (ImageButton) findViewById(R.id.gallery_photo_chooser_button);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        this.galleryPhotoChooserButton.setOnClickListener(
                new IntentResultListener(photoPickerIntent, PHOTO_FROM_GALLERY));
    }


    private void setupCameraFile() throws IOException {
        //TODO Request permission to write to shared directory??
        //TODO if this is making a temp file, how to save it into a permanent file??
        //File storageDir = getExternalStoragePublicDirectory((Environment.DIRECTORY_PICTURES));
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        System.out.println(storageDir.toString());
        this.cameraFile = File.createTempFile(
                "submission_" + this.postDate,  /* prefix */
                ".jpg",         /* suffix */
                storageDir     /* directory */
        );
    }

    private void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        if (this.postTitle != null) {
            ab.setTitle(this.postTitle);
        } else {
            ab.setTitle(R.string.loading);
        }
    }

    private void setupCommentBox(){
        this.commentBox = (EditText)findViewById(R.id.comment_box);
        String[] default_comments = getResources().getStringArray(R.array.default_comments);
        String comment = default_comments[new Random().nextInt(default_comments.length)];
        this.commentBox.setText(comment);
    }

    private void setupCameraButton() {
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        try {
            setupCameraFile();
        } catch (IOException e) {
            System.err.println("could not make photo file: " + e + "\nDisabling camera.");
            disableCameraButton();
        }
        if (this.cameraFile != null) {
            this.cameraURI = FileProvider.getUriForFile(this,
                    "com.hbabaran.rsketchdaily.fileprovider",
                    this.cameraFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.cameraURI);
            this.cameraButton = (ImageButton) findViewById(R.id.camera_button);
            this.cameraButton.setOnClickListener(
                    new IntentResultListener(cameraIntent, PHOTO_FROM_CAMERA));
        }

    }

    private void disableCameraButton() {
        this.cameraButton = (ImageButton) findViewById(R.id.camera_button);
        this.cameraButton.setVisibility(View.GONE);
    }

    private void updateThumbnail() {
        ImageView display = (ImageView) findViewById(R.id.pic_selection_display);
        display.setImageDrawable(null);
        display.setImageURI(this.submission.getImageURI());
    }

    private void updateSubmissionButton(){
        Button submissionButton = (Button) findViewById(R.id.submission_button);
        submissionButton.setOnClickListener(new submitButtonOnClickListener(this.submission, this));
    }

    public void updateSubmissionProgress(int progress){
        Toast.makeText(this, progress, Toast.LENGTH_LONG).show();
    }

    public void updatePostInfo(String title, String id) {
        this.postTitle = title;
        this.submission.setPost(id);
        updateSubmissionButton();
        setupActionBar();
    }

    private void requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            //TODO test on lower build versions the assumption that manifest permissions are fine
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
            //TODO test on lower build versions the assumption that manifest permissions are fine
            this.savePicsPermission = true;
        }
    }

    private class submit extends AsyncTask<Submission, Integer, Submission> {
        protected Submission doInBackground(Submission... submission) {
            if(savePicsPermission) publishProgress(SubmissionActivity.PROGRESS_SAVING_IMAGE);
            if(!savePicsPermission || submission[0].saveImage(getApplicationContext())) {
                publishProgress(SubmissionActivity.PROGRESS_UPLOADING_IMAGE);
                if (submission[0].uploadToImgur(getApplicationContext())) {
                    publishProgress(SubmissionActivity.PROGRESS_POSTING_COMMENT);
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
            } else{
                updateSubmissionProgress(PROGRESS_FAILURE);
            }
            //TODO send back to gallery / commentview
        }
    }

    private class downloadPostInfo extends AsyncTask<Long, Void, List<String>> {
        protected List<String> doInBackground(Long... date) {
            Post post = PostLoader.getPostByDate(new Date(date[0]));
            List<String> postInfo = new ArrayList<>();
            postInfo.add(post.getTitle());
            postInfo.add(post.getID().toString());
            return postInfo;
        }

        protected void onPostExecute(List<String> postInfo) {
            updatePostInfo(postInfo.get(0), postInfo.get(1));
        }
    }

    private class IntentResultListener implements View.OnClickListener {
        private Intent intent;
        private int resultCode;

        public IntentResultListener(Intent intent, int resultCode) {
            this.intent = intent;
            this.resultCode = resultCode;
        }

        public void onClick(View v) {
            startActivityForResult(this.intent, this.resultCode);
        }
    }

    public class submitButtonOnClickListener implements View.OnClickListener{
        public Submission submission;
        public SubmissionActivity activity;
        public submitButtonOnClickListener(Submission submission, SubmissionActivity activity){
            this.submission = submission;
            this.activity = activity;
        }
        public void onClick(View v) {
            submission.setText(commentBox.getText().toString());
            int errorCode = submission.isValidSubmission();
            if(errorCode != SubmissionActivity.SUBMISSION_VALID) {
                Toast.makeText(this.activity, errorCode, Toast.LENGTH_SHORT).show();
                return;
            }
            if(!redditLogin.isReady()){
                Toast.makeText(this.activity, R.string.submission_reddit_login_waiting, Toast.LENGTH_SHORT).show();
                redditLogin.refreshLogin(getApplicationContext());
                return;
            }
            new submit().execute(this.submission);
        }
    }
}
