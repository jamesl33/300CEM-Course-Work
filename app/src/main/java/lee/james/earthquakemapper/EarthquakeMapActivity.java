package lee.james.earthquakemapper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class EarthquakeMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = EarthquakeMapActivity.class.getSimpleName();

    private GoogleMap googleMap;
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
            // TODO - Generate a heatmap
        } else {
            for (Earthquake earthquake : this.earthquakes) {
                this.earthquakeMarkers.add(this.googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()))
                        .radius(100000 * earthquake.getMagnitude())
                        // TODO - Add a user preference to allow the user to pick a color
                        .strokeColor(Color.argb(50, 255, 0, 0))
                        .fillColor(Color.argb(100, 255, 0, 0))));

                this.focusCurrentEarthquake(null);
            }
        }
    }

    public void focusCurrentEarthquake(View view) {
        Integer previousEarthquake = this.currentEarthquake == 0 ? this.earthquakeMarkers.size() - 1 : currentEarthquake - 1;

        this.earthquakeMarkers.get(previousEarthquake).setStrokeColor(Color.argb(50, 255, 0, 0));
        this.earthquakeMarkers.get(previousEarthquake).setFillColor(Color.argb(100, 255, 0, 0));
        this.earthquakeMarkers.get(previousEarthquake).setZIndex(0);

        this.earthquakeMarkers.get(currentEarthquake).setStrokeColor(Color.argb(50, 0, 0, 255));
        this.earthquakeMarkers.get(currentEarthquake).setFillColor(Color.argb(100, 0, 0, 255));
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
