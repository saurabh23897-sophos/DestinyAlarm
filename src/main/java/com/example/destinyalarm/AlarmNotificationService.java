package com.example.destinyalarm;

import static com.example.destinyalarm.Utils.Constants.alarmActivityContext;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_CHANNEL_DESCRIPTION;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_CHANNEL_ID;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_CHANNEL_NAME;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_CONTENT_TITLE;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_ID;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_MESSAGE;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_SERVICE_NAME;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_STYLE;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_VIBRATION_PATTERN;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlarmNotificationService extends IntentService {
    NotificationManager alarmNotificationManager;

    public AlarmNotificationService() {
        super(NOTIFICATION_SERVICE_NAME);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        sendNotification();
    }

    //handle notification
    private void sendNotification() {
        alarmNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, AlarmActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder alarmNotificationBuilder =
                new NotificationCompat.Builder(alarmActivityContext, NOTIFICATION_CHANNEL_ID)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setStyle(NOTIFICATION_STYLE)
                        .setContentText(NOTIFICATION_MESSAGE)
                        .setContentText(NOTIFICATION_CONTENT_TITLE)
                        .setContentTitle(NOTIFICATION_CONTENT_TITLE)
                        .setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                            NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(NOTIFICATION_VIBRATION_PATTERN);
            notificationChannel.enableVibration(true);
            alarmNotificationManager.createNotificationChannel(notificationChannel);
            alarmNotificationBuilder.setPriority(NotificationManager.IMPORTANCE_MAX);
        }

        log.info("Notify with message : {}", NOTIFICATION_MESSAGE);
        alarmNotificationManager.notify(NOTIFICATION_ID, alarmNotificationBuilder.build());
    }


}