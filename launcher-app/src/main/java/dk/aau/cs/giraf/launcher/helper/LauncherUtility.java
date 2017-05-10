package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import dk.aau.cs.giraf.gui.GirafPopupDialog;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.LoginActivity;
import dk.aau.cs.giraf.launcher.activities.SettingsActivity;
import dk.aau.cs.giraf.models.core.Department;
import dk.aau.cs.giraf.models.core.User;

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

    public static void logoutWithDialog(final Activity launchedFrom){
        final GirafPopupDialog noticeDialog = new GirafPopupDialog(R.string.error_login, "Forbindelse midste, du bliver logget ud",launchedFrom);
        noticeDialog.setButton1(R.string.ok, R.drawable.icon_accept, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(launchedFrom);
                noticeDialog.dismiss();
            }
        });

    }

    public static void logout(Activity launchedFrom){
        Intent intent = new Intent(launchedFrom, SettingsActivity.class);
        launchedFrom.startActivity(intent);
        launchedFrom.finish();
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

    public static void showErrorDialog(Context context, String message) {
        final GirafPopupDialog errorDialog = new GirafPopupDialog(R.string.error_login,message,context);
        errorDialog.setButton1(R.string.ok, R.drawable.icon_accept, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorDialog.dismiss();
            }
        });
        errorDialog.show();
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
            Intent storeIntent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id="+intent.getPackage()));
            context.startActivity(intent);
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

}
