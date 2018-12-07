package lee.james.earthquakemapper;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EarthquakeDatabaseHelperTest {
    private EarthquakeDatabaseHelper mEarthquakeDatabase = new EarthquakeDatabaseHelper(InstrumentationRegistry.getTargetContext());

    @Test
    public void getOldest() {
        Earthquake knownCorrect = new Earthquake(
                0,
                -11.242f,
                165.006f,
                6.1f,
                "1950/01/02"
        );

        assertEquals(knownCorrect, mEarthquakeDatabase.getOldest());
    }

    @Test
    public void getLatest() {
        Earthquake latest = mEarthquakeDatabase.getLatest();

        // Since we can't test this properly now as the database it auto updated
        // getLatest() should return an earthquake with an id larger than 19487
        assertTrue(latest.getId() >= 19487);
    }

    @Test
    public void lower_bounds_getEarthquakes() {
        ArrayList<Earthquake> knownCorrect = new ArrayList<>();

        knownCorrect.add(new Earthquake(
                0,
                -11.242f,
                165.006f,
                6.1f,
                "1950/01/02"
        ));

        knownCorrect.add(new Earthquake(
                1,
                17.576f,
                121.428f,
                6.5f,
                "1950/01/03"
        ));

        knownCorrect.add(new Earthquake(
                2,
                -45.798f,
                -77.077f,
                6.3f,
                "1950/01/03"
        ));

        assertEquals(knownCorrect, mEarthquakeDatabase.getEarthquakes("1950/01/02", "1950/01/03"));
    }

    @Test
    public void higher_bounds_getEarthquakes() {
        ArrayList<Earthquake> knownCorrect = new ArrayList<>();

        knownCorrect.add(new Earthquake(
                19477,
                61.3234f,
                -149.9234f,
                7.0f,
                "2018/11/30"
        ));

        knownCorrect.add(new Earthquake(
                19478,
                61.2586f,
                -149.9214f,
                5.7f,
                "2018/11/30"
        ));

        knownCorrect.add(new Earthquake(
                19479,
                61.3582f,
                -149.9943f,
                5.2f,
                "2018/12/01"
        ));

        knownCorrect.add(new Earthquake(
                19480,
                -7.403f,
                128.7097f,
                6.3f,
                "2018/12/01"
        ));

        assertEquals(knownCorrect, mEarthquakeDatabase.getEarthquakes("2018/11/30", "2018/12/01"));
    }
}