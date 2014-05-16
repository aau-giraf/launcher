package dk.aau.cs.giraf.launcher.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.launcher.helper.LauncherUtility;

/**
 * Created by Frederik on 29-04-14.
 */

/**
 * Helper methods used to handle saving of settings for each user
 * to their own preference file.
 */
public class SettingsUtility {

    /**
     * Tag used to identify preference for launcher.
     */
    public static final String LAUNCHER_SETTINGS_TAG = SettingsLauncher.class.getName();

    /**
     * Get launcher settings for a specific user.
     * Settings are saved in a seperate file for each user to fx save selected Android apps
     * available on current device.
     * @param user String used to identify user.
     * @return A preference file for the user.
     */
    public static String getLauncherSettingsTag(String user){
        return LAUNCHER_SETTINGS_TAG + "." + user + ".prefs";
    }

    /**
     * Get launcher settings for a specific user.
     * Settings are saved in a seperate file for each user to fx save selected Android apps
     * available on current device.
     * @param context Current context.
     * @param user String used to identify user.
     * @return A preference file for the user.
     */
    public static SharedPreferences getLauncherSettings(Context context, String user){
        return context.getSharedPreferences(getLauncherSettingsTag(user), Context.MODE_PRIVATE);
    }
}