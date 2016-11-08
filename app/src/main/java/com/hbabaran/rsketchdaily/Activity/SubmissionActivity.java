package com.hbabaran.rsketchdaily.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.hbabaran.rsketchdaily.Helper.PostLoader;
import com.hbabaran.rsketchdaily.Model.Date;
import com.hbabaran.rsketchdaily.Model.Post;
import com.hbabaran.rsketchdaily.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubmissionActivity extends AppCompatActivity {

    public static final int PHOTO_FROM_GALLERY = 1;
    public static final int PHOTO_FROM_CAMERA = 2;
    public static final int REQUEST_CAMERA_PERMISSION = 3;
    private static final String MEDIA_FOLDER = "RSketchDaily";

    private ImageButton galleryPhotoChooserButton;
    private ImageButton cameraButton;

    private String postTitle;
    private String postURL;
    private long postDate;

    private File cameraFile;
    private Uri cameraURI;
    private Uri submissionURI;

    private String submissionText;

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

    public class IntentResultListener implements View.OnClickListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        this.submissionURI = null;
        this.submissionText = null;
        this.postTitle = null;
        this.postURL = null;

        Bundle bundle = getIntent().getExtras();
        this.postDate = new Date(bundle.getLong("date")).toPrimitive();
        this.postTitle = bundle.getString("title");
        this.postURL = bundle.getString("url");
        //async download post title if necessary
        if (this.postTitle == null) {
            new downloadPostInfo().execute(this.postDate);
        }
        setupActionBar();

        //set up the buttons, comment text, submit-my-post button
        setupGalleryPhotoChooserButton();
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
                    this.submissionURI = data.getData();
                    break;
                case PHOTO_FROM_CAMERA:
                    this.submissionURI = this.cameraURI;
                    break;
                default:
                    System.err.println("ERROR: photo picker, activity result not recognized");
            }
            updateThumbnail();
        }
    }

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
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        System.out.println(storageDir.toString());
        this.cameraFile = File.createTempFile(
                "submission_" + this.postDate,  /* prefix */
                ".jpg",         /* suffix */
                storageDir     /* directory */
        );
    }

    private void requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        } else {
            //TODO test on lower build versions the assumption that manifest permissions are fine
            setupCameraButton();
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

    //TODO loading this image from URI is blocking, push out to service
    private void updateThumbnail() {
        ImageView display = (ImageView) findViewById(R.id.pic_selection_display);
        display.setImageDrawable(null);
        display.setImageURI(this.submissionURI);
    }

    private void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        if (this.postTitle != null) {
            ab.setTitle(this.postTitle);
        } else {
            ab.setTitle(R.string.loading);
        }
    }

    protected void updatePostInfo(String title, String url) {
        this.postTitle = title;
        this.postURL = url;
        setupActionBar();
    }


    private void submit() {
        //TODO register the photo being submitted to the gallery (will create a duplicate, that's ok)
        //TODO other stuff...
    }
}
