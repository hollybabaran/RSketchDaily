package com.hbabaran.rsketchdaily.Model;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import com.hbabaran.rsketchdaily.Activity.SubmissionActivity;
import com.hbabaran.rsketchdaily.Helper.SubmissionUpLoader;

import net.dean.jraw.RedditClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by wren on 08/11/2016.
 */

public class Submission {


    private String postID;
    private Uri image;
    private File imageFile;
    private String submissionText;
    private String imgurURL;
    private String commentURL;

    private Boolean savePics; //whether or not to save picture

    public Submission(){
        this.postID = "";
        this.image = null;
        this.submissionText = null;
        this.imgurURL = null;
        this.commentURL = null;
        this.imageFile = null;
    }

    public void setImageURI(Uri image){
        this.image = image;
    }
    public void setImageFile(File imageFile){ this.imageFile = imageFile; }
    public void setText(String submissionText){
        this.submissionText = submissionText;
    }
    public void setPost(String id){
        this.postID = id;
    }

    public Uri getImageURI(){ return this.image; }
    public String getSubmissionText(){ return this.submissionText; }
    public String getPostID(){ return this.postID; }
    public String getImgurURL(){ return this.imgurURL; }
    public String getCommentURL(){ return this.commentURL; }
    public File getImageFile(){ return this.imageFile; }

    public boolean hasComment(){
        return (this.submissionText != null && !this.submissionText.equals(""));
    }
    public boolean hasImageURI(){
        return (this.image != null);
    }
    public boolean hasPost(){
        return (this.postID != "");
    }
    public boolean hasImgurURL() { return (this.imgurURL != null); }
    public boolean hasCommentURL() { return (this.commentURL != null); }
    private boolean hasImageFile() { return (this.imageFile != null); }

    public int isValidSubmission(){
        if(!hasPost()) return SubmissionActivity.MESSAGE_MISSING_POST;
        if(!hasImageURI()) return SubmissionActivity.MESSAGE_MISSING_IMAGE;
        if(!hasComment()) return SubmissionActivity.MESSAGE_MISSING_COMMENT;
        return SubmissionActivity.SUBMISSION_VALID;
    }

    public Boolean submissionSuccessful(){
        return (this.imgurURL != null && this.commentURL != null);
    }

    public Boolean saveImage(Context context){
        if(!hasImageURI()) return false;
        if(!hasImageFile()) return true; //TODO currently do nothing if you're uploading a gallery file
        File permanentImage = savePermanentImage(getImageFile());
        if(permanentImage == null) return false;
        MediaScannerConnection.scanFile(context, new String[] { permanentImage.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        System.out.println("Saved image: " + path);
                    }
                });
        return true;
    }
    private static String APPNAME = "RSketchDaily"; //TODO organize this better...
    private File savePermanentImage(File source){
        File destination;
        try {
            File picsDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).getPath() +
                    File.separatorChar + APPNAME);
            if(!picsDir.exists()) picsDir.mkdir();
            destination = new File( picsDir.getAbsolutePath() +
                    File.separatorChar + source.getName());
            FileChannel src = new FileInputStream(source).getChannel();
            FileChannel dst = new FileOutputStream(destination).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return destination;
    }


    public Boolean uploadToImgur(Context context){
        if(!hasImageURI()) return false;
        this.imgurURL = SubmissionUpLoader.uploadToImgur(this.image, context);
        return(hasImgurURL());
    }

    public Boolean postComment(RedditClient redditClient){
        if(!hasImgurURL() || !hasPost()) return false;
        this.commentURL = SubmissionUpLoader.postRedditComment(
                this.getImgurURL(), this.getPostID(), this.getSubmissionText(), redditClient);
        return(hasCommentURL());
    }
}
