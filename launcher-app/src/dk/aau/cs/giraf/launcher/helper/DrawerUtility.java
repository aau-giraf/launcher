package dk.aau.cs.giraf.launcher.helper;

import android.content.Context;

import java.util.Random;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileApplication;
import dk.aau.cs.giraf.oasis.lib.models.Setting;

/**
 * This class contains the methods needed for the Drawer in HomeActivity to Work
 * NOTE: The Drawer is currently disabled, since the customers did not deemed it needed.
 * This class remains so that it can be enabled at any time if needed.
 */
public class DrawerUtility {


    /**
     * Handles the background color of app icons.
     * This has been temporarily disabled, as it turned out that the clients had no use for it.
     * It is left in, as it may become useful at a later date. It may however not work, as it is
     * tightly coupled with Oasis.
     * You can read more in the report about the Launcher from 2014.
     *
     * @param appInfo The AppInfo object of the applications who's color is requested.
     * @return An integer corresponding to the requested color.
     */
    private int getAppBackgroundColor(Context context, AppInfo appInfo) {
        int[] colors = context.getResources().getIntArray(R.array.appcolors);
//        ProfileApplication profileApplication = mHelper.profileApplicationHelper.getProfileApplicationByProfileIdAndApplicationId(appInfo.getApp(), mCurrentUser);
//        Setting<String, String, String> launcherSettings = profileApplication.getSettings();
//
//		// If settings for the given app exists.
//		if (launcherSettings != null && launcherSettings.containsKey(String.valueOf(appInfo.getApp().getId()))) {
//			HashMap<String, String> appSetting = launcherSettings.get(String.valueOf(appInfo.getApp().getId()));
//
//			// If color settings for the given app exists.
//			if (appSetting != null && appSetting.containsKey(Constants.COLOR_BG)) {
//				return Integer.parseInt(appSetting.get(Constants.COLOR_BG));
//			}
//		}
        //Randomize a color, if no setting exist, and save it.
        int position = (new Random()).nextInt(colors.length);

        // No settings existed, save the new.
        //saveNewBgColor(colors[position], appInfo);

        return colors[position];
    }

    /**
     * Saves a color in the settings of an app.
     * This has been temporarily disabled, as it turned out that the clients had no use for it.
     * It is left in, as it may become useful at a later date. It will currently not work as the code
     * has been changed, to allow the project to compile, despite that necessary variables, constants
     * and methods have since been removed.
     * You can read more in the report about the Launcher from 2014.
     *
     * @param color Color to save.
     * @param appInfo The AppInfo object of the app to save the color for.
     */
    private void saveNewBgColor(Context context, Profile currentUser, int color, AppInfo appInfo) {
        Helper helper = LauncherUtility.getOasisHelper(context);
        ProfileApplication profileApplication = helper.profileApplicationHelper.getProfileApplicationByProfileIdAndApplicationId(appInfo.getApp(), currentUser);
        Setting<String, String, String> launcherSettings = profileApplication.getSettings();

        if (launcherSettings == null) {
            launcherSettings = new Setting<String, String, String>();
        }

        // If no app specific settings exist.
        if (!launcherSettings.containsKey(String.valueOf(appInfo.getApp().getId()))) {
            launcherSettings.addValue(String.valueOf(appInfo.getApp().getId()), "", String.valueOf(color));
        } else if (!launcherSettings.get(String.valueOf(appInfo.getApp().getId())).containsKey("")) {
			/* If no app specific color settings exist.*/
            launcherSettings.get(String.valueOf(appInfo.getApp().getId())).put("", String.valueOf(color));
        }

        //mLauncher.setSettings(launcherSettings);
        //mHelper.applicationHelper.modifyAppByProfile(mLauncher, mCurrentUser);
    }
}
