package lee.james.earthquakemapper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity which loads the settings fragment
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_HEATMAP_SWITCH = "heatmap_switch";
    public static final String KEY_PREF_EARTHQUAKE_COUNT = "earthquake_count";
    public static final String KEY_PREF_DATABASE_UPDATE_INTERVAL = "database_update_interval";
    public static final String KEY_FOCUSED_MARKER_COLOR = "focused_marker_color";
    public static final String KEY_UNFOCUSED_MARKER_COLOR = "unfocused_marker_color";

    /**
     * Replace the content with the settings fragment
     *
     * @param savedInstanceState - The previous state Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

}
