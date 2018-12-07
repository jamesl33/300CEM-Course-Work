package lee.james.earthquakemapper;

import android.os.AsyncTask;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

class UpdateEarthquakeDatabase extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<MainActivity> mMainActivity;

    private String mApiResponse;

    UpdateEarthquakeDatabase(MainActivity mainActivity, String apiResponse) {
        mMainActivity = new WeakReference<>(mainActivity);
        mApiResponse = apiResponse;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        ArrayList<Earthquake> earthquakes = new ArrayList<>();
        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(mMainActivity.get());

        try {
            CSVReader reader = new CSVReaderBuilder(new StringReader(mApiResponse))
                    .withSkipLines(1) // Skip the header
                    .build();

            String[] nextEarthquake;

            while ((nextEarthquake = reader.readNext()) != null) {
                if (isCancelled()) {
                    break;
                }

                earthquakes.add(new Earthquake(
                        null,
                        Float.parseFloat(nextEarthquake[1]),
                        Float.parseFloat(nextEarthquake[2]),
                        Float.parseFloat(nextEarthquake[4]),
                        nextEarthquake[0].split("T")[0].replace('-', '/')
                ));
            }

            // The api results are in the wrong order for our database so we reverse them
            Collections.reverse(earthquakes);

            return earthquakeDatabase.addEarthquakes(earthquakes);
        } catch (IOException error) {
            error.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean successfulAddition) {
        if (successfulAddition) {
            // TODO - Maybe make a notification for this?
            Toast messsage = Toast.makeText(mMainActivity.get(), "Database Update Successful", Toast.LENGTH_SHORT);
            mMainActivity.get().updateDatePreviews();
            messsage.show();
        }
    }

}
