package com.hbabaran.rsketchdaily.Activity.Settings;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;

public class SettingsActivity extends Activity
        implements SettingsFragment.OnFragmentInteractionListener {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
    public void onFragmentInteraction(Uri uri){

    }




}
