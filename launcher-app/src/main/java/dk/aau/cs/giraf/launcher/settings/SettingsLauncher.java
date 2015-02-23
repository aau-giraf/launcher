package dk.aau.cs.giraf.launcher.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import dk.aau.cs.giraf.launcher.R;

/**
 * This fragment contains the settings for Launcher itself, meaning the iconsize and
 * whether to show the starting animation or not.
 */
public class SettingsLauncher extends PreferenceFragment {

    private String userPart;

    /**
     * The contructor for the class
     * @param userPart The userpart of the sharedpreferences
     */
    public SettingsLauncher(String userPart){
        this.userPart = userPart;
    }

    /**
     * OnCreate is per usual overridden to instantiate the mayority of the variables of the class
     * It loads the SharedPreferences of the user and sets the layout accordingly.
     * @param savedInstanceState The previously SavedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getPreferenceManager().setSharedPreferencesName(SettingsUtility.getLauncherSettingsTag(userPart));

        addPreferencesFromResource(R.layout.settings_launcher);
    }
}
