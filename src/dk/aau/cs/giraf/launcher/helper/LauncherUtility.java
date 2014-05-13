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
import android.view.ViewGroup;
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
import dk.aau.cs.giraf.settingslib.settingslib.SettingsUtility;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * <Code>LauncherUtility</Code> contains static methods related to running the Launcher,
 * but which do not inherently belong any specific class instance.
 */
public abstract class LauncherUtility {
    private static HashMap<String,AppInfo> mAppInfoHashMap;

    /* Flags that indicate whether Launcher is in debug mode. These should not be changed from here,
        but from MainActivity.java.                                                                  */
    private static boolean DEBUG_MODE = false;
    private static boolean DEBUG_MODE_AS_CHILD = false;
    private final static String DEFAULT_PACKAGE_FILTER = "";
    private final static boolean DEFAULT_FILTER_INCLUSION = true;

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
     * Logs the current guardian out and launches the authentication activity.
     *
     * @param context Context of the current activity.
     * @return The intent required to launch authentication.
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
     * Gets the GIRAF apps that are usable by the given user, relative to their settings and the system they're logged in on.
     *
     * @param context Context of the current activity.
     * @param user    The user to find apps for.
     * @return List of apps that are usable by this user on this device.
     */
    public static List<Application> getVisibleGirafApps(Context context, Profile user) {
        Helper helper = getOasisHelper(context);

        List<Application> userApps = helper.applicationHelper.getApplicationsByProfile(user);
        List<Application> deviceApps = getAvailableGirafApps(context);

        if (userApps.isEmpty() || deviceApps.isEmpty()) {
            return new ArrayList<Application>();
        }

        // Remove all apps from user's list of apps that are not installed on the device and exclude the launcher it self.
        for (int i = 0; i < userApps.size(); i++) {
            if (!appsContain_A(deviceApps, userApps.get(i)) || userApps.get(i).getPackage().equals("dk.aau.cs.giraf.launcher")) {
                userApps.remove(i);
                i--;
            }
        }

        return userApps;
    }

    /**
     * Finds all GIRAF apps installed on the device that are also registered in the database.
     *
     * @param context Context of the current activity.
     * @return List of apps available for use on the device.
     */
    private static List<Application> getAvailableGirafApps(Context context) {
        Helper helper = getOasisHelper(context);

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
     * Finds all GIRAF apps installed on the device that are also registered in the database, EXCEPT Launcher.
     *
     * @param context Context of the current activity.
     * @return List of apps available for use on the device.
     */
    public static List<Application> getAvailableGirafAppsButLauncher(Context context) {
        Helper helper = getOasisHelper(context);

        List<Application> dbApps = helper.applicationHelper.getApplications();
        List<ResolveInfo> deviceApps = getDeviceGirafApps(context);

        if (dbApps.isEmpty() || deviceApps.isEmpty()) {
            return new ArrayList<Application>();
        }

        //TODO: Launcher name should not be hardcoded!
        for (int i = 0; i < dbApps.size(); i++) {
            String name = dbApps.get(i).getName();
            if (!appsContain_RI(deviceApps, dbApps.get(i)) || name.equals("Launcher")) {
                dbApps.remove(i);
                i--;
            }
        }

        return dbApps;
    }

    /**
     * Finds all GIRAF apps (as ResolveInfos) installed on the device.
     *
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
     *
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
     *
     * @param systemApps List of apps (as ResolveInfos) to check.
     * @param app        The app to check for.
     * @return True if the app is contained in the list; otherwise false.
     */
    public static boolean appsContain_RI(List<ResolveInfo> systemApps, Application app) {
        return appsContain_RI(systemApps, app.getPackage());
    }

    /**
     * Checks whether a list of GIRAF apps installed on the system contains a specified app.
     *
     * @param systemApps  List of apps (as ResolveInfos) to check.
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
     *
     * @param systemApps List of apps (as Apps) to check.
     * @param app        The app to check for.
     * @return True if the app is contained in the list; otherwise false.
     */
    public static boolean appsContain_A(List<Application> systemApps, Application app) {
        return appsContain_A(systemApps, app.getPackage());
    }

    /**
     * Checks whether a list of GIRAF apps installed on the system contains a specified app.
     *
     * @param systemApps  List of apps (as Apps) to check.
     * @param packageName Package name of the app to check for.
     * @return True if the app is contained in the list; otherwise false.
     */
    public static boolean appsContain_A(List<Application> systemApps, String packageName) {
        for (Application app : systemApps) {
            if (app.getPackage().equals(packageName)) {
                return true;
            }
        }

        return false;
    }

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

    public static List<Application> getAndroidApplicationList(Context context, String filter){
        List<ResolveInfo> allApps = getDeviceApps(context);
        List<Application> result = new ArrayList<Application>();
        PackageManager packageManager = context.getPackageManager();

        for(ResolveInfo app : allApps){
            String appActivityName = app.activityInfo.name;
            String appPackageName = app.activityInfo.packageName;

            if (appPackageName.contains(filter))
                continue;

            Application application = new Application();
            application.setPackage(appPackageName);
            application.setActivity(appActivityName);
            application.setName(app.activityInfo.loadLabel(packageManager).toString());
            application.setId(app.hashCode());
            result.add(application);
        }
        return result;
    }

    public static List<Application> convertPackageNamesToApplications(Context context, Set<String> packageNames){
        List<ResolveInfo> allApps = getDeviceApps(context);
        List<Application> selectedApps = new ArrayList<Application>();
        PackageManager packageManager = context.getPackageManager();

        outerloop:
        for(ResolveInfo app : allApps){
            for(String activityName : packageNames){
                String appActivityName = app.activityInfo.name;
                String appPackageName = app.activityInfo.packageName;
                if (appActivityName.equals(activityName)){
                    Application application = new Application();
                    application.setPackage(appPackageName);
                    application.setActivity(appActivityName);
                    application.setName(app.activityInfo.loadLabel(packageManager).toString());
                    application.setId(app.hashCode());
                    selectedApps.add(application);

                    continue outerloop;
                }
            }
        }
        return selectedApps;
    }

    public static LoadAndroidApplicationTask loadGirafApplicationsIntoView(Context context, List<Application> girafAppsList, LinearLayout targetLayout, int iconSize, View.OnClickListener listener)
    {
        return loadGirafApplicationsIntoView(context, null, null, girafAppsList, targetLayout, iconSize, listener);
    }

    public static LoadAndroidApplicationTask loadGirafApplicationsIntoView(Context context, Profile currentUser, List<Application> girafAppsList, LinearLayout targetLayout, int iconSize, View.OnClickListener listener)
    {
        return loadGirafApplicationsIntoView(context, currentUser, null, girafAppsList, targetLayout, iconSize, listener);
    }

    public static LoadAndroidApplicationTask loadGirafApplicationsIntoView(Context context, Profile currentUser, Profile guardian, List<Application> girafAppsList, LinearLayout targetLayout, int iconSize)
    {
        return loadGirafApplicationsIntoView(context, currentUser, guardian, girafAppsList, targetLayout, iconSize, null);
    }

    public static LoadAndroidApplicationTask loadGirafApplicationsIntoView(Context context, Profile currentUser, Profile guardian, List<Application> girafAppsList, LinearLayout targetLayout, int iconSize, View.OnClickListener listener) {
        //Get the list of apps to show in the container
        //List<Application> girafAppsList = LauncherUtility.getAvailableGirafAppsButLauncher(mContext);
        /*if (girafAppsList != null && !girafAppsList.isEmpty()) {
            targetLayout.removeAllViews();

            //Fill AppInfo hash map with AppInfo objects for each app
            if (currentUser == null)
                currentUser = LauncherUtility.getCurrentUser(context);

            appInfoHash = updateAppInfoHashMap(context, girafAppsList, currentUser);
            List<AppInfo> appInfos = new ArrayList<AppInfo>(appInfoHash.values());
            Collections.sort(appInfos, new AppComparator(context));

            int containerWidth = ((ScrollView) targetLayout.getParent()).getWidth();
            int containerHeight = ((ScrollView) targetLayout.getParent()).getHeight();
            // if we are in portrait swap width and height
            if (containerHeight > containerWidth){
                int temp = containerWidth;
                containerWidth = containerHeight;
                containerHeight = temp;
                Log.d(Constants.ERROR_TAG, "Portrait mode detected. Width and height swapped.");
            }

            //Calculate how many apps the screen can fit on each row, and how much space is available for horizontal padding
            int appsPrRow = getAmountOfAppsWithinBounds(containerWidth, iconSize);

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int appsPrColumn = getAmountOfAppsWithinBounds(containerHeight, iconSize);
            int paddingHeight = getLayoutPadding(containerHeight, appsPrColumn, iconSize);

            //Add the first row to the container
            LinearLayout currentAppRow = new LinearLayout(context);
            currentAppRow.setWeightSum(appsPrRow);
            currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
            currentAppRow.setPadding(0, paddingHeight, 0, paddingHeight);
            targetLayout.addView(currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (AppInfo appInfo : appInfos) {
                if (currentAppRow.getChildCount() == appsPrRow) {
                    currentAppRow = new LinearLayout(context);
                    currentAppRow.setWeightSum(appsPrRow);
                    currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
                    currentAppRow.setPadding(0, 0, 0, paddingHeight);
                    targetLayout.addView(currentAppRow);
                }

                AppImageView newAppView = createGirafLauncherApp(context, currentUser, guardian, appInfo, targetLayout, listener);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                newAppView.setScaleX(0.9f);
                newAppView.setScaleY(0.9f);
                currentAppRow.addView(newAppView);
            }

            int appsInLastRow = ((LinearLayout)targetLayout.getChildAt(targetLayout.getChildCount() - 1)).getChildCount();

            while (appsInLastRow < appsPrRow){
                AppImageView newAppView = new AppImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                newAppView.setScaleX(0.9f);
                newAppView.setScaleY(0.9f);
                currentAppRow.addView(newAppView);
                appsInLastRow = ((LinearLayout)targetLayout.getChildAt(targetLayout.getChildCount() - 1)).getChildCount();
            }

        } else {
            // show no apps available message
            Log.e(Constants.ERROR_TAG, "App list is null");
        }*/

        LoadAndroidApplicationTask loadAndroidApplicationTask = new LoadAndroidApplicationTask(context, currentUser, guardian, targetLayout, iconSize, listener);
        Application[] applications = new Application[girafAppsList.size()];
        girafAppsList.toArray(applications);
        loadAndroidApplicationTask.execute(applications);

        return loadAndroidApplicationTask;
    }

    public static boolean loadOtherApplicationsIntoView(Context context, List<ResolveInfo> appList, LinearLayout targetLayout, int iconSize, Profile currentUser) {
        return loadOtherApplicationsIntoView(context, appList, targetLayout, iconSize, null, currentUser);
    }

    public static boolean loadOtherApplicationsIntoView(Context context, List<ResolveInfo> appList, LinearLayout targetLayout, int iconSize, View.OnClickListener onClickListener, Profile currentUser) {
        /*boolean success = false;

        final SharedPreferences preferences;
        if (currentUser == null)
            preferences = LauncherUtility.getSharedPreferencesForCurrentUser(context);
        else
            preferences = LauncherUtility.getSharedPreferencesForCurrentUser(context, currentUser);

        try {
            clearViewOnUIThread(context, targetLayout);
            //Calculate how many apps the screen can fit on each row, and how much space is available for horizontal padding
            int containerWidth = ((ScrollView) targetLayout.getParent()).getWidth();
            int appsPrRow = getAmountOfAppsWithinBounds(containerWidth, iconSize);

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int containerHeight = ((ScrollView) targetLayout.getParent()).getHeight();
            int appsPrColumn = getAmountOfAppsWithinBounds(containerHeight, iconSize);
            int paddingHeight = getLayoutPadding(containerHeight, appsPrColumn, iconSize);
            //Add the first row to the container
            LinearLayout currentAppRow = new LinearLayout(context);
            currentAppRow.setWeightSum(appsPrRow);
            currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
            currentAppRow.setPadding(0, paddingHeight, 0, paddingHeight);
            //targetLayout.addView(currentAppRow);
            addViewOnUIThread(context, targetLayout, currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (ResolveInfo app : appList) {
                if (currentAppRow.getChildCount() == appsPrRow) {
                    currentAppRow = new LinearLayout(context);
                    currentAppRow.setWeightSum(appsPrRow);
                    currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
                    currentAppRow.setPadding(0, 0, 0, paddingHeight);
                    //targetLayout.addView(currentAppRow);
                    addViewOnUIThread(context, targetLayout, currentAppRow);
                }

                AppImageView newAppView;
                if (onClickListener == null)
                    newAppView = createAppView(context, targetLayout, app);
                else
                    newAppView = createAppView(context, targetLayout, app, onClickListener);

                // Mark colors of the selected apps when settings is shown.
                Set<String> selectedApps = preferences.getStringSet(getString(R.string.selected_android_apps_key), new HashSet<String>());
                if (selectedApps.contains(app.activityInfo.name)){
                    newAppView.setChecked(true);
                }

                newAppView.setTag(app);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                newAppView.setScaleX(0.9f);
                newAppView.setScaleY(0.9f);
                //currentAppRow.addView(newAppView);
                addViewOnUIThread(context, currentAppRow, newAppView);
            }

            int appsInLastRow = ((LinearLayout)targetLayout.getChildAt(targetLayout.getChildCount() - 1)).getChildCount();

            while (appsInLastRow < appsPrRow){
                ImageView newAppView = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                newAppView.setScaleX(0.9f);
                newAppView.setScaleY(0.9f);
                addViewOnUIThread(context, currentAppRow, newAppView);
                //currentAppRow.addView(newAppView);
                appsInLastRow = ((LinearLayout)targetLayout.getChildAt(targetLayout.getChildCount() - 1)).getChildCount();
            }

            success = true;
        } catch (Exception e){
            // Exception happend. Do nothing and return false
            Log.e(Constants.ERROR_TAG, e.getMessage());
            success = false;
        }*/
/*
        LoadAndroidApplicationTask loadAndroidApplicationTask = new LoadAndroidApplicationTask(context, currentUser, targetLayout, iconSize, onClickListener);
        ResolveInfo[] resolveInfos = new ResolveInfo[appList.size()];
        appList.toArray(resolveInfos);
        loadAndroidApplicationTask.execute(resolveInfos);*/

        return true;
    }

    private static void clearViewOnUIThread(Context context, final ViewGroup container){
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                container.removeAllViews();
            }
        });
    }

    private static void addViewOnUIThread(Context context, final ViewGroup container, final View toAdd){
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                container.addView(toAdd);
            }
        });
    }

    protected static int getAmountOfAppsWithinBounds(int containerSize, int iconSize) {
        return containerSize / iconSize;
    }

    protected static int getLayoutPadding(int containerSize, int appsPrRow, int iconSize) {
        return (containerSize % iconSize) / (appsPrRow + 1);
    }

    /**
     * Loads the AppInfo object of app from the list, into the {@code mAppInfoHashMap} hash map, making
     * them accessible with only the ID string of the app.
     * @param appsList The list of accessible apps
     */
    public static HashMap<String,AppInfo> updateAppInfoHashMap(Context context, List<Application> appsList) {
        Application[] appArray = new Application[appsList.size()];
        appArray = appsList.toArray(appArray);

        return updateAppInfoHashMap(context, appArray);
    }

    public static HashMap<String,AppInfo> updateAppInfoHashMap(Context context, Application[] appsList) {
        mAppInfoHashMap = new HashMap<String,AppInfo>();

        for (Application app : appsList) {
            AppInfo appInfo = new AppInfo(app);

            appInfo.load(context);
            appInfo.setBgColor(context.getResources().getColor(R.color.app_color_transparent));

            mAppInfoHashMap.put(String.valueOf(appInfo.getId()), appInfo);
        }
        return mAppInfoHashMap;
    }

    private static View addContentToView(Context context, LinearLayout targetLayout, String appName, Drawable appIcon){
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View targetView = inflater.inflate(R.layout.apps, targetLayout, false);

        ImageView appIconView = (ImageView) targetView.findViewById(R.id.app_icon);
        TextView appTextView = (TextView) targetView.findViewById(R.id.app_text);

        appTextView.setText(appName);
        appIconView.setImageDrawable(appIcon);

        return  targetView;
    }

    /**
     *
     * @param context
     * @param appInfo
     * @param targetLayout
     * @return
     */
    protected static AppImageView createGirafLauncherApp(Context context, final Profile currentUser, final Profile guardian, AppInfo appInfo, LinearLayout targetLayout, View.OnClickListener listener) {

        AppImageView appImageView = new AppImageView(context);
        View appView = addContentToView(context, targetLayout, appInfo.getName(), appInfo.getIconImage());

        setAppBackground(appView, appInfo.getBgColor());

        appImageView.setImageBitmap(SettingsUtility.createBitmapFromLayoutWithText(context, appView, Constants.APP_ICON_DIMENSION_DEF, Constants.APP_ICON_DIMENSION_DEF));
        appImageView.setTag(String.valueOf(appInfo.getApp().getId()));
        appImageView.setOnDragListener(new GAppDragger());

        if(listener == null)
        {
            appImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                    Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.press_app);
                    v.startAnimation(animation);
                    } catch (NullPointerException e){
                        // could not get context, no animation.
                    }
                    AppInfo app = mAppInfoHashMap.get((String) v.getTag());
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName(app.getPackage(), app.getActivity()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    if(currentUser.getRole() == Profile.Roles.CHILD)
                        intent.putExtra(Constants.CHILD_ID, currentUser.getId());
                    else
                        intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);

                    intent.putExtra(Constants.GUARDIAN_ID, guardian.getId());
                    intent.putExtra(Constants.APP_COLOR, app.getBgColor());
                    intent.putExtra(Constants.APP_PACKAGE_NAME, app.getPackage());
                    intent.putExtra(Constants.APP_ACTIVITY_NAME, app.getActivity());

                    // Verify the intent will resolve to at least one activity
                    LauncherUtility.secureStartActivity(v.getContext(), intent);
                }
            });

        }
        else
        {
            appImageView.setOnClickListener(listener);
        }

        return appImageView;
    }

    /**
     * Sets the background of the app.
     * @param wrapperView The view the app is located inside.
     * @param backgroundColor The color to use for the background.
     */
    private static void setAppBackground(View wrapperView, int backgroundColor) {
        LinearLayout appViewLayout = (LinearLayout) wrapperView.findViewById(R.id.app_bg);

        RoundRectShape roundRect = new RoundRectShape( new float[] {15,15, 15,15, 15,15, 15,15}, new RectF(), null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRect);

        shapeDrawable.getPaint().setColor(backgroundColor);

        appViewLayout.setBackgroundDrawable(shapeDrawable);
    }

    /**
     * Retrieves all packages on the device as a list of Resolve Info.
     * @param context Context of the application.
     * @return A list of Resolve Info containing information of all discovered applications.
     */
    public static List<ResolveInfo> getApplicationsFromDevice(Context context){
        return getApplicationsFromDevice(context, DEFAULT_PACKAGE_FILTER, true);
    }

    /**
     * Retrieves a list of applications of on the device which fulfills the filter given through
     * packageFilter.
     * @param context Context of the application.
     * @param packageFilter Filter which needs to be fulfilled.
     * @return A list of Resolve Info containing information of the applications found on device.
     */
    public static List<ResolveInfo> getApplicationsFromDevice(Context context, String packageFilter, boolean shouldContainFilter){
        List<ResolveInfo> appInfo = new ArrayList<ResolveInfo>();
        List<ResolveInfo> apps = getDeviceApps(context);

        for (ResolveInfo app : apps){
            if (!packageFilter.equals(DEFAULT_PACKAGE_FILTER) && shouldIncludeApp(app.activityInfo.packageName, packageFilter, shouldContainFilter))
                appInfo.add(app);
        }
        return appInfo;
    }

    /**
     * Determines whether an application fulfills the requirements of the given filter.
     * @param packageName Package name of the application.
     * @param packageFilter Filter which it needs to fulfill.
     * @return true if the filter is fulfilled false otherwise.
     */
    private static boolean shouldIncludeApp(String packageName, String packageFilter, boolean shouldContainFilter){
        boolean result = false;

        try {
            if (packageName.toLowerCase().contains(packageFilter.toLowerCase())){
                result = shouldContainFilter;
            } else {
                result = !shouldContainFilter;
            }
        } catch (Exception e){
            // An exception happened act as filter was not fulfilled. Return false
        }

        return result;
    }

    /**
     * Creates a view of the given @appInfo parameter. The default onClickListener opens the app
     * @param context Context of the application
     * @param appInfo ResolveInfo of the application which needs to be converted into a view.
     * @return A view of the given application. Containing Icon and name.
     */
    public static AppImageView createAppView(final Context context, LinearLayout targetLayout, final ResolveInfo appInfo){
        PackageManager packageManager = context.getPackageManager();
        View appView = addContentToView(context, targetLayout, appInfo.loadLabel(packageManager).toString(), appInfo.loadIcon(packageManager));
        AppImageView appImageView = new AppImageView(context);
        appImageView.setImageBitmap(SettingsUtility.createBitmapFromLayoutWithText(context, appView, Constants.APP_ICON_DIMENSION_DEF, Constants.APP_ICON_DIMENSION_DEF));

        appImageView.setOnClickListener(new View.OnClickListener() { // OnClickListner to open the applicaiton
            @Override
            public void onClick(View view) {
                ActivityInfo activityInfo = appInfo.activityInfo;
                ComponentName componentName = new ComponentName(activityInfo.packageName, activityInfo.name);

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setComponent(componentName);

                context.startActivity(intent);
            }
        });

        return appImageView;
    }

    /**
     * Creates a view of the given @appInfo parameter. The default onClickListener opens the app.
     * @param context Context of the application.
     * @param appInfo ResolveInfo of the application which needs to be converted into a view.
     * @param onClickListener OnClickListener which is to be set on the view.
     * @return A view of the given application. Containing Icon and name.
     */
    public static AppImageView createAppView(Context context, LinearLayout targetLayout, ResolveInfo appInfo, View.OnClickListener onClickListener){
        AppImageView appView = createAppView(context, targetLayout, appInfo);
        appView.setOnClickListener(onClickListener);

        return appView;
    }

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

    public static String getSharedPreferenceUser(Context context){
        Profile currentUser = getCurrentUser(context);
        return getSharedPreferenceUser(currentUser);
    }

    public static SharedPreferences getSharedPreferencesForCurrentUser(Context context, Profile profile){
        String fileName = Constants.TAG + ".";
        fileName += getSharedPreferenceUser(profile);
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferencesForCurrentUser(Context context){
        Profile currentUser = getCurrentUser(context);
        return getSharedPreferencesForCurrentUser(context, currentUser);
    }
}

