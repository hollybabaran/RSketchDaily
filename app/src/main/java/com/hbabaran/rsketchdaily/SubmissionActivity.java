package com.hbabaran.rsketchdaily;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SubmissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        //The bundle has either: date (long) OR a title/URL.
        // If it has a date, ask submissionservice to load the post and get post title / url for action bar and submission info
    }
}
