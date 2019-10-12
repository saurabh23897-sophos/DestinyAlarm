package com.example.destinyalarm;

import static com.example.destinyalarm.Utils.Constants.ALARM_DELAY;
import static com.example.destinyalarm.Utils.Constants.ALARM_MESSAGE;
import static com.example.destinyalarm.Utils.Constants.ALARM_REQUEST_CODE;
import static com.example.destinyalarm.Utils.Constants.ALARM_SET;
import static com.example.destinyalarm.Utils.Constants.ALARM_STOPPED;
import static com.example.destinyalarm.Utils.Constants.FLAGS;
import static com.example.destinyalarm.Utils.Constants.MARKER_NAME;
import static com.example.destinyalarm.Utils.Constants.NOTIFICATION_ID;
import static com.example.destinyalarm.Utils.Constants.PERMISSION_REQUEST_CODE;
import static com.example.destinyalarm.Utils.Constants.SLEEP_DELAY;
import static com.example.destinyalarm.Utils.Constants.THRESHOLD_DISTANCE_IN_METERS;
import static com.example.destinyalarm.Utils.Constants.featureProperties;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.destinyalarm.Utils.Constants;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlarmActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    PendingIntent pendingIntent;

    private boolean setAlarm, clearSource;
    private Button alarmButton, destinationButton;
    private Thread locationBasedAlarmTriggerThread;

    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
    private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String PROPERTY_NAME = "name";

    private MapView mapView;
    private MapboxMap mapboxMap;
    private GeoJsonSource source;
    private FeatureCollection featureCollection;
    private LatLng destinationLatLng;

    private Bundle activitySavedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
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

        destinationButton = findViewById(R.id.setLocationButton);
        destinationButton.setEnabled(false);
        destinationButton.setOnClickListener(v -> setDestination());

        locationBasedAlarmTriggerThread = new Thread(this::locationBasedAlarmTrigger);
        locationBasedAlarmTriggerThread.start();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(activitySavedInstanceState);
        mapView.getMapAsync(this);

        JsonObject jsonProperties = new JsonObject();
        jsonProperties.addProperty(PROPERTY_NAME, MARKER_NAME);
        Constants.setFeatureProperties(jsonProperties);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            log.info("MapBox Map has been loaded");
            mapboxMap.addOnMapClickListener(this);
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
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
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
        locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(LocationComponentOptions.builder(this)
                        .elevation(5)
                        .accuracyAlpha(.6f)
                        .accuracyColor(Color.RED)
                        .accuracyAnimationEnabled(true)
                        .build())
                .build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    private void triggerAlarm() {
        if (setAlarm) {
            terminateAlarm();
            destinationButton.setEnabled(true);
            alarmButton.setEnabled(false);
        } else {
            destinationButton.setEnabled(false);
            Toast.makeText(this, ALARM_SET, Toast.LENGTH_SHORT).show();
        }
        setAlarm = !setAlarm;
        setAlarmButtonText();
    }

    private void setDestination() {
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

    private double getDistanceToDestination() {
        double distanceToDestination = Double.MAX_VALUE;
        Location currentLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
        if (currentLocation != null) {
            distanceToDestination =  destinationLatLng.distanceTo(
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
        return distanceToDestination;
    }

    private void locationBasedAlarmTrigger() {
        while (true) {
            if (setAlarm && getDistanceToDestination() < THRESHOLD_DISTANCE_IN_METERS) {
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

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        destinationLatLng = point;
        new LoadGeoJsonDataTask(this, point).execute();
        destinationButton.setEnabled(true);
        return true;
    }

    public void setUpData(final FeatureCollection collection) {
        featureCollection = collection;
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                if (clearSource) {
                    style.removeLayer(CALLOUT_LAYER_ID);
                    style.removeLayer(MARKER_LAYER_ID);
                    style.removeImage(MARKER_IMAGE_ID);
                    style.removeSource(source);
                }

                source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
                style.addSource(source);
                style.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.red_marker));
                style.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                        .withProperties(
                                iconImage(MARKER_IMAGE_ID),
                                iconAllowOverlap(true),
                                iconOffset(new Float[] {0f, -8f})));
                style.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                        .withProperties(
                                iconImage("{name}"),
                                iconAnchor(ICON_ANCHOR_BOTTOM),
                                iconAllowOverlap(true),
                                iconOffset(new Float[] {-2f, -28f}))
                        .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
                clearSource = true;
            });
        }
    }

    private static class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

        private final WeakReference<AlarmActivity> activityRef;
        private final LatLng latLng;

        LoadGeoJsonDataTask(AlarmActivity activity, LatLng latLng) {
            this.activityRef = new WeakReference<>(activity);
            this.latLng = latLng;
        }

        @Override
        protected FeatureCollection doInBackground(Void... params) {
            if (activityRef.get() == null) {
                return null;
            }

            return FeatureCollection.fromFeature(Feature.fromGeometry(
                    Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()), featureProperties));
        }

        @Override
        protected void onPostExecute(FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            AlarmActivity activity = activityRef.get();
            if (featureCollection == null || activity == null) {
                return;
            }

            if (featureCollection.features() != null) {
                for (Feature singleFeature : featureCollection.features()) {
                    singleFeature.addBooleanProperty(PROPERTY_SELECTED, true);
                }
            }

            activity.setUpData(featureCollection);
            new GenerateViewIconTask(activity).execute(featureCollection);
        }
    }

    private static class GenerateViewIconTask extends
            AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

        private final HashMap<String, View> viewMap = new HashMap<>();
        private final WeakReference<AlarmActivity> activityRef;

        GenerateViewIconTask(AlarmActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @SuppressWarnings("WrongThread")
        @Override
        protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
            AlarmActivity activity = activityRef.get();
            if (activity != null) {
                HashMap<String, Bitmap> imagesMap = new HashMap<>();
                LayoutInflater inflater = LayoutInflater.from(activity);

                FeatureCollection featureCollection = params[0];

                if (featureCollection.features() != null) {
                    for (Feature feature : featureCollection.features()) {

                        @SuppressLint("InflateParams") BubbleLayout bubbleLayout = (BubbleLayout)
                                inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);

                        String name = feature.getStringProperty(PROPERTY_NAME);
                        TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
                        titleTextView.setText(name);

                        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                        bubbleLayout.measure(measureSpec, measureSpec);

                        float measuredWidth = bubbleLayout.getMeasuredWidth();

                        bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

                        Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                        imagesMap.put(name, bitmap);
                        viewMap.put(name, bubbleLayout);
                    }
                }

                return imagesMap;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
            super.onPostExecute(bitmapHashMap);
            AlarmActivity activity = activityRef.get();
            if (activity != null && activity.mapboxMap != null && bitmapHashMap != null) {
                activity.mapboxMap.getStyle(style -> style.addImages(bitmapHashMap));
            }
        }
    }

    private static class SymbolGenerator {
        /**
         * Generate a Bitmap from an Android SDK View.
         *
         * @param view the View to be drawn to a Bitmap
         * @return the generated bitmap
         */
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);

            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();

            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }
}
