package lee.james.earthquakemapper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class EarthquakeMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = EarthquakeMapActivity.class.getSimpleName();

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
        Intent intent = getIntent();
        ArrayList<Earthquake> earthquakes;
        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String earthquakeCount = sharedPref.getString(SettingsActivity.KEY_PREF_EARTHQUAKE_COUNT, "All");

        if (earthquakeCount.equals("All")) {
            earthquakes = earthquakeDatabase.getEarthquakes(intent.getStringExtra("StartDate"), intent.getStringExtra("EndDate"));
        } else {
            earthquakes = earthquakeDatabase.getEarthquakes(intent.getStringExtra("StartDate"), intent.getStringExtra("EndDate"), Integer.valueOf(earthquakeCount));
        }

        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_HEATMAP_SWITCH, false)) {
            // TODO - Generate a heatmap
        } else {
            for (Earthquake earthquake : earthquakes) {
                googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()))
                        .radius(100000 * earthquake.getMagnitude())
                        // TODO - Add a user preference to allow the user to pick a color
                        .strokeColor(Color.argb(50, 255, 0, 0))
                        .fillColor(Color.argb(100, 255, 0, 0)));
            }
        }
    }
}
