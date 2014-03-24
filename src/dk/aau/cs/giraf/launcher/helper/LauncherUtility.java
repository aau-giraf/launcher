package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.AuthenticationActivity;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Class for holding static methods and fields, to minimize code duplication.
 */
public class LauncherUtility {

    private static boolean DEBUG_MODE = false;
    private static boolean DEBUG_MODE_AS_CHILD = false;

    /**
     * Decides if GIRAF launcher is running in debug mode
     * Debug mode can be enabled through enableDebugging() method,
     * which is typically done on MainActivity
     */
    public static boolean isDebugging() {
        return DEBUG_MODE;
    }
    public static boolean isDebuggingAsChild() {
        return DEBUG_MODE_AS_CHILD;
    }

    /**
     * Enable GIRAF launcher debug mode
     */
    public static void enableDebugging(boolean debugging, boolean loginAsChild, Activity activity) {
        DEBUG_MODE = debugging;
        DEBUG_MODE_AS_CHILD = loginAsChild;

        ShowDebugInformation(activity);
    }

    public static void secureStartActivity(Context context, Intent intent) {
        try {
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                throw new ActivityNotFoundException();
            }
        } catch (ActivityNotFoundException e){
            // Sending the caught exception to Google Analytics
            LauncherUtility.SendExceptionGoogleAnalytics(context, e);

            Toast toast = Toast.makeText(context, "Applikationen kunne ikke startes", 2000);
            toast.show();
            Log.e(Constants.ERROR_TAG, e.getMessage());
        }
    }

    /**
     * Show warning if DEBUG_MODE is true
     */
    public static void ShowDebugInformation(Activity a) {
        if (DEBUG_MODE) {
            LinearLayout debug = (LinearLayout) a.findViewById(R.id.debug_mode);
            TextView textView = (TextView) a.findViewById(R.id.debug_mode_text);
            textView.setText(a.getText(R.string.giraf_debug_mode) + " "
                    + (DEBUG_MODE_AS_CHILD ? a.getText(R.string.giraf_debug_as_child)
                    : a.getText(R.string.giraf_debug_as_guardian)));
            debug.setVisibility(View.VISIBLE);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sending the caught exception to Google Analytics.
     * @param context Context of the current activity.
     * @param e The caught exception.
     */
    public static void SendExceptionGoogleAnalytics(Context context,  ActivityNotFoundException e) {
        // May return null if EasyTracker has not yet been initialized with a
        // property ID.
        EasyTracker easyTracker = EasyTracker.getInstance(context);

        // StandardExceptionParser is provided to help get meaningful Exception descriptions.
        easyTracker.send(MapBuilder
                .createException(new StandardExceptionParser(context, null)    // Context and optional collection of package names
                        // to be used in reporting the exception.
                        .getDescription(Thread.currentThread().getName(),           // The name of the thread on which the exception occurred.
                                e),                                         // The exception.
                        false)                                      // False indicates a fatal exception
                .build()
        );
    }

	/**
	 * Saves data for the currently authorized log in.
	 * @param context Context of the current activity.
	 * @param id ID of the guardian logging in.
	 */
	public static void saveLogInData(Context context, int id) {
		SharedPreferences sp = context.getSharedPreferences(Constants.TIMER_KEY, 0);
		SharedPreferences.Editor editor = sp.edit();
		Date d = new Date();

        //TODO: This error is because we used to use longs for the time, but the OasisLoib group wants us to use ints now, however, there is no DateTime format in int format.
		editor.putInt(Constants.DATE_KEY, d.getTime());
		editor.putInt(Constants.GUARDIAN_ID, id);

		editor.commit();
	}

	/**
	 * Finds the currently logged in user.
	 * @param context Context of the current activity.
	 * @return Currently logged in user.
	 */
	public static Profile findCurrentUser(Context context) {
		Helper helper = new Helper(context);

		int currentUserID = findCurrentUserID(context);
		
		if (currentUserID == -1) {
			return null;
		} else {
			return helper.profilesHelper.getProfileById(currentUserID);
		}
	}

	/**
	 * Finds the ID of the currently logged in user.
	 * @param context Context of the current activity.
	 * @return ID of the currently logged in user.
	 */
	public static int findCurrentUserID(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIMER_KEY, 0);
		return sharedPreferences.getInt(Constants.GUARDIAN_ID, -1);
	}

	/**
	 Logs the current guardian out and launches the authentication activity.
	 * @param context Context of the current activity.
	 * @return The intent required to launch authentication.
	 */
	public static Intent logOutIntent(Context context) {
		clearAuthData(context);

		return new Intent(context, AuthenticationActivity.class);
	}

	/**
	 * Clears the current data on who is logged in and when they logged in.
	 * @param context Context of the current activity.
	 */
	public static void clearAuthData(Context context) {
		SharedPreferences sp = context.getSharedPreferences(Constants.TIMER_KEY, 0);
		SharedPreferences.Editor editor = sp.edit();

		editor.putLong(Constants.DATE_KEY, 1);
		editor.putLong(Constants.GUARDIAN_ID, -1);

		editor.commit();
	}

	/**
	 * Checks whether the current user session has expired.
	 * @param context Context of the current activity.
	 * @return True if a log in is required; otherwise false.
	 */
	public static boolean sessionExpired(Context context) {
		SharedPreferences sp = context.getSharedPreferences(Constants.TIMER_KEY, 0);
		Long lastAuthTime = sp.getLong(Constants.DATE_KEY, 1);
		Date d = new Date();

		return d.getTime() > lastAuthTime + Constants.TIME_TO_STAY_LOGGED_IN;
	}

	/**
	 * Converts integer to density pixels (dp)
	 * @param context Context of the current activity
	 * @param i The integer which should be used for conversion
	 * @return i converted to density pixels (dp)
	 */
	public static int intToDP(Context context, int i) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, context.getResources().getDisplayMetrics());
	}

	/**
	 * Returns true if the device currently is held in landscape orientation by the user.
	 * @param context Context of the current activity.
	 * @return true if the device currently is held in landscape orientation by the user.
	 */
	public static boolean isLandscape(Context context) {
		int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		if ((rotation % 2) == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the GIRAF apps that are usable by the given user, relative to their settings and the system they're logged in on.
	 * @param context Context of the current activity.
	 * @param user The user to find apps for.
	 * @return List of apps that are usable by this user on this device.
	 */
	public static List<Application> getVisibleGirafApps(Context context, Profile user) {
		Helper helper = new Helper(context);

		List<Application> userApps = helper.applicationHelper.getApplicationsByProfile(user);
		List<Application> deviceApps = getAvailableGirafApps(context);

		if (userApps.isEmpty() || deviceApps.isEmpty()) {
			return new ArrayList<Application>();
		}

		// Remove all apps from user's list of apps that are not installed on the device.
		for (int i = 0; i < userApps.size(); i++) {
			if (!appsContain_A(deviceApps, userApps.get(i))) {
				userApps.remove(i);
				i--;
			}

            //Exclude the launcher from visible apps
            if (userApps.get(i).getPack().equals("dk.aau.cs.giraf.launcher")) {
                userApps.remove(i);
            }
		}

		return userApps;
	}

	/**
	 * Finds all GIRAF apps installed on the device that are also registered in the database.
	 * @param context Context of the current activity.
	 * @return List of apps available for use on the device.
	 */
	private static List<Application> getAvailableGirafApps(Context context) {
		Helper helper = new Helper(context);

		List<Application> dbApps = helper.applicationHelper.getApplications();
		List<ResolveInfo> deviceApps = getDeviceGirafApps(context);

		if (dbApps.isEmpty() || deviceApps.isEmpty()) {
			return new ArrayList<Application>();
		}

		for (int i = 0; i < dbApps.size(); i++) {
			if (!appsContain_RI(deviceApps, dbApps.get(i))) {
				dbApps.remove(i);
				i--;
			}
		}

		return dbApps;
	}

	/**
	 * Finds all GIRAF apps (as ResolveInfos) installed on the device.
	 * @param context Context of the current activity.
	 * @return List of GIRAF apps.
	 */
	public static List<ResolveInfo> getDeviceGirafApps(Context context) {
		List<ResolveInfo> systemApps = getDeviceApps(context);

		if (systemApps.isEmpty()) {
			return systemApps;
		}

		// Remove all non-GIRAF apps from the list of apps in the system.
		for (int i = 0; i < systemApps.size(); i++) {
			if (!systemApps.get(i).toString().toLowerCase().contains("dk.aau.cs.giraf")) {
				systemApps.remove(i); 
				i--;
			}
		}

		return systemApps;
	}

	/**
	 * Finds all apps installed on the device.
	 * @param context Context of the current activity.
	 * @return List of apps.
	 */
	public static List<ResolveInfo> getDeviceApps(Context context) {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		return context.getPackageManager().queryIntentActivities(mainIntent, 0);
	}

	/**
	 * Checks whether a list of GIRAF apps installed on the system contains a specified app.
	 * @param systemApps List of apps (as ResolveInfos) to check.
	 * @param app The app to check for.
	 * @return True if the app is contained in the list; otherwise false.
	 */
	public static boolean appsContain_RI(List<ResolveInfo> systemApps, Application app) {
		return appsContain_RI(systemApps, app.getPack());
	}

	/**
	 * Checks whether a list of GIRAF apps installed on the system contains a specified app.
	 * @param systemApps List of apps (as ResolveInfos) to check.
	 * @param packageName Package name of the app to check for.
	 * @return True if the app is contained in the list; otherwise false.
	 */
	public static boolean appsContain_RI(List<ResolveInfo> systemApps, String packageName) {
		for (ResolveInfo app : systemApps) {
			if (app.activityInfo.packageName.equals(packageName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether a list of GIRAF apps installed on the system contains a specified app.
	 * @param systemApps List of apps (as Apps) to check.
	 * @param app The app to check for.
	 * @return True if the app is contained in the list; otherwise false.
	 */
	public static boolean appsContain_A(List<Application> systemApps, Application app) {
		return appsContain_A(systemApps, app.getPack());
	}

	/**
	 * Checks whether a list of GIRAF apps installed on the system contains a specified app.
	 * @param systemApps List of apps (as Apps) to check.
	 * @param packageName Package name of the app to check for.
	 * @return True if the app is contained in the list; otherwise false.
	 */
	public static boolean appsContain_A(List<Application> systemApps, String packageName) {
		for (Application app : systemApps) {
			if (app.getPack().equals(packageName)) {
				return true;
			}
		}

		return false;
	}

}
