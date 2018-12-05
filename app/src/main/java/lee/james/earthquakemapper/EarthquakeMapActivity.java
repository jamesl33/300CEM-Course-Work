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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

    private static final String LOG_TAG = EarthquakeMapActivity.class.getSimpleName();

    public static final String KEY_FOCUSED_MARKER_COLOR = "focused_marker_color";
    public static final String KEY_UNFOCUSED_MARKER_COLOR = "unfocused_marker_color";

    // Google
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Floating action button
    private FloatingActionButton fabMapActions;
    private Boolean fabExpanded = false;
    private LinearLayout layoutFabFindCurrentLocation;
    private LinearLayout layoutFabNextEarthquake;
    private LinearLayout layoutFabPreviousEarthquake;
    private LinearLayout layoutFabFocusEarthquake;

    // Earthquakes
    private Integer currentEarthquake;
    private ArrayList<Earthquake> earthquakes;
    private ArrayList<Circle> earthquakeMarkers = new ArrayList<>();

    // Markers
    private LatLng currentLocation;
    private Marker currentLocationMarker;

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
                this.currentEarthquake = savedInstanceState.getInt("currentEarthquake");
            }

            if (savedInstanceState.containsKey("my/latitude") && savedInstanceState.containsKey("my/longitude")) {
                this.currentLocation = new LatLng(savedInstanceState.getDouble("my/latitude"), savedInstanceState.getDouble("my/longitude"));
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        this.fabMapActions = this.findViewById(R.id.fabSetting);

        this.layoutFabNextEarthquake = this.findViewById(R.id.layoutFabNextEarthquake);
        this.layoutFabPreviousEarthquake = this.findViewById(R.id.layoutFabPreviousEarthquake);
        this.layoutFabFocusEarthquake = this.findViewById(R.id.layoutFabFocusEarthquake);
        this.layoutFabFindCurrentLocation = this.findViewById(R.id.layoutFabFindCurrentLocation);

        this.fabMapActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fabExpanded) {
                    if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_HEATMAP_SWITCH, false)) {
                        // Open submenus that are relevant to the earthquake heatmap
                        layoutFabFindCurrentLocation.setVisibility(View.VISIBLE);
                    } else {
                        // Open submenus that are relevant to the earthquake marker map
                        layoutFabNextEarthquake.setVisibility(View.VISIBLE);
                        layoutFabPreviousEarthquake.setVisibility(View.VISIBLE);
                        layoutFabFocusEarthquake.setVisibility(View.VISIBLE);
                        layoutFabFindCurrentLocation.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Close all submenus
                    layoutFabNextEarthquake.setVisibility(View.INVISIBLE);
                    layoutFabPreviousEarthquake.setVisibility(View.INVISIBLE);
                    layoutFabFocusEarthquake.setVisibility(View.INVISIBLE);
                    layoutFabFindCurrentLocation.setVisibility(View.INVISIBLE);

                }

                fabExpanded = !fabExpanded;
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake() {
                if (!sharedPref.getBoolean(SettingsActivity.KEY_PREF_HEATMAP_SWITCH, false)) {
                    int previousEarthquake = currentEarthquake;
                    currentEarthquake = new Random().nextInt(earthquakes.size());
                    focusCurrentEarthquake(previousEarthquake);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap; // Set this as a class attribute so the other functions can access it

        Intent intent = getIntent();
        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String earthquakeCount = sharedPref.getString(SettingsActivity.KEY_PREF_EARTHQUAKE_COUNT, "All");

        if (earthquakeCount.equals("All")) {
            this.earthquakes = earthquakeDatabase.getEarthquakes(intent.getStringExtra("StartDate"), intent.getStringExtra("EndDate"));
        } else {
            this.earthquakes = earthquakeDatabase.getEarthquakes(intent.getStringExtra("StartDate"), intent.getStringExtra("EndDate"), Integer.valueOf(earthquakeCount));
        }

        // Make sure that there is actually some earthquakes
        if (this.earthquakes.size() <= 0) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("message", "There were no significant earthquake during that time period");
            setResult(RESULT_CANCELED, returnIntent);
            this.finish();
            return;
        }

        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_HEATMAP_SWITCH, false)) {
            ArrayList<WeightedLatLng> heatmapData = new ArrayList<>();

            for (Earthquake earthquake : this.earthquakes) {
                heatmapData.add(new WeightedLatLng(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()), earthquake.getMagnitude()));
            }

            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .weightedData(heatmapData)
                    .radius(50)
                    .opacity(1)
                    .build();

            this.googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        } else {
            for (Earthquake earthquake : this.earthquakes) {
                this.earthquakeMarkers.add(this.googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()))
                        .radius(100000 * earthquake.getMagnitude())
                        .strokeColor(sharedPref.getInt(EarthquakeMapActivity.KEY_UNFOCUSED_MARKER_COLOR, 0))
                        .fillColor(sharedPref.getInt(EarthquakeMapActivity.KEY_UNFOCUSED_MARKER_COLOR, 0))));
            }

            if (this.currentEarthquake == null) {
                this.currentEarthquake = 0;
                this.focusNextEarthquake();
            } else {
                this.earthquakeMarkers.get(this.currentEarthquake).setStrokeColor(sharedPref.getInt(EarthquakeMapActivity.KEY_FOCUSED_MARKER_COLOR, 0));
                this.earthquakeMarkers.get(this.currentEarthquake).setFillColor(sharedPref.getInt(EarthquakeMapActivity.KEY_FOCUSED_MARKER_COLOR, 0));
            }
        }

        // If we already have the users location, redraw the marker
        if (this.currentLocation != null) {
            this.currentLocationMarker = this.googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(this.currentLocation.latitude, this.currentLocation.longitude))
                    .title("My Location"));
        }

        earthquakeDatabase.close(); // Ensure that we close the database once we are finished with it
    }

    public void focusCurrentLocation(View view) {
        if (!this.mLocationPermissionGranted) {
            getLocationPermission();
        }

        Location location = getLastKnownLocation();

        if (location == null) {
            Toast.makeText(this, "Unable to find your location", Toast.LENGTH_SHORT).show();
        } else {
            // If we don't have the users location or it's outdated; update it
            if (this.currentLocation == null || (this.currentLocation.latitude != location.getLatitude() && this.currentLocation.longitude != location.getLongitude())) {
                if (this.currentLocationMarker != null) {
                    this.currentLocationMarker.remove();
                }

                this.currentLocationMarker = this.googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("My Location"));

                this.currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }

            // Move the camera to the user location marker
            this.googleMap.animateCamera(
                    CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())), 400, null
            );
        }
    }

    public void _moveCameraToEarthquake() {
        this.googleMap.animateCamera(
                CameraUpdateFactory.newLatLng(new LatLng(this.earthquakes.get(this.currentEarthquake).getLatitude(), this.earthquakes.get(this.currentEarthquake).getLongitude())),
                400,
                null
        );
    }

    public void focusCurrentEarthquake(View view) {
        this._moveCameraToEarthquake();
    }

    public void focusCurrentEarthquake(int previousEarthquake) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Recolor the previous marker as it is now an unfocused marker
        this.earthquakeMarkers.get(previousEarthquake).setStrokeColor(sharedPref.getInt(EarthquakeMapActivity.KEY_UNFOCUSED_MARKER_COLOR, 0));
        this.earthquakeMarkers.get(previousEarthquake).setFillColor(sharedPref.getInt(EarthquakeMapActivity.KEY_UNFOCUSED_MARKER_COLOR, 0));
        this.earthquakeMarkers.get(previousEarthquake).setZIndex(0);

        // Recolor the current marker as it is now the focused marker
        this.earthquakeMarkers.get(currentEarthquake).setStrokeColor(sharedPref.getInt(EarthquakeMapActivity.KEY_FOCUSED_MARKER_COLOR, 0));
        this.earthquakeMarkers.get(currentEarthquake).setFillColor(sharedPref.getInt(EarthquakeMapActivity.KEY_FOCUSED_MARKER_COLOR, 0));
        this.earthquakeMarkers.get(currentEarthquake).setZIndex(Float.POSITIVE_INFINITY);

        this._moveCameraToEarthquake();
    }

    public void focusNextEarthquake() {
        this.focusCurrentEarthquake(this.currentEarthquake == 0 ? this.earthquakeMarkers.size() - 1 : currentEarthquake - 1);
    }

    public void focusPreviousEarthquake() {
        this.focusCurrentEarthquake(this.currentEarthquake == this.earthquakeMarkers.size() - 1 ? 0 : currentEarthquake + 1);
    }

    public void moveToNextEarthquake(View view) {
        // If the user has looked at all the markers; loop
        if (this.currentEarthquake >= this.earthquakes.size() - 1) {
            this.currentEarthquake = 0;
        } else {
            this.currentEarthquake++;
        }

        // Focus the new current marker
        this.focusNextEarthquake();
    }

    public void moveToPreviousEarthquake(View view) {
        // If the user is going beyond the first marker; loop
        if (this.currentEarthquake == 0) {
            this.currentEarthquake = this.earthquakes.size() - 1;
        } else {
            this.currentEarthquake--;
        }

        // Focus the new current marker
        this.focusPreviousEarthquake();
    }

    private Location getLastKnownLocation() {
        Location currentLocation = null;
        LocationManager mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.mLocationPermissionGranted = true;
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
        this.mLocationPermissionGranted = false;

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
        if (this.currentEarthquake != null) {
            outState.putInt("currentEarthquake", this.currentEarthquake);
        }

        // currentLocation will be null if the user hasn't allowed permission or hasn't clicked the
        // current location button yet
        if (this.currentLocation != null) {
            outState.putDouble("my/latitude", this.currentLocation.latitude);
            outState.putDouble("my/longitude", this.currentLocation.longitude);
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
