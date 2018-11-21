package lee.james.earthquakemapper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class EarthquakeDatabaseHelper extends SQLiteAssetHelper {

    private static final String LOG_TAG = EarthquakeDatabaseHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "EarthquakeDatabase.db";

    private static final String EARTHQUAKE_TABLE = "earthquakes";

    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_MAGNITUDE = "magnitude";
    private static final String KEY_DATE = "date";

    private static final String[] COLUMNS = { KEY_ID, KEY_LATITUDE, KEY_LONGITUDE, KEY_MAGNITUDE, KEY_DATE };

    private SQLiteDatabase mWritableDB = this.getWritableDatabase();
    private SQLiteDatabase mReadableDB = this.getReadableDatabase();

    public EarthquakeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Earthquake getOldest() {
        Cursor cursor = mReadableDB.rawQuery("select *, min(date) from earthquakes", null);
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

    public Earthquake getLatest() {
        Cursor cursor = mReadableDB.rawQuery("select *, max(date) from earthquakes", null);
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

}
