package lee.james.earthquakemapper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EarthquakeMapActivity extends FragmentActivity implements OnMapReadyCallback {

    // Permissions
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Google
    private GoogleMap mGoogleMap;
    private boolean mLocationPermissionGranted;

    // Floating action button
    private Boolean mFabExpanded = false;
    private LinearLayout mLayoutFabFindCurrentLocation;
    private LinearLayout mLayoutFabNextEarthquake;
    private LinearLayout mLayoutFabPreviousEarthquake;
    private LinearLayout mLayoutFabFocusEarthquake;

    // Earthquakes
    private Integer mCurrentEarthquake;
    private ArrayList<Earthquake> mEarthquakes;
    private ArrayList<Circle> mEarthquakeMarkers = new ArrayList<>();

    // Markers
    private LatLng mCurrentLocation;
    private Marker mCurrentLocationMarker;

    // Shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_map);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("currentEarthquake")) {
                mCurrentEarthquake = savedInstanceState.getInt("currentEarthquake");
            }

            if (savedInstanceState.containsKey("my/latitude") && savedInstanceState.containsKey("my/longitude")) {
                mCurrentLocation = new LatLng(savedInstanceState.getDouble("my/latitude"), savedInstanceState.getDouble("my/longitude"));
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        FloatingActionButton mFabMapActions = findViewById(R.id.fabSetting);

        mLayoutFabNextEarthquake = findViewById(R.id.layoutFabNextEarthquake);
        mLayoutFabPreviousEarthquake = findViewById(R.id.layoutFabPreviousEarthquake);
        mLayoutFabFocusEarthquake = findViewById(R.id.layoutFabFocusEarthquake);
        mLayoutFabFindCurrentLocation = findViewById(R.id.layoutFabFindCurrentLocation);

        mFabMapActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mFabExpanded) {
                    if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_HEATMAP_SWITCH, false)) {
                        // Open submenus that are relevant to the earthquake heatmap
                        mLayoutFabFindCurrentLocation.setVisibility(View.VISIBLE);
                    } else {
                        // Open submenus that are relevant to the earthquake marker map
                        mLayoutFabNextEarthquake.setVisibility(View.VISIBLE);
                        mLayoutFabPreviousEarthquake.setVisibility(View.VISIBLE);
                        mLayoutFabFocusEarthquake.setVisibility(View.VISIBLE);
                        mLayoutFabFindCurrentLocation.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Close all submenus
                    mLayoutFabNextEarthquake.setVisibility(View.INVISIBLE);
                    mLayoutFabPreviousEarthquake.setVisibility(View.INVISIBLE);
                    mLayoutFabFocusEarthquake.setVisibility(View.INVISIBLE);
                    mLayoutFabFindCurrentLocation.setVisibility(View.INVISIBLE);

                }

                mFabExpanded = !mFabExpanded;
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake() {
                if (!sharedPref.getBoolean(SettingsActivity.KEY_PREF_HEATMAP_SWITCH, false)) {
                    int previousEarthquake = mCurrentEarthquake;
                    mCurrentEarthquake = new Random().nextInt(mEarthquakes.size());
                    focusCurrentEarthquake(previousEarthquake);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap; // Set this as a class attribute so the other functions can access it

        Intent intent = getIntent();
        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String earthquakeCount = sharedPref.getString(SettingsActivity.KEY_PREF_EARTHQUAKE_COUNT, "All");

        if (earthquakeCount.equals("All")) {
            mEarthquakes = earthquakeDatabase.getEarthquakes(intent.getStringExtra("StartDate"),
                    intent.getStringExtra("EndDate"));
        } else {
            mEarthquakes = earthquakeDatabase.getEarthquakes(intent.getStringExtra("StartDate"),
                    intent.getStringExtra("EndDate"), Integer.valueOf(earthquakeCount));
        }

        // Make sure that there is actually some earthquakes
        if (mEarthquakes.size() <= 0) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("message", "There were no significant earthquake during that time period");
            setResult(RESULT_CANCELED, returnIntent);
            finish();
            return;
        }

        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_HEATMAP_SWITCH, false)) {
            generateHeatMap();
        } else {
            placeEarthquakeMarkers(sharedPref.getInt(SettingsActivity.KEY_FOCUSED_MARKER_COLOR, 0),
                    sharedPref.getInt(SettingsActivity.KEY_UNFOCUSED_MARKER_COLOR, 0));
        }

        // If we already have the users location, redraw the marker
        if (mCurrentLocation != null) {
            mCurrentLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude))
                    .title("My Location"));
        }

        earthquakeDatabase.close(); // Ensure that we close the database once we are finished with it
    }

    public void placeEarthquakeMarkers(int focusedColor, int unfocusedColor) {
        for (Earthquake earthquake : mEarthquakes) {
            mEarthquakeMarkers.add(mGoogleMap.addCircle(new CircleOptions()
                    .center(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()))
                    .radius(100000 * earthquake.getMagnitude())
                    .strokeColor(unfocusedColor)
                    .fillColor(unfocusedColor)));
        }

        if (mCurrentEarthquake == null) {
            mCurrentEarthquake = 0;
            focusNextEarthquake();
        } else {
            mEarthquakeMarkers.get(mCurrentEarthquake).setStrokeColor(focusedColor);
            mEarthquakeMarkers.get(mCurrentEarthquake).setFillColor(focusedColor);
        }
    }

    public void generateHeatMap() {
        ArrayList<WeightedLatLng> heatmapData = new ArrayList<>();

        for (Earthquake earthquake : mEarthquakes) {
            heatmapData.add(new WeightedLatLng(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()), earthquake.getMagnitude()));
        }

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .weightedData(heatmapData)
                .radius(50)
                .opacity(1)
                .build();

        mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    public void focusCurrentLocation(View view) {
        if (!mLocationPermissionGranted) {
            getLocationPermission();
        }

        Location location = getLastKnownLocation();

        if (location == null) {
            Toast.makeText(this, "Unable to find your location", Toast.LENGTH_SHORT).show();
        } else {
            // If we don't have the users location or it's outdated; update it
            if (mCurrentLocation == null || (mCurrentLocation.latitude != location.getLatitude() && mCurrentLocation.longitude != location.getLongitude())) {
                if (mCurrentLocationMarker != null) {
                    mCurrentLocationMarker.remove();
                }

                mCurrentLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("My Location"));

                mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }

            // Move the camera to the user location marker
            mGoogleMap.animateCamera(
                    CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())), 400, null
            );
        }
    }

    public void _moveCameraToEarthquake() {
        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLng(new LatLng(mEarthquakes.get(mCurrentEarthquake).getLatitude(), mEarthquakes.get(mCurrentEarthquake).getLongitude())),
                400,
                null
        );
    }

    public void focusCurrentEarthquake(View view) {
        _moveCameraToEarthquake();
    }

    public void focusCurrentEarthquake(int previousEarthquake) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Recolor the previous marker as it is now an unfocused marker
        mEarthquakeMarkers.get(previousEarthquake).setStrokeColor(sharedPref.getInt(SettingsActivity.KEY_UNFOCUSED_MARKER_COLOR, 0));
        mEarthquakeMarkers.get(previousEarthquake).setFillColor(sharedPref.getInt(SettingsActivity.KEY_UNFOCUSED_MARKER_COLOR, 0));
        mEarthquakeMarkers.get(previousEarthquake).setZIndex(0);

        // Recolor the current marker as it is now the focused marker
        mEarthquakeMarkers.get(mCurrentEarthquake).setStrokeColor(sharedPref.getInt(SettingsActivity.KEY_FOCUSED_MARKER_COLOR, 0));
        mEarthquakeMarkers.get(mCurrentEarthquake).setFillColor(sharedPref.getInt(SettingsActivity.KEY_FOCUSED_MARKER_COLOR, 0));
        mEarthquakeMarkers.get(mCurrentEarthquake).setZIndex(Float.POSITIVE_INFINITY);

        _moveCameraToEarthquake();
    }

    public void focusNextEarthquake() {
        focusCurrentEarthquake(mCurrentEarthquake == 0 ? mEarthquakeMarkers.size() - 1 : mCurrentEarthquake - 1);
    }

    public void focusPreviousEarthquake() {
        focusCurrentEarthquake(mCurrentEarthquake == mEarthquakeMarkers.size() - 1 ? 0 : mCurrentEarthquake + 1);
    }

    public void moveToNextEarthquake(View view) {
        // If the user has looked at all the markers; loop
        if (mCurrentEarthquake >= mEarthquakes.size() - 1) {
            mCurrentEarthquake = 0;
        } else {
            mCurrentEarthquake++;
        }

        // Focus the new current marker
        focusNextEarthquake();
    }

    public void moveToPreviousEarthquake(View view) {
        // If the user is going beyond the first marker; loop
        if (mCurrentEarthquake == 0) {
            mCurrentEarthquake = mEarthquakes.size() - 1;
        } else {
            mCurrentEarthquake--;
        }

        // Focus the new current marker
        focusPreviousEarthquake();
    }

    private Location getLastKnownLocation() {
        Location currentLocation = null;
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> enabledProviders = mLocationManager.getProviders(true);

        for (String provider : enabledProviders) {
            // We should be able to safely ignore the warning about location permissions since we are
            // handling getting the users permission.
            @SuppressLint("MissingPermission") Location testLocation = mLocationManager.getLastKnownLocation(provider);

            // Update the location if its more accurate than the old location
            if (testLocation != null) {
                if ((currentLocation == null) || (testLocation.getAccuracy() > currentLocation.getAccuracy())) {
                    currentLocation = testLocation;
                }
            }
        }

        return currentLocation;
    }

    /**
     * Prompts the user for permission to use the device location.
     * https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, EarthquakeMapActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     * https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case EarthquakeMapActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // currentEarthquake will be null if we are in heatmap mode
        if (mCurrentEarthquake != null) {
            outState.putInt("currentEarthquake", mCurrentEarthquake);
        }

        // currentLocation will be null if the user hasn't allowed permission or hasn't clicked the
        // current location button yet
        if (mCurrentLocation != null) {
            outState.putDouble("my/latitude", mCurrentLocation.latitude);
            outState.putDouble("my/longitude", mCurrentLocation.longitude);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

}
