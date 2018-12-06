package lee.james.earthquakemapper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

class EarthquakeDatabaseHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "EarthquakeDatabase.db";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase mWritableDB = this.getWritableDatabase();
    private SQLiteDatabase mReadableDB = this.getReadableDatabase();

    private static final String EARTHQUAKE_TABLE = "earthquakes";

    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_MAGNITUDE = "magnitude";
    private static final String KEY_DATE = "date";

    EarthquakeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    Earthquake getOldest() {
        Cursor cursor = this.mReadableDB.query(
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

    Earthquake getLatest() {
        Cursor cursor = this.mReadableDB.query(
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

    ArrayList<Earthquake> getEarthquakes(String startDate, String endDate) {
        return this.getEarthquakes(startDate, endDate, Integer.MAX_VALUE);
    }

    ArrayList<Earthquake> getEarthquakes(String startDate, String endDate, Integer count) {
        ArrayList<Earthquake> earthquakeList = new ArrayList<>();

        Cursor cursor = this.mReadableDB.query(
                EARTHQUAKE_TABLE,
                null,
                String.format("%s between ? and ?", KEY_DATE),
                new String[]{startDate, endDate},
                null,
                null,
                null);

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

}
