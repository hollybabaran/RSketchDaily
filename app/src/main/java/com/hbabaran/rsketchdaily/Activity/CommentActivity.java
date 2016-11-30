package com.hbabaran.rsketchdaily.Activity;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.hbabaran.rsketchdaily.R;

import java.net.URL;

public class CommentActivity extends AppCompatActivity {

    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.url = getIntent().getExtras().getString("url");
        setContentView(R.layout.activity_comment);
        //TODO one day actually have an inline comment view
        //For now just open the comment in browser
        final WebView webView = ((WebView) findViewById(R.id.webview));
        final URL comment;
        try {
            System.out.println("loading: "+ this.url);
            comment = new URL(this.url);
            webView.loadUrl(comment.toExternalForm());

        } catch(Exception e){
            System.out.println("failed to load comment url: " + this.url);
            e.printStackTrace();
        }
        finish();
        // Load the authorization URL into the browser

    }
}
