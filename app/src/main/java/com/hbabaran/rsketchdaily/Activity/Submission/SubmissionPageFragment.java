package com.hbabaran.rsketchdaily.Activity.Submission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Random;

import static android.app.Activity.RESULT_OK;

/**
 * Created by wren on 1/3/2017.
 */

// Instances of this class are fragments representing a single
// object in our collection.
public class SubmissionPageFragment extends Fragment {
    public static final String ARG_DATE = "date";
    public static final String ARG_CAM_PERMISSION = "camPermission";

    public static final int PHOTO_FROM_GALLERY = 1;
    public static final int PHOTO_FROM_CAMERA = 2;
    public static final int SUBMISSION_VALID = -1;




    private ImageButton galleryPhotoChooserButton;
    private ImageButton cameraButton;
    private EditText commentBox;
    private String savedComment;

    private String postTitle;
    private long postDate;
    private File cameraFile;
    private Uri cameraURI;
    private Submission submission;

    private Boolean cameraPermission;

    private Boolean setupDone;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.activity_submission, container, false);

        this.submission = new Submission();

        Bundle bundle = getArguments();
        this.postDate = new Date(bundle.getLong(ARG_DATE)).toPrimitive();
        this.cameraPermission = false;
        this.cameraPermission = bundle.getBoolean(ARG_CAM_PERMISSION);

        //this.submission.setPost(bundle.getString("id")); //TODO add back for when gallery view is a thing
        //this.postTitle = bundle.getString("title");
        if (this.postTitle == null) {
            new downloadPostInfo().execute(this.postDate);
        }
        setupDone = false;
        this.savedComment = null;
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        if(!setupDone && getUserVisibleHint()){
            setupUI();
        }
    }

    @Override //TODO I believe this is only called the *first* time it's visible
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        setupDone = false;
        if (isVisibleToUser && this.getView()!=null) {
            setupDone = true;
            setupUI();
        }
        if(!isVisibleToUser){
            saveComment();
        }
    }

    private void setupUI(){
        setupCommentBox();
        setupGalleryPhotoChooserButton();
        setupActionBar();
        updateSubmissionButton();
        if(this.cameraPermission){
            setupCameraButton();
            System.out.println("setting up camera button for post: " + this.postTitle);
        } else {
            disableCameraButton();
        }
    }

    private void setupActionBar() {
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (this.postTitle != null) {
            ab.setTitle(this.postTitle);
        } else {
            ab.setTitle(R.string.loading);
        }
    }

    private void saveComment(){
        if(this.commentBox != null) {
            this.savedComment = commentBox.getText().toString();
        }
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
        else{
            System.err.println("Returned from camera not OK: result " + resultCode);
        }
    }


    private void setupGalleryPhotoChooserButton() {
        this.galleryPhotoChooserButton = (ImageButton) getView().findViewById(R.id.gallery_photo_chooser_button);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        this.galleryPhotoChooserButton.setOnClickListener(
                new IntentResultListener(photoPickerIntent, PHOTO_FROM_GALLERY));
    }


    private void setupCameraFile() throws IOException {
        //TODO Request permission to write to shared directory??
        //TODO if this is making a temp file, how to save it into a permanent file??
        //File storageDir = getExternalStoragePublicDirectory((Environment.DIRECTORY_PICTURES));
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        System.out.println(storageDir.toString());
        this.cameraFile = File.createTempFile(
                "submission_" + this.postDate,  /* prefix */
                ".jpg",         /* suffix */
                storageDir     /* directory */
        );
    }



    private void setupCommentBox(){
        this.commentBox = (EditText)getView().findViewById(R.id.comment_box);
        if(this.savedComment == null){
            String[] default_comments = getResources().getStringArray(R.array.default_comments);
            this.savedComment = default_comments[new Random().nextInt(default_comments.length)];
        }
        this.commentBox.setText(this.savedComment);
    }

    public void setupCameraButton() {
        try {
            setupCameraFile();
        } catch (IOException e) {
            System.err.println("could not make photo file: " + e + "\nDisabling camera.");
            disableCameraButton();
        }
        if (this.cameraFile != null) {
            this.cameraURI = FileProvider.getUriForFile(getActivity(),
                    "com.hbabaran.rsketchdaily.fileprovider",
                    this.cameraFile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.cameraURI);
            this.cameraButton = (ImageButton) getView().findViewById(R.id.camera_button);
            this.cameraButton.setOnClickListener(
                    new IntentResultListener(cameraIntent, PHOTO_FROM_CAMERA));
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


    public void disableCameraButton() {
        this.cameraButton = (ImageButton) getView().findViewById(R.id.camera_button);
        this.cameraButton.setVisibility(View.GONE);
    }

    private void updateThumbnail() {
        ImageView display = (ImageView) getView().findViewById(R.id.pic_selection_display);
        display.setImageDrawable(null);
        display.setImageURI(this.submission.getImageURI());
    }

    public void updatePostInfo(String title, String id) {
        this.postTitle = title;
        this.submission.setPost(id);
        if(getUserVisibleHint()) {
            updateSubmissionButton();
            setupActionBar();
        }
    }

    private void updateSubmissionButton(){
        Button submissionButton = (Button) getView().findViewById(R.id.submission_button);
        submissionButton.setOnClickListener(
                new submitButtonOnClickListener(this.submission, (SubmissionSwipeActivity)getActivity()));
    }

    private class submitButtonOnClickListener implements View.OnClickListener{
        public Submission submission;
        public SubmissionSwipeActivity activity;
        public submitButtonOnClickListener(Submission submission, SubmissionSwipeActivity activity){
            this.submission = submission;
            this.activity = activity;
        }
        public void onClick(View v) {
            submission.setText(commentBox.getText().toString());
            int errorCode = submission.isValidSubmission();
            if(errorCode != SUBMISSION_VALID) {
                Toast.makeText(this.activity, errorCode, Toast.LENGTH_SHORT).show();
                return;
            }
            if(!activity.getRedditLogin().isReady()){
                Toast.makeText(this.activity, R.string.submission_reddit_login_waiting, Toast.LENGTH_SHORT).show();
                activity.refreshLogin();
                return;
            }
            activity.getSubmitAsync().execute(this.submission);
        }
    }

    public class downloadPostInfo extends AsyncTask<Long, Void, List<String>> {
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











}