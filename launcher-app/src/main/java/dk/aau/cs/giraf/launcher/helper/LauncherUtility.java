package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.AuthenticationActivity;

import java.util.Date;

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
     * @param debugging    If {@code true} Launcher will run in debugging mode.
     * @param loginAsChild If {@code true} Launcher will automatically log in with a child profile. If
     *                     {@code false} launcher will log in as a guardian.
     * @param activity     An activity in which to display a message indicating that debugging is enabled,
     *                     if this is the case. (See
     *                     {@link dk.aau.cs.giraf.launcher.helper.LauncherUtility#
     *                     showDebugInformation(android.app.Activity)})
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
     *
     * @param context The context from which to start the activity. In case of failure, a toast is shown in this context.
     * @param intent  The intent describing the requested activity.
     */
    public static void secureStartActivity(Context context, Intent intent) {
        //If the activity exists, start it. Otherwise throw an exception.
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            /* We catch a general exception as we do not know the why the launched application crashes. */
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                return;
            }
        } else {
            //Display a toast, to inform the user of the problem.
            Toast toast = Toast.makeText(context,
                context.getString(R.string.activity_not_found_msg), Toast.LENGTH_SHORT);
            toast.show();
            Log.e(Constants.ERROR_TAG, "App could not be started");
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
            textView.setText(activity.getText(R.string.giraf_debug_mode) + " " +
                (isDebuggingAsChild() ? activity.getText(R.string.giraf_debug_as_child)
                    : activity.getText(R.string.giraf_debug_as_guardian)));

            debug.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Send the exception {@code ex} to Google Analytics.
     *
     * @param context Context of the current activity.
     * @param ex      The caught exception.
     */
    private static void sendExceptionGoogleAnalytics(Context context, Exception ex) {
        // May return null if EasyTracker has not yet been initialized with a
        // property ID.
        EasyTracker easyTracker = EasyTracker.getInstance(context);

        // StandardExceptionParser is provided to help get meaningful Exception descriptions.
        // Context and optional collection of package names to be used in reporting the exception.
        easyTracker.send(MapBuilder
            .createException(new StandardExceptionParser(context, null)
                    // The name of the thread on which the exception occurred.
                    .getDescription(Thread.currentThread().getName(),
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
     * @param context   Context in which the preferences should be saved.
     * @param id        ID of the guardian logging in.
     * @param loginTime The time of login (UNIX time, milliseconds).
     */
    public static void saveLogInData(final Context context, final long id, final long loginTime) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        final SharedPreferences.Editor editor = sp.edit();

        editor.putLong(Constants.LOGIN_TIME, loginTime);
        editor.putLong(Constants.GUARDIAN_ID, id);
        Boolean succes = editor.commit();
        if(!succes){
            Log.e("Launcher","Writing to Shared Preferneces failed");
        }
    }

    /**
     * Gets the profile object of the currently logged in user. If no user is currently logged in,
     * {@code null} is returned.
     *
     * @param context Context in which the login information was saved.
     * @return The currently logged in user. If no login information is found, {@code null} is returned.
     */
    public static Profile getCurrentUser(final Context context) {
        final Helper helper = getOasisHelper(context);

        //Get the ID of the logged in user from SharedPreferences.
        final SharedPreferences sp = context.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        final long currentUserId = sp.getLong(Constants.GUARDIAN_ID, -1);

        //Return null if no login information is found, otherwise return the the profile.
        if (currentUserId == -1) {
            return null;
        } else {
            return helper.profilesHelper.getById(currentUserId);
        }
    }

    /**
     * Logs the current guardian out and launches the authentication_activity.
     *
     * @param context Context of the current activity.
     * @return The intent required to launch authentication_activity.
     */
    public static Intent logOutIntent(final Context context) {
        clearAuthData(context);

        return new Intent(context, AuthenticationActivity.class);
    }

    /**
     * Clears the current data on who is logged in and when they logged in.
     *
     * @param context Context of the current activity.
     */
    private static void clearAuthData(final Context context) {

        final SharedPreferences sp = context.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        final SharedPreferences.Editor editor = sp.edit();

        editor.putLong(Constants.LOGIN_TIME, 1);
        editor.putLong(Constants.GUARDIAN_ID, -1);
        editor.putLong(Constants.CHILD_ID, -1);

        Boolean succes = editor.commit();
        if(!succes){
            Log.e("Launcher","Writing to Shared Preferneces failed");
        }
    }

    /**
     * Checks whether the current user session has expired.
     *
     * @param context Context of the current activity.
     * @return True if a log in is required; otherwise false.
     */
    public static boolean sessionExpired(final Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        Long lastAuthTime = sp.getLong(Constants.LOGIN_TIME, 1);
        Date date = new Date();

        return date.getTime() > lastAuthTime + Constants.TIME_TO_STAY_LOGGED_IN;
    }

    /**
     * Converts integer to density pixels (dp).
     *
     * @param context Context of the current activity
     * @param input   The integer which should be used for conversion
     * @return input converted to density pixels (dp)
     */
    public static int intToDp(Context context, int input) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            input, context.getResources().getDisplayMetrics());
    }

    /**
     * This function tries generates an OasisLib Helper, based on the current context.
     * If it succeeds, returns the helper, otherwise sends information to Google Analytics
     * about the error and return null
     *
     * @param context The context of the current activity
     * @return The generated helper
     */
    public static Helper getOasisHelper(Context context) {
        return new Helper(context);
    }

    /**
     * This function returns substring of the correct preference string based on the user profile.
     * Checks for the role and appends the correct role prefix to the profiles ID.
     * This is used to find out which settings file to read.
     *
     * @param profile The profile to generate a string for
     * @return The substring generated.
     */
    public static String getSharedPreferenceUser(Profile profile) {
        String fileName = "";
        if (profile == null)
            fileName += "u";
        else {
            switch (profile.getRole()) {
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
        }

        return fileName;
    }


    /**
     * This function retrieves the shared preferences for the given user.
     * it uses calls to other functions to generate the correct filename string and
     * subsequently retrieves the preferences from the context given
     *
     * @param context The context of the current activity
     * @param profile The profile we are retrieving settings for
     * @return The settings for the given user in the given context.
     */
    public static SharedPreferences getSharedPreferencesForCurrentUser(Context context, Profile profile) {
        String fileName = Constants.TAG + ".";
        fileName += getSharedPreferenceUser(profile);
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }
}
