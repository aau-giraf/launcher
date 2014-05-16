package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementSettings;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Activity responsible for handling Launcher settings and starting
 * other setting-related activities.
 */
public class SettingsActivity extends Activity
        implements SettingsListFragment.SettingsListFragmentListener,
        AppsFragmentInterface {

    private FragmentManager mFragManager;
    private Fragment mActiveFragment;
    private Profile mCurrentUser;

    /**
     * Global variable containing giraf applications with settings.
     * ALL apps with settings are added to this list, which is
     * later filtered to remove applications that are
     * unavailable on the device.
     */
    private ArrayList<SettingsListItem> mAppList;

    /**
     * String constant used to identify the name of the intent
     * other giraf applications must put available through an intent-filter.
     * It is prefixed with the application package name when creating the intent.
     */
    private static final String SETTINGS_INTENT = ".SETTINGSACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Used to handle fragment changes within the containing View
        mFragManager = this.getFragmentManager();
        Fragment settingsFragment = mFragManager.findFragmentById(R.id.settingsContainer);

        // Check if the fragment already exists
        if (settingsFragment == null) {
            // Select the first entry in the list of applications in settings
            SettingsListItem item = getInstalledSettingsApps().get(0);
            // Update active fragment with the first entry
            mActiveFragment = item.mAppFragment;
        }
        // Load the fragment just selected into view
        mFragManager.beginTransaction().add(R.id.settingsContainer, mActiveFragment)
                .commit();
    }


    @Override
    public ArrayList<SettingsListItem> getInstalledSettingsApps(){
        mAppList = new ArrayList<SettingsListItem>();

        // Launcher
        addApplicationByPackageName("dk.aau.cs.giraf.launcher",
                new SettingsLauncher(LauncherUtility.getSharedPreferenceUser(mCurrentUser)));

        // Application management
        addApplicationByName(getString(R.string.apps_list_label),
                new AppManagementSettings(), getResources().getDrawable(R.drawable.ic_apps));

        /************************************************
         *** Add applications in the giraf suite below ***
         *************************************************/
        // TODO: Add giraf applications with settings here

        // Cars
        addApplicationByPackageName("dk.aau.cs.giraf.cars");

        // Zebra
        addApplicationByPackageName("dk.aau.cs.giraf.zebra");

        /*************************************************
         *** Add applications in the giraf suite above ***
         *************************************************/

        // Native android settings - add last
        addAndroidSettings();

        // Only add the apps available on the device
        return removeNonGirafApps(mAppList);
    }

    /**
     * Filter an existing list of applications to remove apps that are not valid giraf apps or
     * apps that are not available on the device.
     * @param list List to remove invalid apps from.
     * @return A new list of valid (available) apps.
     */
    private ArrayList<SettingsListItem> removeNonGirafApps(ArrayList<SettingsListItem> list) {
        // Clone the input list to be able to remove invalid apps
        ArrayList<SettingsListItem> mAvailableSettingsAppList = (ArrayList<SettingsListItem>) list.clone();
        // Get all installed giraf apps
        List<ResolveInfo> installedGirafApps = ApplicationControlUtility.getGirafAppsOnDeviceAsResolveInfoList(this);

        for (SettingsListItem settingsApp : list) {
            for (ResolveInfo installedApp : installedGirafApps) {
                // Get the package name of each application
                String installedAppName = installedApp.activityInfo.applicationInfo.packageName.toLowerCase();

                // Only add to app to settings if not already in the list of available apps
                if (!mAvailableSettingsAppList.contains(settingsApp)) {

                    // Add app to settings if it is installed
                    if (settingsApp.mPackageName != null && installedAppName.contains(settingsApp.mPackageName.toLowerCase())) {
                        mAvailableSettingsAppList.add(settingsApp);
                    // Otherwise it has been added by name and is started through fragment/intent
                    } else if (settingsApp.mAppName != null && (settingsApp.mAppFragment != null || settingsApp.mIntent != null)) {
                        mAvailableSettingsAppList.add(settingsApp);
                    }
                }
            }
        }
        // Return the list containing apps available on the device (giraf and other)
        return mAvailableSettingsAppList;
    }

    /**
     * Add settings to be shown internally in Settings App.
     * @param packageName PackageName of the application to add.
     * @param fragment Fragment with settings that should be started.
     */
    private void addApplicationByPackageName(String packageName, Fragment fragment) {
        // Get the package manager to query package name
        final PackageManager pm = getApplicationContext().getPackageManager();
        // New container for application we want to add, initially null
        ApplicationInfo appInfo = null;

        try {
            // Check if the package name exists on the device
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            // Don't throw exception, just print stack trace
            e.printStackTrace();
        }

        if (appInfo != null) {
            // Extract name of application
            final String appName = pm.getApplicationLabel(appInfo).toString();
            // Extract icon of application
            final Drawable appIcon = pm.getApplicationIcon(appInfo);

            SettingsListItem item = new SettingsListItem(
                    packageName,
                    appName,
                    appIcon,
                    fragment
            );
            // Add item to the list of applications
            mAppList.add(item);
        }
    }

    /**
     * Add settings from another giraf application.
     * The icon is automatically extracted from the package and the giraf intent action is
     * appended to query the intent-filter the application should implement
     * to start its settings activity.
     * @param packageName PackageName of the application to add.
     */
    private void addApplicationByPackageName(String packageName) {
        // Get the package manager to query package name
        final PackageManager pm = getApplicationContext().getPackageManager();
        // New container for application we want to add, initially null
        ApplicationInfo appInfo = null;

        try {
            // Check if the package name exists on the device
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            // Don't throw exception, just print stack trace
            e.printStackTrace();
            // The package does not exist, return
            return;
        }

        Intent intent = new Intent();
        // Add SETTINGS_INTENT key to package name to open the
        // settings of the application
        intent.setAction(packageName + SETTINGS_INTENT);
        // Start as a new task to enable stepping back to settings
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Check if the intent exists
        if(intent.resolveActivity(pm) != null) {
            if (appInfo != null) {
                // Extract name of application
                final String appName = pm.getApplicationLabel(appInfo).toString();
                // Extract icon of application
                final Drawable appIcon = pm.getApplicationIcon(appInfo);

                if (mCurrentUser.getRole() == Profile.Roles.CHILD) // A child profile has been selected, pass id
                    intent.putExtra(Constants.CHILD_ID, mCurrentUser.getId());
                else // We are a guardian, do not add a child
                    intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);

                SettingsListItem item = new SettingsListItem(
                        packageName,
                        appName,
                        appIcon,
                        intent
                );
                // Add item to the list of applications
                mAppList.add(item);
            }
        }
    }

    /**
     * Add another application to the list by Intent.
     * @param appName Name of the application to add.
     * @param fragment Fragment with settings that should be started.
     * @param icon Custom icon to add to list entry.
     */
    private void addApplicationByName(String appName, Fragment fragment, Drawable icon) {
        SettingsListItem item = new SettingsListItem(
                appName,
                icon,
                fragment
        );
        // Add item to the list of applications
        mAppList.add(item);
    }

    /**
     * Add settings to be shown internally in Settings App with custom icon.
     * @param appName Name of the application to add.
     * @param intent Intent of the app to start.
     * @param icon Custom icon to add to list entry.
     */
    private void addApplicationByName(String appName, Intent intent, Drawable icon) {
        SettingsListItem item = new SettingsListItem(
                appName,
                icon,
                intent
        );
        // Add item to the list of applications
        mAppList.add(item);
    }

    /**
     * Add an entry with native android settings to the list.
     */
    private void addAndroidSettings() {
        // Get intent for Native Android Settings
        Intent androidSettingsIntent = new Intent(Settings.ACTION_SETTINGS);
        // Start as a new task to enable stepping back to settings
        androidSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        addApplicationByName(getResources().getString(R.string.giraf_settings_name),
                androidSettingsIntent, getResources().getDrawable(R.drawable.ic_android));
    }

    @Override
    public void setActiveFragment(Fragment fragment) {
        // Only add new transaction if the user clicked a non-active fragment
        if (mActiveFragment != fragment) {
            FragmentTransaction ft = mFragManager.beginTransaction();
            // Replace the fragment in settingsContainer
            ft.replace(R.id.settingsContainer, fragment);
            ft.commit();

            // Update active fragment after transaction has been committed
            mActiveFragment = fragment;
        }
    }

    @Override
    public void reloadActivity()
    {
        // Get the intent of SettingsActivity
        Intent intent = SettingsActivity.this.getIntent();

        if (mCurrentUser.getRole() == Profile.Roles.CHILD) // A child profile has been selected, pass id
            intent.putExtra(Constants.CHILD_ID, mCurrentUser.getId());
        else // We are a guardian, do not add a child
            intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);

        // Stop activity before restarting
        SettingsActivity.this.finish();
        // Start activity again to reload contents
        startActivity(intent);
    }

    @Override
    public void setCurrentUser(Profile profile) {
            mCurrentUser = profile;
    }

    @Override
    public Profile getSelectedProfile() {
        return mCurrentUser;
    }
}