package com.makarand.instashop.Helpers;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Locale;


public class TimeHelper {

    public static String getTimeStringFromTimestamp(Long timestamp) {
        if(timestamp == null) return "";

        Timestamp postedTime = new Timestamp(timestamp);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        long diff = currentTime.getTime() - postedTime.getTime();
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long days = diffHours/24;
        if(diffSeconds < 60) {
            return "Just now";
        } else if(diffMinutes < 60) {
            return diffMinutes + " min ago";
        } else if(diffHours < 24){
            return diffHours + " hrs ago";
        } else if(days <= 1){
            return "Yesterday";
        } else if(days > 1) {
            return days + " days ago";
        }

        Date date = new Date(postedTime.getTime());
        return new java.text.SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
    }
}
