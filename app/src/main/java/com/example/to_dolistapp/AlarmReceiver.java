package com.example.to_dolistapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "MyChannelID";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("taskName");
        playAlarmTune(context);
        Notification notification = buildNotification(context, taskName);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private void playAlarmTune(Context context) {
        Uri alarmTuneUri = getAlarmTuneUri(context);
        if (alarmTuneUri != null) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(context, alarmTuneUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("AlarmReceiver", "Alarm tune URI is null");
        }
    }


    private Uri getAlarmTuneUri(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String uriString = preferences.getString("alarm_tune_uri", null);
        if (uriString != null) {
            return Uri.parse(uriString);
        }
        return null;
    }


    private Notification buildNotification(Context context, String taskName) {
        // The existing notification creation code remains the same
        NotificationCompat.Builder builder;

        // Create a notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(NotificationChannelHelper.createNotificationChannel(CHANNEL_ID, "My Channel", NotificationManager.IMPORTANCE_HIGH));
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        builder.setContentTitle("Task Reminder")
                .setContentText("Don't forget to do: " + taskName)
                .setSmallIcon(R.drawable.ic_help) // Set your notification icon here
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true);

        return builder.build();
    }
}
