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
import com.hbabaran.rsketchdaily.Model.Date;
import com.hbabaran.rsketchdaily.Model.Post;
import com.hbabaran.rsketchdaily.Model.Submission;
import com.hbabaran.rsketchdaily.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubmissionActivity extends AppCompatActivity {

    //public static final String MEDIA_DIR = "Android/data/RSketchDaily";
    public static final int PHOTO_FROM_GALLERY = 1;
    public static final int PHOTO_FROM_CAMERA = 2;
    public static final int REQUEST_CAMERA_PERMISSION = 3;
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

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);
        this.mContext = this.getApplicationContext();
        this.commentBox = (EditText)findViewById(R.id.comment_box);

        this.submission = new Submission(getApplicationContext());

        Bundle bundle = getIntent().getExtras();
        this.postDate = new Date(bundle.getLong("date")).toPrimitive();
        this.submission.setPost(bundle.getString("url"));
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

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_FROM_GALLERY:
                    this.submission.setImage(data.getData());
                    break;
                case PHOTO_FROM_CAMERA:
                    this.submission.setImage(this.cameraURI);
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
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCameraButton();
            } else {
                Toast.makeText(this, "Camera use disabled", Toast.LENGTH_SHORT).show();
                disableCameraButton();
            }
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
        display.setImageURI(this.submission.getImage());
    }

    private void updateSubmissionButton(){
        Button submissionButton = (Button) findViewById(R.id.submission_button);
        submissionButton.setOnClickListener(new submitButtonOnClickListener(this.submission, this));
    }

    public void updateSubmissionProgress(int progress){
        Toast.makeText(this, progress, Toast.LENGTH_LONG).show();
    }

    public void updatePostInfo(String title, String url) {
        this.postTitle = title;
        this.submission.setPost(url);
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

    private class submit extends AsyncTask<Submission, Integer, Submission> {
        protected Submission doInBackground(Submission... submission) {
            publishProgress(SubmissionActivity.PROGRESS_SAVING_IMAGE);
            if(submission[0].saveImage()) {
                publishProgress(SubmissionActivity.PROGRESS_UPLOADING_IMAGE);
                if (submission[0].uploadToImgur(mContext)) {
                    publishProgress(SubmissionActivity.PROGRESS_POSTING_COMMENT);
                    submission[0].postComment();
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
            postInfo.add(post.getPostURL().toString());
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
            if(errorCode == SubmissionActivity.SUBMISSION_VALID){
                new submit().execute(this.submission);
            } else{
                Toast.makeText(this.activity, errorCode, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
