package com.hbabaran.rsketchdaily.Model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by wren on 18/10/2016.
 */

public class Date implements Serializable {
    long unix_maxtime; //11:59 PM
    long unix_mintime; //12:01 AM
    private static long EIGHT_HRS_IN_SECONDS = 28800L;
    private static long TWENTY_FOUR_HRS_IN_SECONDS = EIGHT_HRS_IN_SECONDS * 3;

    //begin static functions
    public static long getUnixMintime(Calendar date){
        Calendar mintime = new GregorianCalendar(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH),
                0,
                1);
        return mintime.getTimeInMillis() / 1000L + EIGHT_HRS_IN_SECONDS;
    }

    public static long getUnixMaxtime(Calendar date){
        Calendar maxtime = new GregorianCalendar(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH),
                23,
                59);
        return maxtime.getTimeInMillis() / 1000L + EIGHT_HRS_IN_SECONDS;
    }

    public static long getUnixMintime(long unixTime){
        Calendar uTime = Calendar.getInstance();
        uTime.setTimeInMillis(unixTime*1000L);
        Calendar mintime = new GregorianCalendar(
                uTime.get(Calendar.YEAR),
                uTime.get(Calendar.MONTH),
                uTime.get(Calendar.DAY_OF_MONTH),
                0,
                1);
        return mintime.getTimeInMillis() / 1000L + EIGHT_HRS_IN_SECONDS;
    }

    public static long getUnixMaxtime(long unixTime){
        Calendar uTime = Calendar.getInstance();
        uTime.setTimeInMillis(unixTime*1000L);
        Calendar maxtime = new GregorianCalendar(
                uTime.get(Calendar.YEAR),
                uTime.get(Calendar.MONTH),
                uTime.get(Calendar.DAY_OF_MONTH),
                23,
                59);
        return maxtime.getTimeInMillis() / 1000L + EIGHT_HRS_IN_SECONDS;
    }


    public static Date getPastDate(Date today, int offsetFromToday){
        return new Date(today.getUnix_mintime() - (TWENTY_FOUR_HRS_IN_SECONDS * offsetFromToday));
    }

    public Date(Calendar date){
        this.unix_mintime = Date.getUnixMintime(date);
        this.unix_maxtime = Date.getUnixMintime(date);
    }

    public Date(long uTime){
        this.unix_mintime = Date.getUnixMintime(uTime);
        this.unix_maxtime = Date.getUnixMaxtime(uTime);
    }

    public Date(){
        this.unix_mintime = Date.getUnixMintime(Calendar.getInstance());
        this.unix_maxtime = Date.getUnixMaxtime(Calendar.getInstance());
    }

    public String toString(){
        Calendar uTime = Calendar.getInstance();
        uTime.setTimeInMillis(this.unix_mintime*1000L);
        String month = uTime.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        return month + " " + uTime.get(Calendar.DAY_OF_MONTH);
    }

    //convert to primitive for parcelable
    public long toPrimitive(){
        return getUnix_mintime();
    }

    public long getUnix_mintime() {
        return this.unix_mintime;
    }
    public long getUnix_maxtime() {
        return this.unix_maxtime;
    }

}
