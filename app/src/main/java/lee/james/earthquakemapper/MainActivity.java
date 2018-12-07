package lee.james.earthquakemapper;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final int MAP_EARTHQUAKES_REQUEST = 1;

    private AsyncTask mDatabaseUpdater;

    private Date startDate;
    private Date endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

            try {
                if (savedInstanceState.containsKey("startDate")) {
                    this.startDate = dateFormat.parse(savedInstanceState.getString("startDate"));
                }

                if (savedInstanceState.containsKey("endDate")) {
                    this.endDate = dateFormat.parse(savedInstanceState.getString("endDate"));
                }
            } catch (ParseException error) {
                error.printStackTrace();
            }
        }

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the default values for the start/end date
        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);

        this.startDate = earthquakeDatabase.getLatest().getDate();
        this.endDate = earthquakeDatabase.getLatest().getDate();
        this.updateDatePreviews();

        // Make the app description scrollable if it doesn't fit on the users display
        TextView app_description = findViewById(R.id.text_view_app_description);
        app_description.setMovementMethod(new ScrollingMovementMethod());

        // Update the api database
        this.updateDatabase();
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

    public void updateDatePreviews() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        TextView text_view_start_date = findViewById(R.id.text_view_start_date);
        TextView text_view_end_date = findViewById(R.id.text_view_end_date);

        text_view_start_date.setText(dateFormat.format(this.startDate));
        text_view_end_date.setText(dateFormat.format(this.endDate));
    }

    public void pickStartDate(View view) {
        // Use the current date as the default date for the date picker
        final Calendar cal = Calendar.getInstance();

        cal.setTime(this.startDate);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                cal.set(year, month, day);
                startDate = cal.getTime();
                updateDatePreviews();
            }
        }, year, month, day);

        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);
        startDatePicker.getDatePicker().setMinDate(earthquakeDatabase.getOldest().getDate().getTime());
        startDatePicker.getDatePicker().setMaxDate(earthquakeDatabase.getLatest().getDate().getTime());
        startDatePicker.getDatePicker().setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
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
                updateDatePreviews();
            }
        }, year, month, day);

        endDatePicker.getDatePicker().setMinDate(this.startDate.getTime());
        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);
        endDatePicker.getDatePicker().setMaxDate(earthquakeDatabase.getLatest().getDate().getTime());
        endDatePicker.getDatePicker().setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        endDatePicker.show();
    }

    public void visualizeEarthquakes(View view) {
        Intent intent = new Intent(this, EarthquakeMapActivity.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

        try {
            intent.putExtra("StartDate", dateFormat.format(this.startDate));
            intent.putExtra("EndDate", dateFormat.format(this.endDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivityForResult(intent, MainActivity.MAP_EARTHQUAKES_REQUEST);
    }

    public void updateDatabase() {
        // Update the database using the AsyncTask UpdateEarthquakeDatabase
        RequestQueue queue = Volley.newRequestQueue(this);
        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(this);

        Date latest = earthquakeDatabase.getLatest().getDate();
        Date today = new Date();

        // Only update once a day TODO - Possibly make a setting to modify this?
        if (latest != today) {
            // Determine the url which should give us the earthquakes we are missing
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String url = String.format("https://earthquake.usgs.gov/fdsnws/event/1/query?format=csv&starttime=%s&endtime=%s&minsig=500",
                    dateFormat.format(latest), dateFormat.format(today));

            // This request will fail if there are over 20,000 results. This is highly unlikely since there was only 19487 earthquakes
            // with a significance value of over 500 since 1950 to today (06/12/18). If it fails due to lack of internet its should
            // just try again when the user next launches the app.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    mDatabaseUpdater = new UpdateEarthquakeDatabase(MainActivity.this, response).execute();
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            queue.add(stringRequest);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Inform the user if the maps activity closed with the canceled flag
        if (data != null) {
            if (requestCode == MainActivity.MAP_EARTHQUAKES_REQUEST && resultCode == RESULT_CANCELED) {
                Toast errorMessage = Toast.makeText(this, data.getStringExtra("message"), Toast.LENGTH_LONG);
                errorMessage.show();
            }
        }
    }

    @Override
    public void onDestroy() {
        // Stop updating the database when the app is about to be destroyed
        this.mDatabaseUpdater.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

        outState.putString("startDate", dateFormat.format(this.startDate));
        outState.putString("endDate", dateFormat.format(this.endDate));

        super.onSaveInstanceState(outState);
    }

}