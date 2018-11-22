package lee.james.earthquakemapper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private DatePickerFragment mDatePickerFragment;

    protected Date startDate;
    protected Date endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatePickerFragment = new DatePickerFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDatePickerDialog(View view) {
        if (view.getId() == R.id.button_start_date) {
            // The user is choosing the start date
            mDatePickerFragment.setFlag(DatePickerFragment.PICK_START_DATE);
        } else {
            // The user is choosing the end date
            mDatePickerFragment.setFlag(DatePickerFragment.PICk_END_DATE);
        }

        // Show the date picker dialog
        mDatePickerFragment.show(getSupportFragmentManager(), getString(R.string.date_picker));
    }

    public void visualizeEarthquakes(View view) {
        // TODO - Launch a google maps activity where we will display the earthquake data
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_HEATMAP_SWITCH, false)) {
            Log.d(LOG_TAG, "launch google maps activity to visualize earthquake data using a heatmap");
        } else {
            Log.d(LOG_TAG, "launch google maps activity to visualize earthquake data");
        }
    }
}