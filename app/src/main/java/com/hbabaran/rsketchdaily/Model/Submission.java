package com.hbabaran.rsketchdaily.Model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.hbabaran.rsketchdaily.Activity.SubmissionActivity;
import com.hbabaran.rsketchdaily.Helper.SubmissionUpLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by wren on 08/11/2016.
 */

public class Submission {

    private Context context;

    private String postURL;
    private Uri image;
    private String submissionText;
    private String imgurURL;
    private String commentURL;

    public Submission(Context context){
        this.context = context;
        this.postURL = null;
        this.image = null;
        this.submissionText = null;
        this.imgurURL = null;
        this.commentURL = null;
    }

    public void setImage(Uri image){
        this.image = image;
    }
    public void setText(String submissionText){
        this.submissionText = submissionText;
    }
    public void setPost(String post){
        this.postURL = post;
    }
    public void setImgurURL(String url){ this.imgurURL = url; }
    public void setCommentURL(String url){ this.commentURL = url; }

    public Uri getImage(){ return this.image; }
    public String getSubmissionText(){ return this.submissionText; }
    public String getPostURL(){ return this.postURL; }
    public String getImgurURL(){ return this.imgurURL; }
    public String getCommentURL(){ return this.imgurURL; }

    public boolean hasComment(){
        return (this.submissionText != null && !this.submissionText.equals(""));
    }
    public boolean hasImage(){
        return (this.image != null);
    }
    public boolean hasPost(){
        return (this.postURL != null);
    }
    public boolean hasImgurURL() { return (this.imgurURL != null); }
    public boolean hasCommentURL() { return (this.commentURL != null); }

    public int isValidSubmission(){
        if(!hasPost()) return SubmissionActivity.MESSAGE_MISSING_POST;
        if(!hasImage()) return SubmissionActivity.MESSAGE_MISSING_IMAGE;
        if(!hasComment()) return SubmissionActivity.MESSAGE_MISSING_COMMENT;
        return SubmissionActivity.SUBMISSION_VALID;
    }

    public Boolean submissionSuccessful(){
        return (this.imgurURL != null && this.commentURL != null);
    }

    public Boolean saveImage(){
        if(!hasImage()) return false;
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(this.image);
        context.sendBroadcast(mediaScanIntent);
        return true;
    }

    public Boolean uploadToImgur(Context context){
        if(!hasImage()) return false;
        this.imgurURL = SubmissionUpLoader.uploadToImgur(this.image, context);
        return(hasImgurURL());
    }

    public Boolean postComment(){
        if(!hasImgurURL() || !hasPost()) return false;
        this.commentURL = SubmissionUpLoader.postRedditComment(
                this.getImgurURL(), this.getPostURL());
        return(hasCommentURL());
    }
}
