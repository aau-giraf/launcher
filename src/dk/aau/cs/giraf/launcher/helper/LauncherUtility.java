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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.AuthenticationActivity;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.ProfileLauncher;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.GAppDragger;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.settingslib.settingslib.SettingsUtility;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Class for holding static methods and fields, to minimize code duplication.
 */
public class LauncherUtility {

    private static boolean DEBUG_MODE = false;
    private static boolean DEBUG_MODE_AS_CHILD = false;
    private final static String DEFAULT_PACKAGE_FILTER = "";

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
            } else { // activity was not found. We want to be notified on google analytics
                throw new ActivityNotFoundException();
            }
        } catch (ActivityNotFoundException e) {
            // Sending the caught exception to Google Analytics
            LauncherUtility.SendExceptionGoogleAnalytics(context, e);

            Toast toast = Toast.makeText(context, context.getString(R.string.activity_not_found_msg), 2000);
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
     *
     * @param context Context of the current activity.
     * @param e       The caught exception.
     */
    public static void SendExceptionGoogleAnalytics(Context context, Exception e) {
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
     *
     * @param context Context of the current activity.
     * @param id      ID of the guardian logging in.
     */
    public static void saveLogInData(Context context, int id) {
        SharedPreferences sp = context.getSharedPreferences(Constants.TIMER_KEY, 0);
        SharedPreferences.Editor editor = sp.edit();
        Date d = new Date();

        //TODO: This error is because we used to use longs for the time, but the OasisLoib group wants us to use ints now, however, there is no DateTime format in int format.
        editor.putLong(Constants.DATE_KEY, d.getTime());
        editor.putInt(Constants.GUARDIAN_ID, id);

        editor.commit();
    }

    /**
     * Finds the currently logged in user.
     *
     * @param context Context of the current activity.
     * @return Currently logged in user.
     */
    public static Profile findCurrentUser(Context context) {
        Helper helper = getOasisHelper(context);

        int currentUserID = findCurrentUserID(context);

        if (currentUserID == -1) {
            return null;
        } else {
            return helper.profilesHelper.getProfileById(currentUserID);
        }
    }

    /**
     * Finds the ID of the currently logged in user.
     *
     * @param context Context of the current activity.
     * @return ID of the currently logged in user.
     */
    public static int findCurrentUserID(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TIMER_KEY, 0);
        return sharedPreferences.getInt(Constants.GUARDIAN_ID, -1);
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
        SharedPreferences sp = context.getSharedPreferences(Constants.TIMER_KEY, 0);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(Constants.DATE_KEY, 1);
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
        SharedPreferences sp = context.getSharedPreferences(Constants.TIMER_KEY, 0);
        Long lastAuthTime = sp.getLong(Constants.DATE_KEY, 1);
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
     * Returns true if the device currently is held in landscape orientation by the user.
     *
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
            SendExceptionGoogleAnalytics(context, e);
            Log.e(Constants.ERROR_TAG, e.getMessage());
        }
        return helper;
    }

    public static HashMap<String,AppInfo> loadApplicationsIntoView(Context context, List<Application> girafAppsList, LinearLayout targetLayout, int iconSize) {
        //Get the list of apps to show in the container
        //List<Application> girafAppsList = LauncherUtility.getAvailableGirafAppsButLauncher(mContext);
        HashMap<String,AppInfo> appInfos = null;
        if (girafAppsList != null && !girafAppsList.isEmpty()) {
            targetLayout.removeAllViews();
            appInfos = new HashMap<String,AppInfo>();

            //Fill AppInfo hash map with AppInfo objects for each app
            Profile user = LauncherUtility.findCurrentUser(context);
            appInfos = loadAppInfos(context, girafAppsList, user);

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
            targetLayout.addView(currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (Map.Entry<String,AppInfo> entry : appInfos.entrySet()) {
                if (currentAppRow.getChildCount() == appsPrRow) {
                    currentAppRow = new LinearLayout(context);
                    currentAppRow.setWeightSum(appsPrRow);
                    currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
                    currentAppRow.setPadding(0, 0, 0, paddingHeight);
                    targetLayout.addView(currentAppRow);
                }

                ImageView newAppView = createGirafLauncherApp(context, entry.getValue(), targetLayout);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                newAppView.setScaleX(0.9f);
                newAppView.setScaleY(0.9f);
                currentAppRow.addView(newAppView);
            }

            int appsInLastRow = ((LinearLayout)targetLayout.getChildAt(targetLayout.getChildCount() - 1)).getChildCount();

            while (appsInLastRow < appsPrRow){
                ImageView newAppView = new ImageView(context);
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
        }

        return appInfos;
    }


    private static int getAmountOfAppsWithinBounds(int containerSize, int iconSize) {
        return containerSize / iconSize;
    }

    private static int getLayoutPadding(int containerSize, int appsPrRow, int iconSize) {
        return (containerSize % iconSize) / (appsPrRow + 1);
    }

    /**
     * Loads the AppInfo object of app from the list, into the {@code mAppInfos} hashmap, making
     * them accesible with only the ID string of the app.
     * @param appsList The list of accessible apps
     */
    private static HashMap<String,AppInfo> loadAppInfos(Context context, List<Application> appsList, Profile currentUser) {
        HashMap<String,AppInfo> appInfos = new HashMap<String,AppInfo>();

        for (Application app : appsList) {
            AppInfo appInfo = new AppInfo(app);

            appInfo.load(context, currentUser);
            appInfo.setBgColor(context.getResources().getColor(R.color.app_color_transparent));

            appInfos.put(String.valueOf(appInfo.getId()), appInfo);
        }
        return appInfos;
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
    private static ImageView createGirafLauncherApp(Context context, AppInfo appInfo, LinearLayout targetLayout) {
        ImageView appImageView = new ImageView(context);
        final Profile currentUser = LauncherUtility.findCurrentUser(context);
        View appView = addContentToView(context, targetLayout, appInfo.getName(), appInfo.getIconImage());

        setAppBackground(appView, appInfo.getBgColor());

        appImageView.setImageBitmap(SettingsUtility.createBitmapFromLayoutWithText(context, appView, Constants.APP_ICON_DIMENSION_DEF, Constants.APP_ICON_DIMENSION_DEF));
        appImageView.setTag(String.valueOf(appInfo.getApp().getId()));
        appImageView.setOnDragListener(new GAppDragger());

        if(currentUser.getRole() == Profile.Roles.GUARDIAN)
            appImageView.setOnClickListener(new ProfileLauncher());
        else{
            appImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppInfo app = HomeActivity.getAppInfo((String) v.getTag());
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName(app.getPackage(), app.getActivity()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    intent.putExtra(Constants.CHILD_ID, currentUser.getId());
                    intent.putExtra(Constants.APP_COLOR, app.getBgColor());
                    intent.putExtra(Constants.APP_PACKAGE_NAME, app.getPackage());
                    intent.putExtra(Constants.APP_ACTIVITY_NAME, app.getActivity());

                    // Verify the intent will resolve to at least one activity
                    LauncherUtility.secureStartActivity(v.getContext(), intent);
                }
            });
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
     * @param packageManager PackageManager of the device. Is retrieved by
     *                       getActivity().getPackageManager();
     * @return A list of Resolve Info containing information of all discovered applications.
     */
    public static List<ResolveInfo> getApplicationsFromDevice(PackageManager packageManager){
        return getApplicationsFromDevice(packageManager, DEFAULT_PACKAGE_FILTER);
    }

    /**
     * Retrieves a list of applications of on the device which fulfills the filter given through
     * packageFilter.
     * @param packageManager PackageManager of the device. Is retrieved by
     *                       getActivity().getPackageManager();
     * @param packageFilter Filter which needs to be fulfilled.
     * @return A list of Resolve Info containing information of the applications found on device.
     */
    public static List<ResolveInfo> getApplicationsFromDevice(PackageManager packageManager, String packageFilter){
        List<ResolveInfo> appInfo = new ArrayList<ResolveInfo>();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        for (ResolveInfo app : apps){
            if (!packageFilter.equals(DEFAULT_PACKAGE_FILTER) && shouldIncludeApp(app.activityInfo.packageName, packageFilter))
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
    private static boolean shouldIncludeApp(String packageName, String packageFilter){
        try {
            if (packageName.toLowerCase().contains(packageFilter.toLowerCase()))
                return true;
        } catch (Exception e){
            // An exception happened act as filter was not fulfilled. Return false
        }
        return false;
    }

    /**
     * Creates a view of the given @appInfo parameter. The default onClickListener opens the app
     * @param context Context of the application
     * @param appInfo ResolveInfo of the application which needs to be converted into a view.
     * @param packageManager PackageManager of the device. Is retrieved by
     *                       getActivity().getPackageManager();
     * @return A view of the given application. Containing Icon and name.
     */
    public static View createAppView(final Context context, LinearLayout targetLayout, final ResolveInfo appInfo, PackageManager packageManager){

        View appView = addContentToView(context, targetLayout, appInfo.loadLabel(packageManager).toString(), appInfo.loadIcon(packageManager));
        appView.setOnClickListener(new View.OnClickListener() { // OnClickListner to open the applicaiton
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

        return appView;
    }

    /**
     * Creates a view of the given @appInfo parameter. The default onClickListener opens the app.
     * @param context Context of the application.
     * @param appInfo ResolveInfo of the application which needs to be converted into a view.
     * @param packageManager PackageManager of the device. Is retrieved by
     *                       getActivity().getPackageManager();
     * @param onClickListener OnClickListener which is to be set on the view.
     * @return A view of the given application. Containing Icon and name.
     */
    public static View createAppView(Context context, LinearLayout targetLayout, ResolveInfo appInfo, PackageManager packageManager, View.OnClickListener onClickListener){
        View appView = createAppView(context, targetLayout, appInfo, packageManager);
        appView.setOnClickListener(onClickListener);

        return appView;
    }
}
