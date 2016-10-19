package com.hbabaran.rsketchdaily;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by wren on 18/10/2016.
 */

public class Date implements Serializable {
    long unix_maxtime; //11:59 PM
    long unix_mintime; //12:01 AM

    //begin static functions
    public static long getUnixMintime(Calendar date){
        Calendar mintime = new GregorianCalendar(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH),
                0,
                1);
        return mintime.getTimeInMillis() / 1000L;
    }

    public static long getUnixMaxtime(Calendar date){
        Calendar maxtime = new GregorianCalendar(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH),
                23,
                59);
        return maxtime.getTimeInMillis() / 1000L;
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
        return mintime.getTimeInMillis() / 1000L;
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
        return maxtime.getTimeInMillis() / 1000L;
    }


    public Date(Calendar date){
        this.unix_mintime = Date.getUnixMintime(date);
        this.unix_maxtime = Date.getUnixMintime(date);
    }

    public Date(long uTime){
        this.unix_mintime = Date.getUnixMintime(uTime);
        this.unix_maxtime = Date.getUnixMaxtime(uTime);
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
