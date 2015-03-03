package dk.aau.cs.giraf.launcher.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import dk.aau.cs.giraf.launcher.R;

/**
 * This fragment contains the settings for Launcher itself, meaning the iconsize and
 * whether to show the starting animation or not.
 */
public class SettingsLauncher extends PreferenceFragment {

    private static final String USER_IDENTIFICATION_STRING_TAG = "content";

    public static SettingsLauncher newInstance(final String text)
    {
        final SettingsLauncher newFragment = new SettingsLauncher();

        Bundle args = new Bundle();
        args.putString(USER_IDENTIFICATION_STRING_TAG, text);
        newFragment.setArguments(args);

        return newFragment;
    }

    /**
     * OnCreate is per usual overridden to instantiate the mayority of the variables of the class
     * It loads the SharedPreferences of the user and sets the layout accordingly.
     * @param savedInstanceState The previously SavedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();

        if (arguments != null)
        {
            final String text = arguments.getString(USER_IDENTIFICATION_STRING_TAG);

            if (text != null)
            {
                this.getPreferenceManager().setSharedPreferencesName(SettingsUtility.getLauncherSettingsTag(text));
            }
        }

        addPreferencesFromResource(R.layout.settings_launcher);
    }
}
