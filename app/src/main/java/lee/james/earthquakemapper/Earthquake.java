package lee.james.earthquakemapper;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Earthquake {

    private static final String LOG_TAG = Earthquake.class.getSimpleName();

    private Integer id;
    private Float latitude;
    private Float longitude;
    private Float magnitude;
    private Date date;

    public Earthquake(Integer id, Float latitude, Float longitude, Float magnitude, String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.magnitude = magnitude;

        try {
            this.date = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public Integer getId() {
        return this.id;
    }

    public Float getLatitude() {
        return this.latitude;
    }

    public Float getLongitude() {
        return this.longitude;
    }

    public Float getMagnitude() {
        return this.magnitude;
    }

    public Date getDate() {
        return this.date;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%d", this.id);
    }
}
