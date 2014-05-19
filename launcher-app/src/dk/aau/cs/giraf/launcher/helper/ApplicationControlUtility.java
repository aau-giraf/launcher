package dk.aau.cs.giraf.launcher.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * This class contains functions that are used to either:
 *  1. Return lists of applications.
 *  2. Check if certain lists of applications live up to certain criteria.
 */
public class ApplicationControlUtility {

    private final static String ANDROID_FILTER = "dk.aau.cs.giraf";

    /**
     * Gets the GIRAF apps that are usable by the given user, relative to their settings and the system they're logged in on.
     * @param context Context of the current activity.
     * @param user    The user to find apps for.
     * @return List of apps that are usable by this user on this device.
     */
    public static List<Application> getAvailableGirafAppsForUser(Context context, Profile user) {
        Helper helper = LauncherUtility.getOasisHelper(context);

        List<Application> userApps = helper.applicationHelper.getApplicationsByProfile(user);
        List<Application> deviceApps = getGirafAppsOnDeviceAsApplicationList(context);

        if (userApps.isEmpty() || deviceApps.isEmpty()) {
            return new ArrayList<Application>();
        }

        // Remove from the list, all apps that are not installed on the device and exclude the launcher itself.
        for (int i = 0; i < userApps.size(); i++) {
            if (!doesApplicationListContainApp(deviceApps, userApps.get(i)) || userApps.get(i).getPackage().equals("dk.aau.cs.giraf.launcher")) {
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
    private static List<Application> getGirafAppsOnDeviceAsApplicationList(Context context) {
        Helper helper = LauncherUtility.getOasisHelper(context);

        List<Application> dbApps = helper.applicationHelper.getApplications();
        List<ResolveInfo> deviceApps = getGirafAppsOnDeviceAsResolveInfoList(context);

        if (dbApps.isEmpty() || deviceApps.isEmpty()) {
            return new ArrayList<Application>();
        }

        for (int i = 0; i < dbApps.size(); i++) {
            if (!doesResolveInfoListContainApp(deviceApps, dbApps.get(i))) {
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
    public static List<Application> getGirafAppsOnDeviceButLauncherAsApplicationList(Context context) {
        Helper helper = LauncherUtility.getOasisHelper(context);

        List<Application> dbApps = helper.applicationHelper.getApplications();
        List<ResolveInfo> deviceApps = getGirafAppsOnDeviceAsResolveInfoList(context);

        if (dbApps.isEmpty() || deviceApps.isEmpty()) {
            return new ArrayList<Application>();
        }

        //TODO: Launcher name should not be hardcoded!
        for (int i = 0; i < dbApps.size(); i++) {
            String name = dbApps.get(i).getName();
            if (!doesResolveInfoListContainApp(deviceApps, dbApps.get(i)) || name.equals("Launcher")) {
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
    public static List<ResolveInfo> getGirafAppsOnDeviceAsResolveInfoList(Context context) {
        List<ResolveInfo> systemApps = getAllAppsOnDeviceAsResolveInfoList(context);

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
    public static List<ResolveInfo> getAllAppsOnDeviceAsResolveInfoList(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        return context.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    /**
     * This function returns Android Applications installed on the device as a
     * list of applications, based on a given filter
     * @param context The context of the current activity
     * @return
     */
    public static List<Application> getAndroidAppsOnDeviceAsApplicationList(Context context){
        List<ResolveInfo> allApps = getAllAppsOnDeviceAsResolveInfoList(context);
        List<Application> result = new ArrayList<Application>();
        PackageManager packageManager = context.getPackageManager();

        for(ResolveInfo app : allApps){
            String appActivityName = app.activityInfo.name;
            String appPackageName = app.activityInfo.packageName;

            if (appPackageName.contains(ANDROID_FILTER))
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

    /**
     * Checks whether a list of GIRAF apps installed on the system contains a specified app.
     *
     * @param systemApps List of apps (as ResolveInfos) to check.
     * @param app        The app to check for.
     * @return True if the app is contained in the list; otherwise false.
     */
    public static boolean doesResolveInfoListContainApp(List<ResolveInfo> systemApps, Application app) {
        return doesResolveInfoListContainApp(systemApps, app.getPackage());
    }

    /**
     * Checks whether a list of GIRAF apps installed on the system contains a specified app.
     *
     * @param systemApps  List of apps (as ResolveInfos) to check.
     * @param packageName Package name of the app to check for.
     * @return True if the app is contained in the list; otherwise false.
     */
    public static boolean doesResolveInfoListContainApp(List<ResolveInfo> systemApps, String packageName) {
        for (ResolveInfo app : systemApps) {
            if (app.activityInfo.packageName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a list of apps installed on the system contains a specified app.
     *
     * @param systemApps List of apps (as Apps) to check.
     * @param app        The app to check for.
     * @return True if the app is contained in the list; otherwise false.
     */
    public static boolean doesApplicationListContainApp(List<Application> systemApps, Application app) {
        return doesApplicationListContainApp(systemApps, app.getPackage());
    }

    /**
     * Checks whether a list of apps installed on the system contains a specified app.
     *
     * @param systemApps  List of apps (as Apps) to check.
     * @param packageName Package name of the app to check for.
     * @return True if the app is contained in the list; otherwise false.
     */
    public static boolean doesApplicationListContainApp(List<Application> systemApps, String packageName) {
        for (Application app : systemApps) {
            if (app.getPackage().equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This function convert a list of Package names to a list of Applications.
     * @param context The context of the current activity
     * @param packageNames The list of Package names to be converted into Applications
     * @return
     */
    public static List<Application> convertPackageNamesToApplications(Context context, Set<String> packageNames){
        List<ResolveInfo> allApps = getAllAppsOnDeviceAsResolveInfoList(context);
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
}
