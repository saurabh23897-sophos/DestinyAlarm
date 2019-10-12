package com.example.destinyalarm.Utils;

import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Style;

import lombok.Setter;

public class Constants {
    public static final float THRESHOLD_DISTANCE_IN_METERS = 1000;

    public static final int ALARM_DELAY = 5;
    public static final int ALARM_REQUEST_CODE = 133;
    public static final int ANIMATION_DURATION = 500;
    public static final int FLAGS = 0;
    public static final int NOTIFICATION_ID = 1;
    public static final int PERMISSION_REQUEST_CODE = 101;
    public static final int REQUEST_CODE_AUTOCOMPLETE = 10;
    public static final int SLEEP_DELAY = 5000;

    public static final long[] NOTIFICATION_VIBRATION_PATTERN = new long[]{0, 1000, 500, 1000};

    public static final String ALARM_MESSAGE = "Your Destination Is Near!";
    public static final String ALARM_SET = "Alarm Set For Your Destination!";
    public static final String ALARM_STOPPED = "Alarm Stopped!";
    public static final String MARKER_NAME = "Destination";
    public static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Destination Alarm Channel";
    public static final String NOTIFICATION_CHANNEL_ID = "AlarmChannel";
    public static final String NOTIFICATION_CHANNEL_NAME = "Alarm Notification Channel";
    public static final String NOTIFICATION_CONTENT_TITLE = "Alarm";
    public static final String NOTIFICATION_MESSAGE = "Wake Up! Wake Up! " + ALARM_MESSAGE;
    public static final String NOTIFICATION_SERVICE_NAME = "AlarmNotificationService";

    public static final Style NOTIFICATION_STYLE =
            new NotificationCompat.BigTextStyle().bigText(NOTIFICATION_MESSAGE);

    @SuppressLint("StaticFieldLeak")
    @Setter
    public static Context alarmActivityContext;

    @Setter
    public static JsonObject featureProperties;

    public static final CarmenFeature HOME = CarmenFeature.builder().text("Home")
            .geometry(Point.fromLngLat(12.988107, 77.698714))
            .placeName("Amrutha Sparkling Nest Apartment")
            .id("mapbox-home")
            .properties(new JsonObject())
            .build();
    public static final CarmenFeature WORK = CarmenFeature.builder().text("Office")
            .placeName("Amazon, Taurus 1, Bagmane Constellation Business Park")
            .geometry(Point.fromLngLat(12.979543, 77.697223))
            .id("mapbox-work")
            .properties(new JsonObject())
            .build();
}
