package lee.james.earthquakemapper;


import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Fragment which loads all the preferences from res/xml/preferences.xml
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    /**
     * @param savedInstanceState - The previous state Bundle
     * @param rootKey            -  If non-null root the fragment at the key.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

}
