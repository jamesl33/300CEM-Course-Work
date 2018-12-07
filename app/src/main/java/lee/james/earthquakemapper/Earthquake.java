package lee.james.earthquakemapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Object to represent a single earthquake. Allows the easy management of earthquake data fetched
 * from the database.
 */
class Earthquake {

    private Integer mId;
    private Float mLatitude;
    private Float mLongitude;
    private Float mMagnitude;
    private Date mDate;

    Earthquake(Integer id, Float latitude, Float longitude, Float magnitude, String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

        mId = id;
        mLatitude = latitude;
        mLongitude = longitude;
        mMagnitude = magnitude;

        try {
            mDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    Integer getId() {
        return mId;
    }

    Float getLatitude() {
        return mLatitude;
    }

    Float getLongitude() {
        return mLongitude;
    }

    Float getMagnitude() {
        return mMagnitude;
    }

    Date getDate() {
        return mDate;
    }

}
