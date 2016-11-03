package com.hbabaran.rsketchdaily;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by wren on 11/3/2016.
 */

public class SubmissionService extends IntentService {
    private Intent intent;

    public SubmissionService(){
        super("SubmissionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
