package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.AuthenticationActivity;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.GAppDragger;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * <Code>LauncherUtility</Code> contains static methods related to running the Launcher,
 * but which do not inherently belong any specific class instance.
 */
public abstract class LauncherUtility {


    /* Flags that indicate whether Launcher is in debug mode. These should not be changed from here,
        but from MainActivity.java.                                                                  */
    private static boolean DEBUG_MODE = false;
    private static boolean DEBUG_MODE_AS_CHILD = false;

    /**
     * Returns whether Launcher is running in debug mode. Debug mode is toggled in the
     * source code of {@code MainActivity}.
     *
     * @return {@code true} if Launcher is running in debug mode.
     */
    public static boolean isDebugging() {
        return DEBUG_MODE;
    }

    /**
     * Returns whether Launcher is running in debug mode with a child profile. This setting is toggled in the
     * source code of {@code MainActivity}.
     *
     * @return {@code true} if Launcher is running in debug mode with a child profile.
     */
    public static boolean isDebuggingAsChild() {
        return DEBUG_MODE_AS_CHILD;
    }

    /**
     * Set whether Launcher is running in debug mode. If debug mode is enabled, Launcher will skip its opening
     * animation and the login screen. {@code loginAsChild} decides whether the logged in profile is a child or
     * a guardian. If debug mode is enabled, a warning is shown in {@code activity}.
     *
     * @param debugging If {@code true} Launcher will run in debugging mode.
     * @param loginAsChild If {@code true} Launcher will automatically log in with a child profile. If
     *                     {@code false} launcher will log in as a guardian.
     * @param activity An activity in which to display a message indicating that debugging is enabled,
     *                 if this is the case. (See {@link dk.aau.cs.giraf.launcher.helper.LauncherUtility#showDebugInformation(android.app.Activity)})
     */
    public static void setDebugging(boolean debugging, boolean loginAsChild, Activity activity) {
        DEBUG_MODE = debugging;
        DEBUG_MODE_AS_CHILD = loginAsChild;

        if (DEBUG_MODE) {
            showDebugInformation(activity);
        }
    }

    /**
     * Starts an activity indicated in {@code intent}. If the activity does not exist, Google Analytics is notified,
     * and a toast is shown.
     * @param context The context from which to start the activity. In case of failure, a toast is shown in this context.
     * @param intent The intent describing the requested activity.
     */
    public static void secureStartActivity(Context context, Intent intent) {
        try {
            //If the activity exists, start it. Otherwise throw an exception.
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                throw new ActivityNotFoundException();
            }
        } catch (ActivityNotFoundException ex) {
            // Sending the caught exception to Google Analytics
            LauncherUtility.sendExceptionGoogleAnalytics(context, ex);

            //Display a toast, to inform the user of the problem.
            Toast toast = Toast.makeText(context, context.getString(R.string.activity_not_found_msg), Toast.LENGTH_SHORT);
            toast.show();
            Log.e(Constants.ERROR_TAG, ex.getMessage());
        }
    }

    /**
     * Shows a message in {@code activity} indicating that debugging mode is enabled.
     *
     * @param activity The activity in which to show the message.
     */
    public static void showDebugInformation(Activity activity) {
        if (activity != null) {
            //Get the necessary views.
            LinearLayout debug = (LinearLayout) activity.findViewById(R.id.debug_view);
            TextView textView = (TextView) activity.findViewById(R.id.debug_text_view);

            //Fill the view with information on the debug settings.
            textView.setText(activity.getText(R.string.giraf_debug_mode) + " "
                    + (DEBUG_MODE_AS_CHILD ? activity.getText(R.string.giraf_debug_as_child)
                    : activity.getText(R.string.giraf_debug_as_guardian)));

            debug.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Send the exception {@code ex} to Google Analytics.
     *
     * @param context Context of the current activity.
     * @param ex       The caught exception.
     */
    public static void sendExceptionGoogleAnalytics(Context context, Exception ex) {
        // May return null if EasyTracker has not yet been initialized with a
        // property ID.
        EasyTracker easyTracker = EasyTracker.getInstance(context);

        // StandardExceptionParser is provided to help get meaningful Exception descriptions.
        easyTracker.send(MapBuilder
                .createException(new StandardExceptionParser(context, null)    // Context and optional collection of package names
                        // to be used in reporting the exception.
                        .getDescription(Thread.currentThread().getName(),           // The name of the thread on which the exception occurred.
                                ex),                                         // The exception.
                        false)                                      // False indicates a fatal exception
                .build()
        );
    }

    /**
     * Saves information on which user is currently logged in, and what the time login was authorised.
     * This information is used to determine when to automatically logout the user.
     * The information is saved in a {@code SharedPreferences} file.
     *
     * @param context Context in which the preferences should be saved.
     * @param id      ID of the guardian logging in.
     * @param loginTime The time of login (UNIX time, milliseconds).
     */
    public static void saveLogInData(Context context, int id, long loginTime) {
        SharedPreferences sp = context.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(Constants.LOGIN_TIME, loginTime);
        editor.putInt(Constants.GUARDIAN_ID, id);

        editor.commit();
    }

    /**
     * Gets the profile object of the currently logged in user. If no user is currently logged in,
     * {@code null} is returned.
     *
     * @param context Context in which the login information was saved.
     * @return The currently logged in user. If no login information is found, {@code null} is returned.
     */
    public static Profile getCurrentUser(Context context) {
        Helper helper = getOasisHelper(context);

        //Get the ID of the logged in user from SharedPreferences.
        SharedPreferences sp = context.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        int currentUserID = sp.getInt(Constants.GUARDIAN_ID, -1);

        //Return null if no login information is found, otherwise return the the profile.
        if (currentUserID == -1) {
            return null;
        } else {
            return helper.profilesHelper.getProfileById(currentUserID);
        }
    }

    /**
     * Logs the current guardian out and launches the authentication_activity.
     *
     * @param context Context of the current activity.
     * @return The intent required to launch authentication_activity.
     */
    public static Intent logOutIntent(Context context) {
        clearAuthData(context);

        return new Intent(context, AuthenticationActivity.class);
    }

    /**
     * Clears the current data on who is logged in and when they logged in.
     *
     * @param context Context of the current activity.
     */
    public static void clearAuthData(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(Constants.LOGIN_TIME, 1);
        editor.putLong(Constants.GUARDIAN_ID, -1);

        editor.commit();
    }

    /**
     * Checks whether the current user session has expired.
     *
     * @param context Context of the current activity.
     * @return True if a log in is required; otherwise false.
     */
    public static boolean sessionExpired(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        Long lastAuthTime = sp.getLong(Constants.LOGIN_TIME, 1);
        Date d = new Date();

        return d.getTime() > lastAuthTime + Constants.TIME_TO_STAY_LOGGED_IN;
    }

    /**
     * Converts integer to density pixels (dp)
     *
     * @param context Context of the current activity
     * @param i       The integer which should be used for conversion
     * @return i converted to density pixels (dp)
     */
    public static int intToDP(Context context, int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, context.getResources().getDisplayMetrics());
    }

    /**
     * This function tries generates an OasisLib Helper, based on the current context.
     * If it succeeds, returns the helper, otherwise sends information to Google Analytics about the error and return null
     * @param context The context of the current activity
     * @return The generated helper
     */
    public static Helper getOasisHelper(Context context) {
        Helper helper = null;
        try {
            helper = new Helper(context);
        } catch (Exception e) {
            sendExceptionGoogleAnalytics(context, e);
            Log.e(Constants.ERROR_TAG, e.getMessage());
        }
        return helper;
    }

    /**
     * This function returns substring of the correct preference string based on the user profile.
     * Checks for the role and appends the correct role prefix to the profiles ID.
     * This is used to find out which settings file to read.
     * @param profile The profile to generate a string for
     * @return The substring generated.
     */
    public static String getSharedPreferenceUser(Profile profile){
        String fileName = "";
        switch (profile.getRole()){
            case GUARDIAN:
                fileName += "g";
                break;
            case CHILD:
                fileName += "c";
                break;
            case ADMIN:
                fileName += "a";
                break;
            case PARENT:
                fileName += "p";
                break;
            default: // File type is unknown
                fileName += "u";
                break;
        }

        fileName += String.valueOf(profile.getId());
        return  fileName;
    }

    /**
     * This function returns substring of the correct preference string based on the user profile.
     * This user profile is extracted from the context given.
     * Checks for the role and appends the correct role prefix to the profiles ID.
     * This is used to find out which settings file to read.
     * @param context The context of the current activity
     * @return The substring generated.
     */
    public static String getSharedPreferenceUserFromContext(Context context){
        Profile currentUser = getCurrentUser(context);
        return getSharedPreferenceUser(currentUser);
    }

    /**
     * This function retrives the shared preferences for the given user.
     * it uses calls to other functions to generate the correct filename string and
     * subsequently retrieves the preferences from the context given
     * @param context The context of the current activity
     * @param profile The profile we are retrieving settings for
     * @return The settings for the given user in the given context.
     */
    public static SharedPreferences getSharedPreferencesForCurrentUser(Context context, Profile profile){
        String fileName = Constants.TAG + ".";
        fileName += getSharedPreferenceUser(profile);
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    /**
     * This function retrives the shared preferences for the given user.
     * Finds the user based on the given context.
     * it uses calls to other functions to generate the correct filename string and
     * subsequently retrieves the preferences from the context given
     * @param context The context of the current activity
     * @return The settings for the user in the given context.
     */
    public static SharedPreferences getSharedPreferencesForCurrentUser(Context context){
        Profile currentUser = getCurrentUser(context);
        return getSharedPreferencesForCurrentUser(context, currentUser);
    }
}
