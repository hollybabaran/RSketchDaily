package com.hbabaran.rsketchdaily.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.hbabaran.rsketchdaily.Helper.AuthConstants.IMGUR_CLIENT_ID;

/**
 * Created by wren on 11/11/2016.
 */

public class SubmissionUpLoader {

    public static String uploadToImgur(Uri image, Context context){
        try {
            System.out.println("attempting to upload to imgur");
            URL url;
            url = new URL("https://api.imgur.com/3/image");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String data = getFileData(image, context);
            if(data == null) return null;
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Client-ID " + AuthConstants.IMGUR_CLIENT_ID);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            conn.connect();
            StringBuilder stb = new StringBuilder();
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                stb.append(line).append("\n");
            }
            wr.close();
            rd.close();
            return parseResponseForLink(stb.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private static String getFileData(Uri uri, Context context){
        Bitmap image;
        try {
            image = decodeFile(uri, context);
        } catch (IOException e){
            System.err.println("Couldn't open image file: " + e);
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String dataImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        String data;
        try {
            data = URLEncoder.encode("image", "UTF-8") + "="
                    + URLEncoder.encode(dataImage, "UTF-8");
        } catch (UnsupportedEncodingException e){
            System.err.println("Could not encode image for upload: " + e);
            return null;
        }
        return data;
    }
    private static final int IMAGE_MAX_SIZE = 600;
    private static Bitmap decodeFile(Uri uri, Context context) throws IOException{
        Bitmap b;
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        InputStream fis = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int)Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }
        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = context.getContentResolver().openInputStream(uri);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();
        return b;
    }
    private static String parseResponseForLink(String response){

        return null;
    }

}



