package dk.aau.cs.giraf.launcher.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.launcher.settings.components.IconResizer;

public class SettingsLauncher extends PreferenceFragment {

    private String userPart;

    public SettingsLauncher(String userPart){
        this.userPart = userPart;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getPreferenceManager().setSharedPreferencesName(SettingsUtility.getLauncherSettingsTag(userPart));

        addPreferencesFromResource(R.layout.launcher_settings);
    }
}
