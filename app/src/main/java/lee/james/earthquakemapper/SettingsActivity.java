package lee.james.earthquakemapper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_HEATMAP_SWITCH = "heatmap_switch";
    public static final String KEY_PREF_EARTHQUAKE_COUNT = "earthquake_count";
    public static final String KEY_PREF_DATABASE_UPDATE_INTERVAL = "database_update_interval";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

}
