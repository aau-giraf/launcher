package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;
import dk.aau.cs.giraf.gui.GirafProfileSelectorDialog;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AndroidFragment;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppContainerFragment;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementFragment;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.GirafFragment;
import dk.aau.cs.giraf.showcaseview.ShowcaseManager;
import dk.aau.cs.giraf.showcaseview.ShowcaseView;
import dk.aau.cs.giraf.showcaseview.targets.Target;
import dk.aau.cs.giraf.showcaseview.targets.ViewTarget;

/**
 * Activity responsible for handling Launcher settings_activity and starting
 * other setting-related activities.
 */
public class SettingsActivity extends GirafActivity
        implements SettingsListFragment.SettingsListFragmentListener,
        GirafProfileSelectorDialog.OnSingleProfileSelectedListener,
        AppsFragmentInterface, ShowcaseManager.ShowcaseCapable {

    private static final String IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY_SETTINGS_ACTIVITY";

    private static final int CHANGE_USER_SELECTOR_DIALOG = 100;

    /**
     * The variables mostly used inside the class
     */
    private FragmentManager mFragManager;
    private android.support.v4.app.FragmentManager mSupportFragManager;

    private Profile mCurrentUser = null;
    private Profile mLoggedInGuardian;

    private ListView mSettingsListView;
    private SettingsListAdapter mAdapter;

    // Used to implement help functionality (ShowcaseView)
    private ShowcaseManager showcaseManager;
    private boolean isFirstRun;

    /**
     * Used in onResume and onPause for handling showcaseview for first run
     */
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

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

        mSettingsListView = (ListView) findViewById(R.id.settingsListView);

        mSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsListItem item = (SettingsListItem) parent.getAdapter().getItem(position);

                // If the item contains a fragment, set it as active
                if (item instanceof FragmentSettingsListItem) {

                    FragmentSettingsListItem fragmentItem = ((FragmentSettingsListItem) item);

                    // Notify class implementing the callback interface that a new fragment has been selected
                    if (fragmentItem.fragment == null) {
                        setActiveFragment(fragmentItem.supportFragment);
                    } else {
                        setActiveFragment(fragmentItem.fragment);
                    }

                    // Update the adapter to reflect the selection
                    mAdapter.setSelected(position);
                }
                // Otherwise it must be an intent
                else if (item instanceof IntentSettingsListItem) {
                    try {
                        // Start a new activity with the intent
                        startActivity(((IntentSettingsListItem) item).intent);
                    }
                    // Handle exception if the intended activity can not be started.
                    catch (ActivityNotFoundException e) {
                        Toast.makeText(parent.getContext(), R.string.settings_activity_not_found_msg, Toast.LENGTH_SHORT).show();
                    } finally {
                        // Notify adapter to redraw views since we want to reset the visual style
                        // of the selected since its state should not be preserved (= reset selected item
                        // to the item selected when starting the Intent.
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        final GirafPictogramItemView mProfileButton = (GirafPictogramItemView) findViewById(R.id.profile_widget_settings);

        ProfileController pc = new ProfileController(this);

        final long childID = this.getIntent().getLongExtra(Constants.CHILD_ID, -1);

        Profile mCurrentUser;

        // The childID is -1 meaning that no childs are available
        if (childID == -1) {
            mCurrentUser = pc.getById(this.getIntent().getLongExtra(Constants.GUARDIAN_ID, -1));
        } else { // A child is found - set it as active and add its profile selector
            mCurrentUser = pc.getById(childID);
        }
        // Notify about the current user
        setCurrentUser(mCurrentUser);

        // Instantiates a new adapter to render the items in the ListView with a list of installed (available) apps
        mAdapter = new SettingsListAdapter(this, mSettingsListView, this.getInstalledSettingsApps());
        // Set the new adapter in the ListView
        mSettingsListView.setAdapter(mAdapter);

        mAdapter.setSelected(0);

        //Load the correct profile picture for the choosen profile
        mProfileButton.setImageModel(mCurrentUser, this.getResources().getDrawable(R.drawable.no_profile_pic));
        mProfileButton.setTitle(mCurrentUser.getName());

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

        final GirafButton changeUserButton = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_change_user));
        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GirafProfileSelectorDialog changeUser = GirafProfileSelectorDialog.newInstance(SettingsActivity.this, mLoggedInGuardian.getId(), false, false, "Vælg den borger du vil skifte til.", CHANGE_USER_SELECTOR_DIALOG);
                changeUser.show(getSupportFragmentManager(), "" + CHANGE_USER_SELECTOR_DIALOG);
            }
        });

        addGirafButtonToActionBar(changeUserButton, LEFT);

        final GirafButton helpGirafButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_help));
        helpGirafButton.setId(R.id.help_button);
        helpGirafButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleShowcase();
            }
        });

        addGirafButtonToActionBar(helpGirafButton, GirafActivity.RIGHT);

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
                // Load the fragment just selected into view
                mFragManager.beginTransaction().add(R.id.settingsContainer, item.fragment).commit();
            } else {
                // Load the fragment just selected into view
                mSupportFragManager.beginTransaction().add(R.id.settingsContainer, item.supportFragment).commit();
            }
        }

        mFragManager.executePendingTransactions();
        mSupportFragManager.executePendingTransactions();
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
                getResources().getDrawable(R.drawable.icon_applications));


        // Get intent for Native Android Settings
        final Intent androidSettingsIntent = new Intent(Settings.ACTION_SETTINGS);

        // Start as a new task to enable stepping back to settings_activity
        androidSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        addApplicationByTitle(getResources().getString(R.string.settings_tablist_tablet),
                androidSettingsIntent, getResources().getDrawable(R.drawable.icon_android));

        // Launcher
        addApplicationByPackageName("dk.aau.cs.giraf.launcher",
                AboutFragment.newInstance(),
                getString(R.string.about_giraf_tab_title));

        // Return all applications
        return mAppList;
    }

    /**
     * Add settings to be shown internally in Settings App.
     *
     * @param packageName PackageName of the application to add.
     * @param fragment    Fragment with settings that should be started.
     */
    private void addApplicationByPackageName(final String packageName, final Fragment fragment, final String alias) {

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
            // TODO: This a quick fix to show the correct icon in the list. This is however alright since the sidebar is not used for any other apps
            final Drawable appIcon = this.getResources().getDrawable(R.drawable.icon_giraf_no_background); //pm.getApplicationIcon(appInfo);

            // Create the item
            final FragmentSettingsListItem item = new FragmentSettingsListItem(title, appIcon, fragment);

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
    private void addApplicationByTitle(final String title, final android.support.v4.app.Fragment fragment, final Drawable icon) {

        // Create the new item
        final FragmentSettingsListItem item = new FragmentSettingsListItem(title, icon, fragment);

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
    private void addApplicationByTitle(final String title, final Intent intent, final Drawable icon) {

        final IntentSettingsListItem item = new IntentSettingsListItem(title, icon, intent);

        // Add item to the list of applications
        mAppList.add(item);
    }

    /**
     * This function replaces the currently activity fragment in the FragmentManager with a new one
     *
     * @param fragment the fragement that should now be displayed.
     */
    @Override
    public void setActiveFragment(final Fragment fragment) {

        final Fragment mActiveFragment = mFragManager.findFragmentById(R.id.settingsContainer);

        // Only add new transaction if the user clicked a non-active fragment
        if (mActiveFragment == null || !mActiveFragment.equals(fragment)) {

            final android.support.v4.app.Fragment mActiveSupportFragment = mSupportFragManager.findFragmentById(R.id.settingsContainer);

            if (mActiveSupportFragment != null) {
                android.support.v4.app.FragmentTransaction ft = mSupportFragManager.beginTransaction();
                ft.remove(mActiveSupportFragment);
                ft.commit();
            }

            final FragmentTransaction ft = mFragManager.beginTransaction();

            // Replace the fragment in settingsContainer
            ft.replace(R.id.settingsContainer, fragment);
            ft.commit();
        }
    }

    /**
     * This function replaces the currently activity fragment in the FragmentManager with a new one
     *
     * @param fragment the fragement that should now be displayed.
     */
    @Override
    public void setActiveFragment(final android.support.v4.app.Fragment fragment) {

        final android.support.v4.app.Fragment mActiveSupportFragment = mSupportFragManager.findFragmentById(R.id.settingsContainer);

        // Only add new transaction if the user clicked a non-active fragment
        if (mActiveSupportFragment == null || !mActiveSupportFragment.equals(fragment)) {
            final Fragment mActiveFragment = mFragManager.findFragmentById(R.id.settingsContainer);

            if (mActiveFragment != null) {
                FragmentTransaction ft = mFragManager.beginTransaction();
                ft.remove(mActiveFragment);
                ft.commit();
            }

            android.support.v4.app.FragmentTransaction ft = mSupportFragManager.beginTransaction();

            // Replace the fragment in settingsContainer
            ft.replace(R.id.settingsContainer, fragment);
            ft.commit();
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
        final Intent intent = SettingsActivity.this.getIntent();

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
     * Get the current logged in guardian
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

    @Override
    protected void onResume() {
        super.onResume();

        // Check if this is the first run of the app
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.isFirstRun = prefs.getBoolean(IS_FIRST_RUN_KEY, true);

        if (isFirstRun) {
            showShowcase();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(IS_FIRST_RUN_KEY, false);
            editor.commit();
        }
        /*
        // If it is the first run display ShowcaseView
        if (isFirstRun) {
            mSettingsListView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    //showShowcase();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(IS_FIRST_RUN_KEY, false);
                    editor.commit();

                    synchronized (SettingsActivity.this) {

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            mSettingsListView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
                        } else {
                            mSettingsListView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
                        }

                        globalLayoutListener = null;
                    }
                }
            });
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove global layout listener
        synchronized (SettingsActivity.this) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                mSettingsListView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
            } else {
                mSettingsListView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
            }
            globalLayoutListener = null;
        }

        if (showcaseManager != null) {
            showcaseManager.stop();
        }
    }

    @Override
    public void showShowcase() {

        //TODO: Move string constants to xml files

        // Create a relative location for the next button
        final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        final int margin = ((Number) (getResources().getDisplayMetrics().density * 24)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        showcaseManager = new ShowcaseManager();

        final ListView settingsList = mSettingsListView;

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                mSettingsListView.setSelection(0);

                // TODO: Last minute fix (Find a better way to call this once the scroll is complete) (i.e. dont use postDelayed)
                showcaseView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mSettingsListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                final View generalTab = settingsList.getChildAt(0);

                                final ViewTarget generalTabTarget = new ViewTarget(generalTab, 1.2f);

                                final int[] coords = new int[2];

                                generalTab.getLocationOnScreen(coords);

                                // Calculate position for the help text
                                final int textX = coords[0] + generalTab.getMeasuredWidth() + margin * 4;
                                final int textY = coords[1];

                                showcaseView.setShowcase(generalTabTarget, true);
                                showcaseView.setContentTitle("Generelt");
                                showcaseView.setContentText("Generelle indstillinger for Giraf Launcheren");
                                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                                showcaseView.setButtonPosition(lps);
                                showcaseView.setTextPostion(textX, textY);
                            }
                        }, 100);
                    }


                }, 100);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                mSettingsListView.setSelection(1);

                // TODO: Last minute fix (Find a better way to call this once the scroll is complete) (i.e. dont use postDelayed)
                mSettingsListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final View applicationManagementTab = settingsList.getChildAt(1);

                        final ViewTarget applicationManagementTarget = new ViewTarget(applicationManagementTab, 1.2f);

                        final int[] coords = new int[2];

                        applicationManagementTab.getLocationOnScreen(coords);

                        // Calculate position for the help text
                        final int textX = coords[0] + applicationManagementTab.getMeasuredWidth() + margin * 4;
                        final int textY = coords[1];

                        showcaseView.setShowcase(applicationManagementTarget, true);
                        showcaseView.setContentTitle("Applikations håndtering");
                        showcaseView.setContentText("Indstil hvilke applikationer der skal vises");
                        showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                        showcaseView.setButtonPosition(lps);
                        showcaseView.setTextPostion(textX, textY);
                    }
                }, 100);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                mSettingsListView.setSelection(2);

                // TODO: Last minute fix (Find a better way to call this once the scroll is complete) (i.e. dont use postDelayed)
                mSettingsListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        final View androidSettingsTab = settingsList.getChildAt(2);

                        final ViewTarget androidSettingsTarget = new ViewTarget(androidSettingsTab, 1.2f);

                        final int[] coords = new int[2];

                        androidSettingsTab.getLocationOnScreen(coords);

                        // Calculate position for the help text
                        final int textX = coords[0] + androidSettingsTab.getMeasuredWidth() + margin * 4;
                        final int textY = coords[1];

                        showcaseView.setShowcase(androidSettingsTarget, true);
                        showcaseView.setContentTitle("Tablet indstillinger");
                        showcaseView.setContentText("Generelle indstillinger for din tablet");
                        showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                        showcaseView.setButtonPosition(lps);
                        showcaseView.setTextPostion(textX, textY);
                    }
                }, 100);
            }
        });

        final android.support.v4.app.Fragment currentSupportSettingsContent = mSupportFragManager.findFragmentById(R.id.settingsContainer);

        if (currentSupportSettingsContent != null && currentSupportSettingsContent instanceof AppContainerFragment) {

            showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
                @Override
                public void configShowCaseView(final ShowcaseView showcaseView) {

                    // Find the currently active content fragment
                    final Fragment currentNormalSettingsContent = mFragManager.findFragmentById(R.id.settingsContainer);

                    final View noAppsMessageView = currentSupportSettingsContent.getView().findViewById(R.id.noAppsMessage);
                    final View appsViewPager = currentSupportSettingsContent.getView().findViewById(R.id.appsViewPager);

                    final Target noAppsMessageTarget = new ViewTarget(noAppsMessageView, 1.1f);
                    final Target appsViewPagerTarget = new ViewTarget(appsViewPager, 1.4f);

                    // Calculate position for the help text
                    final int noAppsMessageViewTextX = findViewById(R.id.settings_button).getRight() + margin * 2;
                    final int noAppsMessageViewTextY = findViewById(R.id.settings_button).getBottom() + margin;

                    final int appsViewPagerTextX = findViewById(R.id.settings_button).getRight() + margin * 2;
                    final int appsViewPagerTextY = findViewById(R.id.settings_button).getBottom() + margin;

                    if (currentSupportSettingsContent instanceof GirafFragment) {

                        showcaseView.setContentTitle("Giraf Applikationer");

                        if (noAppsMessageView.getVisibility() == View.VISIBLE) {
                            showcaseView.setShowcase(noAppsMessageTarget, true);
                            showcaseView.setContentText("Her vil Giraf applikationer kunne vælges til og fra når de bliver installeret");
                            showcaseView.setTextPostion(noAppsMessageViewTextX, noAppsMessageViewTextY);
                        } else {
                            showcaseView.setShowcase(appsViewPagerTarget, true);
                            showcaseView.setContentText("Her kan Giraf applikationer vælges til og fra");
                            showcaseView.setTextPostion(appsViewPagerTextX, appsViewPagerTextY);
                        }

                    } else if (currentSupportSettingsContent instanceof AndroidFragment) {

                        showcaseView.setContentTitle("Android Applikationer");

                        if (noAppsMessageView.getVisibility() == View.VISIBLE) {
                            showcaseView.setShowcase(noAppsMessageTarget, true);
                            showcaseView.setContentText("Her vil Android applikationer kunne vælges til og fra når de bliver installeret");
                            showcaseView.setTextPostion(noAppsMessageViewTextX, noAppsMessageViewTextY);
                        } else {
                            showcaseView.setShowcase(appsViewPagerTarget, true);
                            showcaseView.setContentText("Her kan Android applikationer vælges til og fra");
                            showcaseView.setTextPostion(appsViewPagerTextX, appsViewPagerTextY);
                        }
                    }

                    showcaseView.setButtonPosition(lps);
                    showcaseView.setStyle(R.style.GirafLastCustomShowcaseTheme);
                }

            });
        }

        showcaseManager.setOnDoneListener(new ShowcaseManager.OnDoneListener() {
            @Override
            public void onDone(ShowcaseView showcaseView) {
                showcaseManager = null;
                isFirstRun = false;
            }
        });

        showcaseManager.start(this);
    }

    @Override
    public synchronized void hideShowcase() {

        if (showcaseManager != null) {
            showcaseManager.stop();
            showcaseManager = null;
        }
    }

    @Override
    public synchronized void toggleShowcase() {

        if (showcaseManager != null) {
            hideShowcase();
        } else {
            showShowcase();
        }
    }
}