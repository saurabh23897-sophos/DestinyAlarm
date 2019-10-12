package com.example.destinyalarm.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Style;

import lombok.Setter;

public class Constants {
    public static final float THRESHOLD_DISTANCE_IN_METERS = 400;

    public static final int ALARM_DELAY = 5;
    public static final int ALARM_REQUEST_CODE = 133;
    public static final int FLAGS = 0;
    public static final int NOTIFICATION_ID = 1;
    public static final int PERMISSION_REQUEST_CODE = 101;
    public static final int SLEEP_DELAY = 5000;

    public static final long[] NOTIFICATION_VIBRATION_PATTERN = new long[]{0, 1000, 500, 1000};

    public static final String ALARM_MESSAGE = "Your Destination Is Near!";
    public static final String ALARM_SET = "Alarm Set For Your Destination!";
    public static final String ALARM_STOPPED = "Alarm Stopped!";
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

    public static final String MOCK_FEATURE_GEOJSON = "{\n" +
            "  \"type\": \"FeatureCollection\",\n" +
            "  \"features\": [\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"marker-color\": \"#7e7e7e\",\n" +
            "        \"marker-size\": \"medium\",\n" +
            "        \"marker-symbol\": \"\",\n" +
            "        \"name\": \"Bangalore\"\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [\n" +
            "          77.575279,\n" +
            "          12.976750\n" +
            "        ]\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"marker-color\": \"#7e7e7e\",\n" +
            "        \"marker-size\": \"medium\",\n" +
            "        \"marker-symbol\": \"\",\n" +
            "        \"name\": \"Custom Marker\"\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [\n" +
            "          78.075279,\n" +
            "          12.976750\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
