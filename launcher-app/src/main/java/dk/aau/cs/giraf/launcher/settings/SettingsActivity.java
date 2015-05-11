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
import android.view.View;

import java.util.ArrayList;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafProfileSelectorDialog;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementFragment;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;

/**
 * Activity responsible for handling Launcher settings_activity and starting
 * other setting-related activities.
 */
public class SettingsActivity extends GirafActivity
        implements SettingsListFragment.SettingsListFragmentListener,
        GirafProfileSelectorDialog.OnSingleProfileSelectedListener,
        AppsFragmentInterface {

    private static final int CHANGE_USER_SELECTOR_DIALOG = 100;

    /**
     * The variables mostly used inside the class
     */
    private FragmentManager mFragManager;
    private android.support.v4.app.FragmentManager mSupportFragManager;
    private Fragment mActiveFragment;
    private android.support.v4.app.Fragment mActiveSupportFragment;
    private Profile mCurrentUser = null;
    private Profile mLoggedInGuardian;

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


        final Helper mHelper = LauncherUtility.getOasisHelper(this);

        final long childId = getIntent().getExtras().getLong(Constants.CHILD_ID);
        final long guardianId = getIntent().getExtras().getLong(Constants.GUARDIAN_ID);

        mLoggedInGuardian = mHelper.profilesHelper.getById(guardianId);

        if (childId != Constants.NO_CHILD_SELECTED_ID) {
            mCurrentUser = mHelper.profilesHelper.getById(childId);
        } else {
            mCurrentUser = mHelper.profilesHelper.getById(guardianId);
        }

        // Change the title of the action bar to include the name of the current user
        if (mCurrentUser != null) {
            this.setActionBarTitle(getString(R.string.settingsFor) + mCurrentUser.getName());
        }

        GirafButton changeUserButton = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_change_user));
        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GirafProfileSelectorDialog changeUser = GirafProfileSelectorDialog.newInstance(SettingsActivity.this, mLoggedInGuardian.getId(), false, false, "Vælg den borger du vil skifte til.", CHANGE_USER_SELECTOR_DIALOG);
                changeUser.show(getSupportFragmentManager(), "" + CHANGE_USER_SELECTOR_DIALOG);
            }
        });

        addGirafButtonToActionBar(changeUserButton, LEFT);

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
            FragmentSettingsListItem item = new FragmentSettingsListItem(title, appIcon, fragment);

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
    private void addApplicationByTitle(String title, android.support.v4.app.Fragment fragment, Drawable icon) {
        // Create the new item
        FragmentSettingsListItem item = new FragmentSettingsListItem(title, icon, fragment);

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

        IntentSettingsListItem item = new IntentSettingsListItem(title, icon, intent);
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

            if (mActiveSupportFragment != null) {
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
        if (mActiveSupportFragment == null || !mActiveSupportFragment.equals(fragment)) {
            if (mActiveFragment != null) {
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

    /**
     * gets the current user
     *
     * @return
     */
    public Profile getCurrentUser() {
        return mCurrentUser;
    }

    /**
     * get the current logged in guardian
     *
     * @return
     */
    @Override
    public Profile getLoggedInGuardian() {
        return mLoggedInGuardian;
    }

    @Override
    public void onProfileSelected(final int i, final Profile profile) {

        if (i == CHANGE_USER_SELECTOR_DIALOG) {

            // Notify that a profile selection has been made
            setCurrentUser(profile);

            // Reload activity to reflect different user settings
            reloadActivity();
        }

    }
}