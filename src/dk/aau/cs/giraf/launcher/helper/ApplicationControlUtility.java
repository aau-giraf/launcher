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
 * Created by Vagner on 13-05-14.
 */
public class ApplicationControlUtility {

    /**
     * Gets the GIRAF apps that are usable by the given user, relative to their settings and the system they're logged in on.
     *
     * @param context Context of the current activity.
     * @param user    The user to find apps for.
     * @return List of apps that are usable by this user on this device.
     */
    public static List<Application> getAppsAvailableForUser(Context context, Profile user) {
        Helper helper = LauncherUtility.getOasisHelper(context);

        List<Application> userApps = helper.applicationHelper.getApplicationsByProfile(user);
        List<Application> deviceApps = getAvailableGirafApps(context);

        if (userApps.isEmpty() || deviceApps.isEmpty()) {
            return new ArrayList<Application>();
        }

        // Remove all apps from user's list of apps that are not installed on the device and exclude the launcher it self.
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
    private static List<Application> getAvailableGirafApps(Context context) {
        Helper helper = LauncherUtility.getOasisHelper(context);

        List<Application> dbApps = helper.applicationHelper.getApplications();
        List<ResolveInfo> deviceApps = getGirafAppsInstalledOnDevice(context);

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
    public static List<Application> getAvailableGirafAppsButLauncher(Context context) {
        Helper helper = LauncherUtility.getOasisHelper(context);

        List<Application> dbApps = helper.applicationHelper.getApplications();
        List<ResolveInfo> deviceApps = getGirafAppsInstalledOnDevice(context);

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
    public static List<ResolveInfo> getGirafAppsInstalledOnDevice(Context context) {
        List<ResolveInfo> systemApps = getAppsInstalledOnDevice(context);

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
    public static List<ResolveInfo> getAppsInstalledOnDevice(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        return context.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    public static List<Application> getAndroidApplicationList(Context context, String filter){
        List<ResolveInfo> allApps = getAppsInstalledOnDevice(context);
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
     * Checks whether a list of GIRAF apps installed on the system contains a specified app.
     *
     * @param systemApps List of apps (as Apps) to check.
     * @param app        The app to check for.
     * @return True if the app is contained in the list; otherwise false.
     */
    public static boolean doesApplicationListContainApp(List<Application> systemApps, Application app) {
        return doesApplicationListContainApp(systemApps, app.getPackage());
    }

    /**
     * Checks whether a list of GIRAF apps installed on the system contains a specified app.
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

    public static List<Application> convertPackageNamesToApplications(Context context, Set<String> packageNames){
        List<ResolveInfo> allApps = getAppsInstalledOnDevice(context);
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
