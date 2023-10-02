package com.example.to_dolistapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationChannelHelper {

    public static NotificationChannel createNotificationChannel(String channelId, CharSequence channelName, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationChannel(channelId, channelName, importance);
        }
        return null;
    }
}

