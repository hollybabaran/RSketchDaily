package com.hbabaran.rsketchdaily.Helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

    public static String uploadToImgur(Uri image){
        try {
            System.out.println("attempting to upload to imgur");
            URL url;
            url = new URL("https://api.imgur.com/3/image");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String data = getFileData(image);

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
            return stb.toString();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private static String getFileData(Uri uri){
        File file = new File(uri.getPath());
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();


        String dataImage = Base64.encodeToString(byteArray, Base64.DEFAULT); // .encode(byteArray);
        String data = null;
        try {
            data = URLEncoder.encode("image", "UTF-8") + "="
                    + URLEncoder.encode(dataImage, "UTF-8");
        } catch (UnsupportedEncodingException e){
            System.err.println("Could not encode image for upload: " + e);
        }
        return data;
    }

    /*
    private static void writePostStream(Uri image, OutputStream out){
        System.out.println("Writing the post stream...");
        Map<String, File> post = new HashMap<>();
        post.put("image", new File(image.getPath()));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput oo;
        byte[] mapInBytes;
        try {
            oo = new ObjectOutputStream(bos);
            oo.writeObject(post);
            oo.flush();
            mapInBytes = bos.toByteArray();
            out.write(mapInBytes);
        } catch(IOException e){
            System.err.println("Error encountered generating the byte stream for upload: " + e);
        }
        System.out.println("finished writing the post stream");
    }

    private static String parseResponseURL(InputStream in){
        System.out.println("got a response, gonna try and print it");
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(in));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(sb.toString());


        String imageURL = null;
        return imageURL;
    }
    */
}



