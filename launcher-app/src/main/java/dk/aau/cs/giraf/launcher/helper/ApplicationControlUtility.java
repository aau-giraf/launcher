package dk.aau.cs.giraf.launcher.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.models.core.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class contains functions that are used to either:
 *  1. Return lists of applications.
 *  2. Check if certain lists of applications live up to certain criteria.
 */
public class ApplicationControlUtility {

    private static final String ANDROID_FILTER = "dk.aau.cs.giraf";


    /**
     * Returns true or false is the app is in on the device.
     * @param app the app.
     * @param context the context.
     * @return true if the app exist or false if not.
     */
    public static boolean isAppOnDevice(Application app, Context context){
        try {
            context.getPackageManager().getPackageInfo(app.getPackage(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e){
            return  false;
        }
    }

    /**
     * Finds all GIRAF apps installed on the device.
     *
     * @param context Context of the current activity.
     * @return List of GIRAF apps.
     */
    public static List<Application> getGirafAppsButLauncherOnDevice(Context context) {
        List<ResolveInfo> systemApps = getAllAppsOnDeviceAsResolveInfoList(context);
        List<Application> applications = new ArrayList<Application>();
        if (systemApps.isEmpty()) {
            return applications;
        }
        String girafNameSpace = context.getString(R.string.giraf_namespace);
        String girafLauncherNameSpace = context.getString(R.string.launcher_namespace);
        for(ResolveInfo info : systemApps) {
            String infoString = info.toString().toLowerCase();
            if(infoString.contains(girafNameSpace) && !infoString.contains(girafLauncherNameSpace)){
                String appActivityName = info.activityInfo.name;
                String appPackageName = info.activityInfo.packageName;
                String appName = info.activityInfo.loadLabel(context.getPackageManager()).toString();
                applications.add(new Application(appName, appPackageName, appActivityName));
            }
        }
        return applications;
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
     * list of applications, based on a given filter.
     * @param context The context of the current activity
     * @return list of application
     */
    public static List<Application> getAndroidAppsOnDeviceAsApplicationList(Context context) {
        List<ResolveInfo> allApps = getAllAppsOnDeviceAsResolveInfoList(context);
        List<Application> result = new ArrayList<Application>();
        PackageManager packageManager = context.getPackageManager();

        for (ResolveInfo app : allApps) {
            String appActivityName = app.activityInfo.name;
            String appPackageName = app.activityInfo.packageName;

            if (appPackageName.contains(ANDROID_FILTER))
                continue;


            Application application = new Application(
                app.activityInfo.loadLabel(packageManager).toString(),appPackageName,appActivityName);
            application.setId(app.hashCode()); //Todo find out if the id is something that is handled by us or the database
            result.add(application);
        }
        return result;
    }

    /**
     * This function convert a list of Package names to a list of Applications.
     * @param context The context of the current activity
     * @param packageNames The list of Package names to be converted into Applications
     * @return List of applications
     */
    public static List<Application> convertPackageNamesToApplications(Context context, Set<String> packageNames) {
        List<ResolveInfo> allApps = getAllAppsOnDeviceAsResolveInfoList(context);
        List<Application> selectedApps = new ArrayList<Application>();
        PackageManager packageManager = context.getPackageManager();

        outerloop:
        for (ResolveInfo app : allApps) {
            for (String activityName : packageNames) {
                String appActivityName = app.activityInfo.name;
                String appPackageName = app.activityInfo.packageName;
                if (appActivityName.equals(activityName)) {
                    Application application = new Application(
                        app.activityInfo.loadLabel(packageManager).toString(),appPackageName,appActivityName);
                    application.setId(app.hashCode());//Todo find out if the id is something that is handled by us or the database
                    selectedApps.add(application);

                    continue outerloop;
                }
            }
        }
        return selectedApps;
    }
}
