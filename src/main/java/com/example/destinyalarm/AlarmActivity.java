package com.example.destinyalarm;

import static com.example.destinyalarm.Utils.Constants.ALARM_DELAY;
import static com.example.destinyalarm.Utils.Constants.ALARM_MESSAGE;
import static com.example.destinyalarm.Utils.Constants.ALARM_REQUEST_CODE;
import static com.example.destinyalarm.Utils.Constants.ALARM_SET;
import static com.example.destinyalarm.Utils.Constants.ALARM_STOPPED;
import static com.example.destinyalarm.Utils.Constants.FLAGS;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_ID;
import static com.example.destinyalarm.Utils.Constants.PERMISSION_REQUEST_CODE;
import static com.example.destinyalarm.Utils.Constants.SLEEP_DELAY;
import static com.example.destinyalarm.Utils.Constants.THRESHOLD_DISTANCE_IN_METERS;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.destinyalarm.Utils.Constants;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlarmActivity extends AppCompatActivity implements OnMapReadyCallback {

    PendingIntent pendingIntent;

    private boolean setAlarm, setDestination;
    private double distanceToDestination;
    private Button alarmButton;
    private Thread locationBasedAlarmTriggerThread;

    private Location destinationLocation;
    private MapView mapView;
    private MapboxMap mapboxMap;

    private Bundle activitySavedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_accessToken));
        setContentView(R.layout.activity_main);
        Constants.setAlarmActivityContext(this);
        activitySavedInstanceState = savedInstanceState;

        if (arePermissionRequiredForLocation()) {
            requestPermissions();
        } else {
            initAll();
        }
    }

    private void initAll() {
        Intent alarmIntent = new Intent(AlarmActivity.this, AlarmBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(AlarmActivity.this, ALARM_REQUEST_CODE, alarmIntent, FLAGS);

        alarmButton = findViewById(R.id.alarmTriggerButton);
        alarmButton.setEnabled(false);
        alarmButton.setOnClickListener(v -> triggerAlarm());

        findViewById(R.id.setLocationButton).setOnClickListener(v -> setDestination());

        locationBasedAlarmTriggerThread = new Thread(this::locationBasedAlarmTrigger);
        locationBasedAlarmTriggerThread.start();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(activitySavedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            log.info("MapBox Map has been loaded");
            enableLocationComponent(style);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationBasedAlarmTriggerThread = null;
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    log.warn("Permission not granted for {}", permissions[i]);
                    requestPermissions();
                    return;
                }
            }
            initAll();
        }
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    private void triggerAlarm() {
        if (setAlarm) {
            terminateAlarm();
            alarmButton.setEnabled(false);
        } else {
            Toast.makeText(this, ALARM_SET, Toast.LENGTH_SHORT).show();
        }
        setAlarm = !setAlarm;
        setAlarmButtonText();
    }

    private void setDestination() {
        destinationLocation = new Location("");

        setDestination = true;
        alarmButton.setEnabled(true);
        setAlarm = false;
        setAlarmButtonText();
    }

    private void setAlarmButtonText() {
        alarmButton.setText(setAlarm ? R.string.stop_alarm : R.string.set_alarm);
    }

    private boolean arePermissionRequiredForLocation() {
        return !(ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        log.info("Requested Permissions For Location");
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    private void terminateAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }

        stopService(new Intent(AlarmActivity.this, AlarmSoundService.class));

        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }

        Toast.makeText(this, ALARM_STOPPED, Toast.LENGTH_SHORT).show();
    }

    private void locationBasedAlarmTrigger() {
        while (true) {
            if (setAlarm && distanceToDestination < THRESHOLD_DISTANCE_IN_METERS) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, ALARM_DELAY);

                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (manager != null) {
                    manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                }

                runOnUiThread(() -> Toast.makeText(AlarmActivity.this, ALARM_MESSAGE, Toast.LENGTH_SHORT).show());
            }
            try {
                Thread.sleep(SLEEP_DELAY);
            } catch (InterruptedException e) {
                log.error("Exception occurred while introducing delay in while loop", e);
            }
        }
    }
}
