package com.hbabaran.rsketchdaily.Activity.Submission;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.hbabaran.rsketchdaily.Model.Date;


/**
 * Created by wren on 1/3/2017.
 */

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class SubmissionPagerAdapter extends FragmentStatePagerAdapter {

    private Date today;
    private Boolean camPermission;

    public SubmissionPagerAdapter(FragmentManager fm, Date today) {
        super(fm);
        this.today = today;
        this.camPermission = false;
    }

    public void setCamPermission(boolean camPermission, ViewGroup container){
        this.camPermission = camPermission;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new SubmissionPageFragment();
        Bundle args = new Bundle();
        args.putLong(SubmissionPageFragment.ARG_DATE,
                Date.getPastDate(today, i).toPrimitive());
        args.putBoolean(SubmissionPageFragment.ARG_CAM_PERMISSION,
                this.camPermission);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 365; //arbitrary limit... only the latest year of posts are available
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Date.getPastDate(today, position).toString();
        //SubmissionPageFragment frag = (SubmissionPageFragment)getItem(position);
        //return frag.getPostTitle();
    }
}

