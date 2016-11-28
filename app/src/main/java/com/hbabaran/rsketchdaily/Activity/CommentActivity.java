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

    private String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.id = getIntent().getExtras().getString("id");
        setContentView(R.layout.activity_comment);
        //TODO one day actually have an inline comment view
        //For now just open the comment in chrome
        final WebView webView = ((WebView) findViewById(R.id.webview));
        final URL comment;
        try {
            comment = new URL("https://www.reddit.com/r/wrentestsapps/comments/5f80px/nov_27_have_you_ever_been_so_far_as_to_do_more/dai5jmm");
            webView.loadUrl(comment.toExternalForm());

        } catch(Exception e){}
        // Load the authorization URL into the browser

    }
}
