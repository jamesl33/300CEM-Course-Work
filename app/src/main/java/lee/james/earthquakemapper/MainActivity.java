package lee.james.earthquakemapper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
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
            mDatePickerFragment.setFlag(DatePickerFragment.PICK_END_DATE);
        }

        // Show the date picker dialog
        mDatePickerFragment.show(getSupportFragmentManager(), getString(R.string.date_picker));
    }

    public void visualizeEarthquakes(View view) {
        if (this.startDate == null || this.endDate == null) {
            Toast.makeText(this, "You must choose filtering dates!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EarthquakeMapActivity.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        try {
            intent.putExtra("StartDate", dateFormat.format(this.startDate));
            intent.putExtra("EndDate", dateFormat.format(this.endDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(intent);
    }
}