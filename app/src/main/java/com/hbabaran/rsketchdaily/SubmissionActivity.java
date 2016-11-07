package com.hbabaran.rsketchdaily;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.R.attr.path;
import static android.R.attr.thumb;

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
    private File photoFile;
    private Uri photoURI;

    private Bitmap thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        Bundle bundle = getIntent().getExtras();
        postDate = bundle.getLong("date");
        postTitle = bundle.getString("title");
        postURL = bundle.getString("url");
        if(this.postTitle == null){
            //query submissionService to load the post title/url from date
        }
        //set up the buttons, comment text, submit-my-post button
        PackageManager packageManager = this.getPackageManager();
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            requestCameraPermissions();
        } else {
            disableCameraButton();
        }
        setupGalleryPhotoChooserButton();
        setupCameraButton();
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

    private void setupGalleryPhotoChooserButton(){
        this.galleryPhotoChooserButton = (ImageButton) findViewById(R.id.gallery_photo_chooser_button);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        this.galleryPhotoChooserButton.setOnClickListener(
                new IntentResultListener(photoPickerIntent, PHOTO_FROM_GALLERY));
    }

    //TODO creating this file is probably blocking; push this out to service and update button
    private void setupCameraFile() throws IOException{
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        System.out.println(storageDir.toString());
        this.photoFile = File.createTempFile(
                "submission_" + this.postDate,  /* prefix */
                ".jpg",         /* suffix */
                storageDir     /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        //this.photoFilePath = "file:" + photoFile.getAbsolutePath();
    }

    private void requestCameraPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        } else{
            //assume permissions in the manifest are fine; TODO test on lower build versions
            setupCameraButton();
        }
    }

    private void setupCameraButton(){
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        try {
            setupCameraFile();
        } catch (IOException e){
            //TODO could not make photo file, throw an error?
            //TODO (Will this happen if file already exists? We want to just overwrite it.)
            System.err.println("could not make photo file: " + e);
        }
        if(this.photoFile != null) {
            this.photoURI =  FileProvider.getUriForFile(this,
                    "com.hbabaran.rsketchdaily.fileprovider",
                    this.photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.photoURI);
            this.cameraButton = (ImageButton) findViewById(R.id.camera_button);
            this.cameraButton.setOnClickListener(
                    new IntentResultListener(cameraIntent, PHOTO_FROM_CAMERA));
        }

    }

    private void disableCameraButton(){
        //TODO no camera present or couldn't get permission; disable the camera
        System.err.println("no camera detected or no camera permission granted");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_FROM_GALLERY:
                    System.out.println("got an image from the gallery");
                    Uri selectedImageUri = data.getData();
                    ImageView display = (ImageView) findViewById(R.id.pic_selection_display);
                    display.setImageDrawable(null);
                    display.setImageURI(selectedImageUri);
                    break;
                case PHOTO_FROM_CAMERA:
                    //TODO loading image from URI is blocking, push to service
                    updateThumbnail();
                    //TODO register camera image to gallery/media
                    //TODO scale down the thumbnail
                    break;
                default:
                    System.err.println("ERROR: photo picker, activity result not recognized");
            }
        }
    }

    private void updateThumbnail(){
        ImageView display = (ImageView) findViewById(R.id.pic_selection_display);
        display.setImageDrawable(null);
        display.setImageURI(this.photoURI);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCameraButton();
            }else {
                Toast.makeText(this, "Camera use disabled", Toast.LENGTH_SHORT).show();
                disableCameraButton();
            }
        }
    }
}
