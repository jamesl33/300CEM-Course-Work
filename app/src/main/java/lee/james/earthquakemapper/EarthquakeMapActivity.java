package lee.james.earthquakemapper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;

public class EarthquakeMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = EarthquakeMapActivity.class.getSimpleName();

    public static final String KEY_FOCUSED_MARKER_COLOR = "focused_marker_color";
    public static final String KEY_UNFOCUSED_MARKER_COLOR = "unfocused_marker_color";

    // Google
    private GoogleMap googleMap;

    // Floating action button
    private FloatingActionButton fabMapActions;
    private Boolean fabExpanded = false;
    private LinearLayout layoutFabFindCurrentLocation;
    private LinearLayout layoutFabNextEarthquake;
    private LinearLayout layoutFabFocusEarthquake;

    // Earthquakes
    private Integer currentEarthquake = 0;
    private ArrayList<Earthquake> earthquakes;
    private ArrayList<Circle> earthquakeMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        this.fabMapActions = this.findViewById(R.id.fabSetting);

        this.layoutFabNextEarthquake = this.findViewById(R.id.layoutFabNextEarthquake);
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
                        layoutFabFindCurrentLocation.setVisibility(View.VISIBLE);
                        layoutFabNextEarthquake.setVisibility(View.VISIBLE);
                        layoutFabFocusEarthquake.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Close all submenus
                    layoutFabFindCurrentLocation.setVisibility(View.INVISIBLE);
                    layoutFabNextEarthquake.setVisibility(View.INVISIBLE);
                    layoutFabFocusEarthquake.setVisibility(View.INVISIBLE);
                }

                fabExpanded = !fabExpanded;
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
                        .radius(100000 * earthquake.getMagnitude())));

                this.focusCurrentEarthquake(null);
            }
        }
    }

    public void focusCurrentLocation(View view) {
        // TODO - When the user clicks the fabCurrentLocation button focus on their location
    }

    public void focusCurrentEarthquake(View view) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        Integer previousEarthquake = this.currentEarthquake == 0 ? this.earthquakeMarkers.size() - 1 : currentEarthquake - 1;

        this.earthquakeMarkers.get(previousEarthquake).setStrokeColor(sharedPref.getInt(EarthquakeMapActivity.KEY_UNFOCUSED_MARKER_COLOR, 0));
        this.earthquakeMarkers.get(previousEarthquake).setFillColor(sharedPref.getInt(EarthquakeMapActivity.KEY_UNFOCUSED_MARKER_COLOR, 0));
        this.earthquakeMarkers.get(previousEarthquake).setZIndex(0);

        this.earthquakeMarkers.get(currentEarthquake).setStrokeColor(sharedPref.getInt(EarthquakeMapActivity.KEY_FOCUSED_MARKER_COLOR, 0));
        this.earthquakeMarkers.get(currentEarthquake).setFillColor(sharedPref.getInt(EarthquakeMapActivity.KEY_FOCUSED_MARKER_COLOR, 0));
        this.earthquakeMarkers.get(currentEarthquake).setZIndex(Float.POSITIVE_INFINITY);

        this.googleMap.animateCamera(
                CameraUpdateFactory.newLatLng(new LatLng(this.earthquakes.get(this.currentEarthquake).getLatitude(), this.earthquakes.get(this.currentEarthquake).getLongitude())),
                400,
                null
        );
    }

    public void moveToNextEarthquake(View view) {
        if (this.currentEarthquake >= this.earthquakes.size() - 1) {
            this.currentEarthquake = 0;
        } else {
            this.currentEarthquake++;
        }

        this.focusCurrentEarthquake(null);
    }
}
