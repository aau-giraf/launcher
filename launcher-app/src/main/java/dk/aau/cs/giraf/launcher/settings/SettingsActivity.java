package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementFragment;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
* Activity responsible for handling Launcher settings_activity and starting
* other setting-related activities.
*/
public class SettingsActivity extends FragmentActivity
        implements SettingsListFragment.SettingsListFragmentListener,
        AppsFragmentInterface {

    /**
     * The variables mostly used inside the class
     */
    private FragmentManager mFragManager;
    private android.support.v4.app.FragmentManager mSupportFragManager;
    private Fragment mActiveFragment;
    private android.support.v4.app.Fragment mActiveSupportFragment;
    private Profile mCurrentUser = null;
    private Profile mLoggedInGuardian;
    private Helper mHelper;

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

    /**
     * The onCreate method must be overridden as usual, and initialized most of the variables needed by the Activity.
     * In particular, because te SettingsActivity mostly handles Fragments, it initializes the FragmentManager and
     * loads the first fragment needed to be displayed: The first settingsitem in the list.
     *
     * @param savedInstanceState The previously saved InstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        mHelper = LauncherUtility.getOasisHelper(this);

        final int childId = getIntent().getExtras().getInt(Constants.CHILD_ID);
        final int guardianId = getIntent().getExtras().getInt(Constants.GUARDIAN_ID);

        if(childId != Constants.NO_CHILD_SELECTED_ID)
        {
            mCurrentUser = mHelper.profilesHelper.getProfileById(childId);
        }
        else
        {
            mCurrentUser = mHelper.profilesHelper.getProfileById(guardianId);
        }

        mLoggedInGuardian = mHelper.profilesHelper.getProfileById(guardianId);

        // Used to handle fragment changes within the containing View
        mFragManager = this.getFragmentManager();
        mSupportFragManager = this.getSupportFragmentManager();
        Fragment settingsFragment = mFragManager.findFragmentById(R.id.settingsContainer);

        // Check if the fragment already exists
        if (settingsFragment == null) {
            // Select the first entry in the list of applications in settings_activity
            // The first entry is always the general settings
            FragmentSettingsListItem item = (FragmentSettingsListItem) getInstalledSettingsApps().get(0);

            // Update active fragment with the first entry
            if (item.fragment != null) {
                mActiveFragment = item.fragment;
                // Load the fragment just selected into view
                mFragManager.beginTransaction().add(R.id.settingsContainer, mActiveFragment).commit();
            } else {
                mActiveSupportFragment = item.supportFragment;
                // Load the fragment just selected into view
                mSupportFragManager.beginTransaction().add(R.id.settingsContainer, mActiveSupportFragment).commit();
            }
        }
    }

    /**
     * This function gets the elements that should be added to the list of items in the left side
     * Firstly, it gets the settings for Launcher itself, along with the "Apps" menu, where users select or deselect apps.
     * Finally, gets the currently installed apps that have settings to be shown in SettingsActivity.
     * Currently, these apps are only "Cars" (Stemmespillet) and "Zebra" (Sekvens).
     *
     * @return an Array consisting of the SettingsListitems that should be put into the left scrollview.
     */
    @Override
    public ArrayList<SettingsListItem> getInstalledSettingsApps() {
        mAppList = new ArrayList<SettingsListItem>();

        // Launcher
        addApplicationByPackageName("dk.aau.cs.giraf.launcher",
                SettingsLauncher.newInstance(LauncherUtility.getSharedPreferenceUser(mCurrentUser)),
                getString(R.string.settings_tablist_general));

        // Application management
        addApplicationByTitle(getString(R.string.settings_tablist_applications),
                new AppManagementFragment(),
                getResources().getDrawable(R.drawable.ic_apps));

        /************************************************
         *** Add applications in the giraf suite below ***
         *************************************************/
        // TODO: Add giraf applications with settings here

        // Cars
        addApplicationByPackageName("dk.aau.cs.giraf.cars", null);

        // Zebra
        addApplicationByPackageName("dk.aau.cs.giraf.zebra", null);

        /*************************************************
         *** Add applications in the giraf suite above ***
         *************************************************/

        // Get intent for Native Android Settings
        Intent androidSettingsIntent = new Intent(Settings.ACTION_SETTINGS);

        // Start as a new task to enable stepping back to settings_activity
        androidSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        addApplicationByTitle(getResources().getString(R.string.settings_tablist_tablet),
                androidSettingsIntent, getResources().getDrawable(R.drawable.ic_android));

        // Return all applications
        return mAppList;
    }

    /**
     * Add settings to be shown internally in Settings App.
     *
     * @param packageName PackageName of the application to add.
     * @param fragment    Fragment with settings that should be started.
     */
    private void addApplicationByPackageName(String packageName, Fragment fragment, String alias) {
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

            // The package did not exist, just return
            return;
        }

        if (appInfo != null) {
            String title;

            // Test if the provided alias is set
            if (alias == null || alias.isEmpty()) {
                // Extract name of application
                title = pm.getApplicationLabel(appInfo).toString();
            } else {
                title = alias;
            }

            // Extract icon of application
            final Drawable appIcon = pm.getApplicationIcon(appInfo);

            // Create the item
            FragmentSettingsListItem item = new FragmentSettingsListItem(
                    title,
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
     * to start its settings_activity.
     *
     * @param packageName PackageName of the application to add.
     */
    private void addApplicationByPackageName(String packageName, String alias) {
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

        // Create new empty intent
        Intent intent = new Intent();

        // Add SETTINGS_INTENT key to package name to open the
        // settings of the application
        intent.setAction(packageName + SETTINGS_INTENT);

        // Start as a new task to enable stepping back to settings_activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Check if the intent exists
        if (intent.resolveActivity(pm) != null && appInfo != null) {
            String title;

            // Test if the provided alias is set
            if (alias == null || alias.isEmpty()) {
                // Extract name of application
                title = pm.getApplicationLabel(appInfo).toString();
            } else {
                title = alias;
            }

            // Extract icon of application
            final Drawable appIcon = pm.getApplicationIcon(appInfo);

            // A child profile has been selected, pass id
            if (mCurrentUser.getRole() == Profile.Roles.CHILD) {
                intent.putExtra(Constants.CHILD_ID, mCurrentUser.getId());
            }
            // We are a guardian, do not add a child
            else {
                intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);
            }

            // Create new item
            IntentSettingsListItem item = new IntentSettingsListItem(
                    title,
                    appIcon,
                    intent
            );

            // Add item to the list of applications
            mAppList.add(item);
        }
    }

    /**
     * Add another application to the list by Intent.
     *
     * @param title    Name of the application to add.
     * @param fragment Fragment with settings that should be started.
     * @param icon     Custom icon to add to list entry.
     */
    private void addApplicationByTitle(String title, Fragment fragment, Drawable icon) {
        // Create the new item
        FragmentSettingsListItem item = new FragmentSettingsListItem(
                title,
                icon,
                fragment
        );

        // Add item to the list of applications
        mAppList.add(item);
    }

    /**
     * Add another application to the list by Intent.
     *
     * @param title    Name of the application to add.
     * @param fragment Fragment with settings that should be started.
     * @param icon     Custom icon to add to list entry.
     */
    private void addApplicationByTitle(String title, android.support.v4.app.Fragment fragment, Drawable icon) {
        // Create the new item
        FragmentSettingsListItem item = new FragmentSettingsListItem(
                title,
                icon,
                fragment
        );

        // Add item to the list of applications
        mAppList.add(item);
    }

    /**
     * Add settings to be shown internally in Settings App with custom icon.
     *
     * @param title  Name of the application to add.
     * @param intent Intent of the app to start.
     * @param icon   Custom icon to add to list entry.
     */
    private void addApplicationByTitle(String title, Intent intent, Drawable icon) {
        IntentSettingsListItem item = new IntentSettingsListItem(
                title,
                icon,
                intent
        );
        // Add item to the list of applications
        mAppList.add(item);
    }

    /**
     * This function replaces the currently activity fragment in the FragmentManager with a new one
     *
     * @param fragment the fragement that should now be displayed.
     */
    @Override
    public void setActiveFragment(Fragment fragment) {
        // Only add new transaction if the user clicked a non-active fragment
        if (mActiveFragment == null || !mActiveFragment.equals(fragment)) {

            if(mActiveSupportFragment != null)
            {
                android.support.v4.app.FragmentTransaction ft = mSupportFragManager.beginTransaction();
                ft.remove(mActiveSupportFragment);
                ft.commit();
            }

            FragmentTransaction ft = mFragManager.beginTransaction();
            // Replace the fragment in settingsContainer
            ft.replace(R.id.settingsContainer, fragment);
            ft.commit();

            // Update active fragment after transaction has been committed
            mActiveFragment = fragment;
            mActiveSupportFragment = null;
        }
    }

    /**
     * This function replaces the currently activity fragment in the FragmentManager with a new one
     *
     * @param fragment the fragement that should now be displayed.
     */
    @Override
    public void setActiveFragment(android.support.v4.app.Fragment fragment) {

        // Only add new transaction if the user clicked a non-active fragment
        if (mActiveSupportFragment == null || !mActiveSupportFragment.equals(fragment))
        {
            if(mActiveFragment != null)
            {
                FragmentTransaction ft = mFragManager.beginTransaction();
                ft.remove(mActiveFragment);
                ft.commit();
            }

            android.support.v4.app.FragmentTransaction ft = mSupportFragManager.beginTransaction();
            // Replace the fragment in settingsContainer
            ft.replace(R.id.settingsContainer, fragment);
            ft.commit();

            // Update active fragment after transaction has been committed
            mActiveFragment = null;
            mActiveSupportFragment = fragment;
        }
    }

    /**
     * This function finishes the current instance of SettingsActivity and starts a new instance of it.
     * Because it is used when switching to a new user, it needs to be overridden,
     * so the currentUser of the new SettingsActivity is the new user chosen.
     */
    @Override
    public void reloadActivity() {
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

    /**
     * Sets the current profile
     *
     * @param profile The selected profile.
     */
    @Override
    public void setCurrentUser(Profile profile) {
        mCurrentUser = profile;
    }

    public Profile getCurrentUser() {
        return mCurrentUser;
    }

    @Override
    public Profile getLoggedInGuardian() {
        return mLoggedInGuardian;
    }
}