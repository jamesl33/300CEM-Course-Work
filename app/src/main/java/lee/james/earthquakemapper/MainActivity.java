package lee.james.earthquakemapper;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final int MAP_EARTHQUAKES_REQUEST = 1;

    private EarthquakeDatabaseHelper mEarthquakeDatabase;
    private AsyncTask mDatabaseUpdater;

    private Boolean mDatesEdited = false;
    private Date mStartDate;
    private Date mEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

            try {
                if (savedInstanceState.containsKey("mStartDate")) {
                    mStartDate = dateFormat.parse(savedInstanceState.getString("mStartDate"));
                }

                if (savedInstanceState.containsKey("mEndDate")) {
                    mEndDate = dateFormat.parse(savedInstanceState.getString("mEndDate"));
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
        mEarthquakeDatabase = new EarthquakeDatabaseHelper(this);
        mStartDate = mEarthquakeDatabase.getLatest().getDate();
        mEndDate = mEarthquakeDatabase.getLatest().getDate();
        updateDatePreviews();

        // Make the app description scrollable if it doesn't fit on the users display
        TextView app_description = findViewById(R.id.text_view_app_description);
        app_description.setMovementMethod(new ScrollingMovementMethod());

        // Update the api database
        updateDatabase();
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

        if (!mDatesEdited) {
            // If the database is updated and the user hasn't picked a date yet; update them.
            mStartDate = mEarthquakeDatabase.getLatest().getDate();
            mEndDate = mEarthquakeDatabase.getLatest().getDate();
        }

        text_view_start_date.setText(dateFormat.format(mStartDate));
        text_view_end_date.setText(dateFormat.format(mStartDate));
    }

    public void pickStartDate(View view) {
        // Use the current date as the default date for the date picker
        final Calendar cal = Calendar.getInstance();

        cal.setTime(mStartDate);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                cal.set(year, month, day);
                mStartDate = cal.getTime();
                mDatesEdited = true;
                updateDatePreviews();
            }
        }, year, month, day);

        startDatePicker.getDatePicker().setMinDate(mEarthquakeDatabase.getOldest().getDate().getTime());
        startDatePicker.getDatePicker().setMaxDate(mEarthquakeDatabase.getLatest().getDate().getTime());
        startDatePicker.getDatePicker().setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        startDatePicker.show();
    }

    public void pickEndDate(View view) {
        // Use the current date as the default date for the date picker
        final Calendar cal = Calendar.getInstance();

        if (mEndDate != null) {
            cal.setTime(mEndDate);
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog endDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                cal.set(year, month, day);
                mEndDate = cal.getTime();
                mDatesEdited = true;
                updateDatePreviews();
            }
        }, year, month, day);

        endDatePicker.getDatePicker().setMinDate(mStartDate.getTime());
        endDatePicker.getDatePicker().setMaxDate(mEarthquakeDatabase.getLatest().getDate().getTime());
        endDatePicker.getDatePicker().setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        endDatePicker.show();
    }

    public void visualizeEarthquakes(View view) {
        Intent intent = new Intent(this, EarthquakeMapActivity.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

        try {
            intent.putExtra("StartDate", dateFormat.format(mStartDate));
            intent.putExtra("EndDate", dateFormat.format(mEndDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivityForResult(intent, MainActivity.MAP_EARTHQUAKES_REQUEST);
    }

    public void updateDatabase() {
        // Update the database using the AsyncTask UpdateEarthquakeDatabase
        RequestQueue queue = Volley.newRequestQueue(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        Calendar latest = Calendar.getInstance();
        latest.setTime(mEarthquakeDatabase.getLatest().getDate());
        Calendar today = Calendar.getInstance();

        String updateInterval = sharedPref.getString(SettingsActivity.KEY_PREF_DATABASE_UPDATE_INTERVAL, "A Day Old");
        long daysDiff = TimeUnit.MILLISECONDS.toDays(today.getTimeInMillis() - latest.getTimeInMillis());

        // Allow the user to choose how old the data in the database can get
        if (((daysDiff >= 1) && (updateInterval.equals("A Day Old"))) || (daysDiff >= 7 && (updateInterval.equals("A Week Old")) || (daysDiff >= 30) && (updateInterval.equals("A Month Old")))) {
            // Determine the url which should give us the earthquakes we are missing
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String url = String.format("https://earthquake.usgs.gov/fdsnws/event/1/query?format=csv&starttime=%s&endtime=%s&minsig=500",
                    dateFormat.format(latest.getTime()), dateFormat.format(today.getTime()));

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
        if (mDatabaseUpdater != null) {
            mDatabaseUpdater.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

        outState.putString("mStartDate", dateFormat.format(mStartDate));
        outState.putString("mEndDate", dateFormat.format(mEndDate));

        super.onSaveInstanceState(outState);
    }

}