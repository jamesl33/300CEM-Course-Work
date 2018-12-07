package lee.james.earthquakemapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Class to allow the fetching of data from the sqlite database
 */
class EarthquakeDatabaseHelper extends SQLiteAssetHelper {

    // Database metadata
    private static final String DATABASE_NAME = "EarthquakeDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String EARTHQUAKE_TABLE = "earthquakes";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_MAGNITUDE = "magnitude";
    private static final String KEY_DATE = "date";

    // The actual database objects
    private SQLiteDatabase mWritableDB = getWritableDatabase();
    private SQLiteDatabase mReadableDB = getReadableDatabase();

    EarthquakeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Get the oldest earthquake from the database (The oldest earthquake is the once with the smallest
     * id).
     *
     * @return {Earthquake} - The oldest earthquake in the database
     */
    Earthquake getOldest() {
        Cursor cursor = mReadableDB.query(
                EARTHQUAKE_TABLE,
                null,
                String.format("%s = (select min(%s) from %s)", KEY_ID, KEY_ID, EARTHQUAKE_TABLE),
                null,
                null,
                null,
                null);

        cursor.moveToFirst();

        Earthquake oldest = new Earthquake(
                cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                cursor.getFloat(cursor.getColumnIndex(KEY_LATITUDE)),
                cursor.getFloat(cursor.getColumnIndex(KEY_LONGITUDE)),
                cursor.getFloat(cursor.getColumnIndex(KEY_MAGNITUDE)),
                cursor.getString(cursor.getColumnIndex(KEY_DATE))
        );

        cursor.close();

        return oldest;
    }

    /**
     * Get the earthquake from the database with the latest date (This will be the earthquake with
     * the largest id).
     * @return {Earthquake} - The youngest earthquake in the database.
     */
    Earthquake getLatest() {
        Cursor cursor = mReadableDB.query(
                EARTHQUAKE_TABLE,
                null,
                String.format("%s = (select max(%s) from %s)", KEY_ID, KEY_ID, EARTHQUAKE_TABLE),
                null,
                null,
                null,
                null);

        cursor.moveToFirst();

        Earthquake latest = new Earthquake(
                cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                cursor.getFloat(cursor.getColumnIndex(KEY_LATITUDE)),
                cursor.getFloat(cursor.getColumnIndex(KEY_LONGITUDE)),
                cursor.getFloat(cursor.getColumnIndex(KEY_MAGNITUDE)),
                cursor.getString(cursor.getColumnIndex(KEY_DATE))
        );

        cursor.close();

        return latest;
    }

    /**
     * Get all of the earthquakes from the database that are between two dates.
     * @param startDate - The date to start collecting from
     * @param endDate - The date to stop collecting at (inclusive)
     * @return {ArrayList<Earthqauke>} - All the earthquakes that occurred between 'startDate' and 'endDate'
     */
    ArrayList<Earthquake> getEarthquakes(String startDate, String endDate) {
        return getEarthquakes(startDate, endDate, Integer.MAX_VALUE);
    }

    /**
     * Method override for getEarthquakes(String, String) which allows the user to limit how
     * large ArrayList<Earthquake> can get.
     * @param startDate - The date to start collecting from
     * @param endDate - The date to stop collecting at (inclusive)
     * @param count - The number of earthquakes to get
     * @return {ArrayList<Earthqauke>} - 'count' number of earthquakes that occurred between 'startDate' and 'endDate'
     */
    ArrayList<Earthquake> getEarthquakes(String startDate, String endDate, Integer count) {
        ArrayList<Earthquake> earthquakeList = new ArrayList<>();

        Cursor cursor = mReadableDB.query(
                EARTHQUAKE_TABLE,
                null,
                String.format("%s between ? and ?", KEY_DATE),
                new String[]{startDate, endDate},
                null,
                null,
                null);

        cursor.moveToFirst();

        if (cursor.moveToFirst()) {
            int currentCount = 0;

            while (currentCount < count && !cursor.isAfterLast()) {
                earthquakeList.add(new Earthquake(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getFloat(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getFloat(cursor.getColumnIndex(KEY_LONGITUDE)),
                        cursor.getFloat(cursor.getColumnIndex(KEY_MAGNITUDE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE))
                ));

                currentCount++;
                cursor.moveToNext();
            }

        }

        cursor.close();

        return earthquakeList;
    }

    /**
     * Add all the earthquakes from a collection into the database. (This function is used in the AsyncTask to auto update the database).
     * @param earthquakes - The collection of earthquakes as obtained from the USGS web api.
     * @return - True if any earthquake was added to the database. False if no addition were made.
     */
    Boolean addEarthquakes(ArrayList<Earthquake> earthquakes) {
        // Remember to reverse the ArrayList if it's coming from the USGS
        Boolean successfulAddition = false;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

        for (Earthquake earthquake : earthquakes) {
            Cursor cursor = mReadableDB.query(
                    EARTHQUAKE_TABLE,
                    null,
                    String.format("%s = ? and %s = ? and %s = ? and %s = ?", KEY_LATITUDE, KEY_LONGITUDE, KEY_MAGNITUDE, KEY_DATE),
                    new String[]{earthquake.getLatitude().toString(), earthquake.getLongitude().toString(), earthquake.getMagnitude().toString(), dateFormat.format(earthquake.getDate())},
                    null,
                    null,
                    null
            );

            // This value will be zero if the earthquake doesn't already exist in the database
            if (cursor.getCount() == 0) {
                Earthquake previousEarthquake = getLatest();

                ContentValues values = new ContentValues();

                values.put(KEY_ID, previousEarthquake.getId() + 1);
                values.put(KEY_LATITUDE, earthquake.getLatitude().toString());
                values.put(KEY_LONGITUDE, earthquake.getLongitude().toString());
                values.put(KEY_MAGNITUDE, earthquake.getMagnitude().toString());
                values.put(KEY_DATE, dateFormat.format(earthquake.getDate()));

                mWritableDB.insert(EARTHQUAKE_TABLE, null, values);

                successfulAddition = true;
            }

            cursor.close();
        }

        return successfulAddition;
    }

}