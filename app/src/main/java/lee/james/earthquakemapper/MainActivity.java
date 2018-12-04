package lee.james.earthquakemapper;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Date startDate;
    private Date endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

    public void pickStartDate(View view) {
        // Use the current date as the default date for the date picker
        final Calendar cal = Calendar.getInstance();

        if (this.startDate != null) {
            cal.setTime(this.startDate);
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                cal.set(year, month, day);
                startDate = cal.getTime();
            }
        }, year, month, day);

        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);

        if (this.startDate == null) {
            startDatePicker.getDatePicker().setMinDate(earthquakeDatabase.getOldest().getDate().getTime());
        }

        startDatePicker.getDatePicker().setMaxDate(earthquakeDatabase.getLatest().getDate().getTime());
        startDatePicker.show();
    }

    public void pickEndDate(View view) {
        // Use the current date as the default date for the date picker
        final Calendar cal = Calendar.getInstance();

        if (this.endDate != null) {
            cal.setTime(this.endDate);
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog endDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                cal.set(year, month, day);
                endDate = cal.getTime();
            }
        }, year, month, day);

        if (this.startDate != null) {
            endDatePicker.getDatePicker().setMinDate(this.startDate.getTime());
        }

        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);
        endDatePicker.getDatePicker().setMaxDate(earthquakeDatabase.getLatest().getDate().getTime());
        endDatePicker.show();
    }

    public void visualizeEarthquakes(View view) {
        if (this.startDate == null || this.endDate == null) {
            Toast.makeText(this, "You must choose filtering dates!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EarthquakeMapActivity.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

        try {
            intent.putExtra("StartDate", dateFormat.format(this.startDate));
            intent.putExtra("EndDate", dateFormat.format(this.endDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(intent);
    }

}