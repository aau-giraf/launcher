package dk.aau.cs.giraf.launcher.activities;

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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafProfileSelectorDialog;
import dk.aau.cs.giraf.gui.GirafUserItemView;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.*;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.*;
import dk.aau.cs.giraf.launcher.widgets.SeekBarWithNumericScale;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.User;
import dk.aau.cs.giraf.models.core.authentication.Role;
import dk.aau.cs.giraf.showcaseview.ShowcaseManager;
import dk.aau.cs.giraf.showcaseview.ShowcaseView;
import dk.aau.cs.giraf.showcaseview.targets.Target;
import dk.aau.cs.giraf.showcaseview.targets.ViewTarget;
import dk.aau.cs.giraf.utilities.GrayScaleHelper;
import dk.aau.cs.giraf.utilities.IntentConstants;

import java.util.ArrayList;

/**
 * Activity responsible for handling Launcher settings_activity and starting
 * other setting-related activities.
 */
public class SettingsActivity extends GirafActivity
    implements SettingsListFragment.SettingsListFragmentListener,
    GirafProfileSelectorDialog.OnSingleProfileSelectedListener,
    AppsFragmentInterface, ShowcaseManager.ShowcaseCapable
{

    private RequestQueue queue;
    private static final String IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY_SETTINGS_ACTIVITY";

    private static final int CHANGE_USER_SELECTOR_DIALOG = 100;

    /**
     * The variables mostly used inside the class.
     */
    private FragmentManager fragmentManager;
    private android.support.v4.app.FragmentManager supportFragmentManager;

    private User currentUser;


    private ListView settingsListView;
    private SettingsListAdapter settingsListAdapter;

    // Used to implement help functionality (ShowcaseView)
    private ShowcaseManager showcaseManager;
    private boolean isFirstRun;

    /**
     * Used in onResume and onPause for handling showcaseview for first run.
     */
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    /**
     * Global variable containing giraf applications with settings.
     * ALL apps with settings are added to this list, which is
     * later filtered to remove applications that are
     * unavailable on the device.
     */
    private ArrayList<SettingsListItem> appList;

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
        queue = RequestQueueHandler.getInstance(this.getApplicationContext()).getRequestQueue();
        currentUser = (User) getIntent().getExtras().getSerializable(IntentConstants.CURRENT_USER);
        GrayScaleHelper.setGrayScaleForActivityByUser(this,currentUser);
        settingsListView = (ListView) findViewById(R.id.settingsListView);

        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsListItem item = (SettingsListItem) parent.getAdapter().getItem(position);

                // If the item contains a fragment, set it as active
                if (item instanceof FragmentSettingsListItem) {

                    FragmentSettingsListItem fragmentItem = ((FragmentSettingsListItem) item);

                    // Notify class implementing the callback interface that a new fragment has been selected
                    if (fragmentItem.getFragment() == null) {
                        setActiveFragment(fragmentItem.getSupportFragment());
                    } else {
                        setActiveFragment(fragmentItem.getFragment());
                    }

                    // Update the adapter to reflect the selection
                    settingsListAdapter.setSelected(position);
                } else if (item instanceof IntentSettingsListItem) {
                    // Otherwise it must be an intent
                    try {
                        // Start a new activity with the intent
                        startActivity(((IntentSettingsListItem) item).getIntent());
                    } catch (ActivityNotFoundException e) {
                        // Handle exception if the intended activity can not be started.
                        Toast.makeText(parent.getContext(),
                            R.string.settings_activity_not_found_msg, Toast.LENGTH_SHORT).show();
                    } finally {
                        // Notify adapter to redraw views since we want to reset the visual style
                        // of the selected since its state should not be preserved (= reset selected item
                        // to the item selected when starting the Intent.
                        settingsListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        final GirafUserItemView mProfileButton =
            (GirafUserItemView) findViewById(R.id.profile_widget_settings);

        // Notify about the current user
        setCurrentUser(currentUser);

        // Instantiates a new adapter to render the items in the ListView with a list of installed (available) apps
        settingsListAdapter = new SettingsListAdapter(this, settingsListView, this.getInstalledSettingsApps());
        // Set the new adapter in the ListView
        settingsListView.setAdapter(settingsListAdapter);

        settingsListAdapter.setSelected(0);

        //Load the correct profile picture for the choosen profile
        mProfileButton.setImageModel(currentUser, this.getResources().getDrawable(R.drawable.no_profile_pic));
        mProfileButton.setTitle(currentUser.getScreenName());

        final long childIdNew = getIntent().getExtras().getLong(Constants.CHILD_ID);
        final long guardianId = getIntent().getExtras().getLong(Constants.GUARDIAN_ID);

        // Change the title of the action bar to include the name of the current user
        if (currentUser != null) {
            this.setActionBarTitle(getString(R.string.settingsFor) + currentUser.getScreenName());
        }

        final GirafButton changeUserButton = new GirafButton(this, this.getResources()
            .getDrawable(R.drawable.icon_change_user));
        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GirafProfileSelectorDialog changeUser = GirafProfileSelectorDialog.newInstance(SettingsActivity.this,
                    currentUser, false, false, getString(R.string.settings_choose_citizen),
                    CHANGE_USER_SELECTOR_DIALOG);
                changeUser.show(getSupportFragmentManager(), "" + CHANGE_USER_SELECTOR_DIALOG);
            }

        });

        addGirafButtonToActionBar(changeUserButton, LEFT);

        final GirafButton helpGirafButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_help));
        helpGirafButton.setId(R.id.help_button);
        helpGirafButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShowcase();
            }
        });

        addGirafButtonToActionBar(helpGirafButton, GirafActivity.RIGHT);

        // Used to handle fragment changes within the containing View
        fragmentManager = this.getFragmentManager();
        supportFragmentManager = this.getSupportFragmentManager();
        Fragment settingsFragment = fragmentManager.findFragmentById(R.id.settingsContainer);

        // Check if the fragment already exists
        if (settingsFragment == null) {
            // Select the first entry in the list of applications in settings_activity
            // The first entry is always the general settings
            FragmentSettingsListItem item = (FragmentSettingsListItem) getInstalledSettingsApps().get(0);

            // Update active fragment with the first entry
            if (item.getFragment() != null) {
                // Load the fragment just selected into view
                fragmentManager.beginTransaction().add(R.id.settingsContainer, item.getFragment()).commit();
            } else {
                // Load the fragment just selected into view
                supportFragmentManager.beginTransaction().add(R.id.settingsContainer,
                    item.getSupportFragment()).commit();
            }
        }

        fragmentManager.executePendingTransactions();
        supportFragmentManager.executePendingTransactions();
        if (getIntent().getExtras().getBoolean(Constants.ENTER_ADD_APP_MANAGER_BOOL)) {
            settingsListAdapter.setSelected(getString(R.string.settings_tablist_applications));
            setActiveFragment(new AppManagementFragment());
        }
    }

    /**
     * This function gets the elements that should be added to the list of items in the left side
     * Firstly, it gets the settings for Launcher itself, along with the "Apps" menu,
     * where users select or deselect apps.
     * Finally, gets the currently installed apps that have settings to be shown in SettingsActivity.
     * Currently, these apps are only "Cars" (Stemmespillet) and "Zebra" (Sekvens). //ToDO rewrite
     *
     * @return an Array consisting of the SettingsListitems that should be put into the left scrollview.
     */
    @Override
    public ArrayList<SettingsListItem> getInstalledSettingsApps() {
        appList = new ArrayList<SettingsListItem>();

        // Launcher
        addApplicationByPackageName("dk.aau.cs.giraf.launcher",
            SettingsLauncher.newInstance(currentUser),
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
        return appList;
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
        } catch (PackageManager.NameNotFoundException e) {
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
            // TODO: This a quick fix to show the correct icon in the list.
            // TODO: This is however alright since the sidebar is not used for any other apps
            final Drawable appIcon = this.getResources()
                .getDrawable(R.drawable.icon_giraf_no_background); //pm.getApplicationIcon(appInfo);

            // Create the item
            final FragmentSettingsListItem item = new FragmentSettingsListItem(title, appIcon, fragment);

            // Add item to the list of applications
            appList.add(item);
        }
    }

    /**
     * Add another application to the list by Intent.
     *
     * @param title    Name of the application to add.
     * @param fragment Fragment with settings that should be started.
     * @param icon     Custom icon to add to list entry.
     */
    private void addApplicationByTitle(final String title,
                                       final android.support.v4.app.Fragment fragment, final Drawable icon)
    {
        // Create the new item
        final FragmentSettingsListItem item = new FragmentSettingsListItem(title, icon, fragment);

        // Add item to the list of applications
        appList.add(item);
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
        appList.add(item);
    }

    /**
     * This function replaces the currently activity fragment in the FragmentManager with a new one
     *
     * @param fragment the fragement that should now be displayed.
     */
    @Override
    public void setActiveFragment(final Fragment fragment) {

        final Fragment mActiveFragment = fragmentManager.findFragmentById(R.id.settingsContainer);

        // Only add new transaction if the user clicked a non-active fragment
        if (mActiveFragment == null || !mActiveFragment.equals(fragment)) {

            final android.support.v4.app.Fragment mActiveSupportFragment =
                supportFragmentManager.findFragmentById(R.id.settingsContainer);

            if (mActiveSupportFragment != null) {
                android.support.v4.app.FragmentTransaction ft = supportFragmentManager.beginTransaction();
                ft.remove(mActiveSupportFragment);
                ft.commit();
            }

            final FragmentTransaction ft = fragmentManager.beginTransaction();

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

        final android.support.v4.app.Fragment mActiveSupportFragment =
            supportFragmentManager.findFragmentById(R.id.settingsContainer);

        // Only add new transaction if the user clicked a non-active fragment
        if (mActiveSupportFragment == null || !mActiveSupportFragment.equals(fragment)) {
            final Fragment mActiveFragment = fragmentManager.findFragmentById(R.id.settingsContainer);

            if (mActiveFragment != null) {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.remove(mActiveFragment);
                ft.commit();
            }

            android.support.v4.app.FragmentTransaction ft = supportFragmentManager.beginTransaction();

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

        GetRequest<User> userGetRequest = new GetRequest<User>(User.class, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                intent.putExtra(IntentConstants.CURRENT_USER, currentUser);
                // Stop activity before restarting
                SettingsActivity.this.finish();

                // Start activity again to reload contents
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    LoginRequest loginRequest = new LoginRequest(currentUser, new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            GetRequest<User> userGetRequest = new GetRequest<User>(User.class, new Response.Listener<User>() {
                                @Override
                                public void onResponse(User response) {
                                    if (currentUser.isRole(Role.User)) {
                                        // A child profile has been selected, pass id
                                        intent.putExtra(Constants.CHILD_ID, currentUser.getUsername());
                                    } else { // We are a guardian, do not add a child
                                        intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);
                                    }
                                    // Stop activity before restarting
                                    SettingsActivity.this.finish();

                                    // Start activity again to reload contents
                                    startActivity(intent);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    LauncherUtility.logoutWithDialog(SettingsActivity.this);
                                }
                            });
                            queue.add(userGetRequest);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            LauncherUtility.logoutWithDialog(SettingsActivity.this);
                        }
                    });
                    queue.add(loginRequest);
                }
                else{
                    LauncherUtility.logoutWithDialog(SettingsActivity.this);
                }
            }
        });
        queue.add(userGetRequest);
    }

    /**
     * Sets the current profile
     *
     * @param profile The selected profile.
     */
    @Override
    public void setCurrentUser(User profile) {
        currentUser = profile;
    }

    /**
     * gets the current user.
     *
     * @return profile of the current user
     */

    public User getCurrentUser() {
        return currentUser;
    }


    @Override
    public void onProfileSelected(final int input, final User profile) {
        if (input == CHANGE_USER_SELECTOR_DIALOG) {

            // Notify that a profile selection has been made
            setCurrentUser(profile);

            // Reload activity to reflect different user settings
            reloadActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        currentUser = (User) getIntent().getExtras().getSerializable(IntentConstants.CURRENT_USER);
        GrayScaleHelper.setGrayScaleForActivityByUser(this,currentUser);
        // Check if this is the first run of the app
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.isFirstRun = prefs.getBoolean(IS_FIRST_RUN_KEY, true);

        // If it is the first run display ShowcaseView
        if (isFirstRun) {
            settingsListView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener =
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        showShowcase();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(IS_FIRST_RUN_KEY, false);
                        editor.commit();

                        synchronized (SettingsActivity.this) {

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                settingsListView.getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(globalLayoutListener);
                            } else {
                                settingsListView.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(globalLayoutListener);
                            }

                            globalLayoutListener = null;
                        }
                    }
                });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove global layout listener
        synchronized (SettingsActivity.this) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                settingsListView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
            } else {
                settingsListView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
            }
            globalLayoutListener = null;
        }

        if (showcaseManager != null) {
            showcaseManager.stop();
        }
    }

    @Override
    public void showShowcase() {

        // TODO: This code gets runtime error on smaller screens (eg. phones)

        // Create a relative location for the next button
        final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        final int margin = ((Number) (getResources().getDisplayMetrics().density * 24)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        showcaseManager = new ShowcaseManager();

        final ListView settingsList = settingsListView;

        final int originalPos = settingsListView.getSelectedItemPosition();

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {
                // TODO: Last minute fix (Find a better way to call this once the scroll is complete)
                // TODO: (i.e. dont use postDelayed)
                showcaseView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final View profile = findViewById(R.id.profile_widget_settings);
                        final ViewTarget profileTarget = new ViewTarget(profile, 1.2f);

                        final int[] coords = new int[2];

                        profile.getLocationOnScreen(coords);

                        // Calculate position for the help text
                        final int textX = coords[0] + profile.getMeasuredWidth() + margin * 4;
                        final int textY = coords[1];

                        showcaseView.setShowcase(profileTarget, true);
                        showcaseView.setContentTitle(getString(R.string.settings_current_profile));
                        showcaseView.setContentText(getString(R.string.settings_the_settings_for_profile));
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

                //settingsListView.setSelection(0);
                settingsListView.smoothScrollToPositionFromTop(0, 0, 120);

                // TODO: Last minute fix (Find a better way to call this once the scroll is complete)
                // TODO: (i.e. dont use postDelayed)
                showcaseView.postDelayed(new Runnable() {
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
                        showcaseView.setContentTitle(getString(R.string.settings_tablist_general));
                        showcaseView.setContentText(getString(R.string.settings_tablist_general_for_launcher));
                        showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                        showcaseView.setButtonPosition(lps);
                        showcaseView.setTextPostion(textX, textY);
                    }


                }, 200);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                settingsListView.smoothScrollToPositionFromTop(0, 0, 120);
                // TODO: Last minute fix (Find a better way to call this once the scroll is complete)
                // TODO: (i.e. dont use postDelayed)
                settingsListView.postDelayed(new Runnable() {
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
                        showcaseView.setContentTitle(getString(R.string.settings_app_management));
                        showcaseView.setContentText(getString(R.string.settings_set_which_apps_to_show));
                        showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                        showcaseView.setButtonPosition(lps);
                        showcaseView.setTextPostion(textX, textY);
                    }
                }, 200);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                settingsListView.smoothScrollToPositionFromTop(2, 0, 120);

                // TODO: Last minute fix (Find a better way to call this once the scroll is complete)
                // TODO: (i.e. dont use postDelayed)
                settingsListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //child is 1 because we have scrolled!
                        final View androidSettingsTab = settingsList.getChildAt(1);

                        final ViewTarget androidSettingsTarget = new ViewTarget(androidSettingsTab, 1.2f);

                        final int[] coords = new int[2];

                        androidSettingsTab.getLocationOnScreen(coords);

                        // Calculate position for the help text
                        final int textX = coords[0] + androidSettingsTab.getMeasuredWidth() + margin * 4;
                        final int textY = coords[1];

                        showcaseView.setShowcase(androidSettingsTarget, true);
                        showcaseView.setContentTitle(getString(R.string.settings_tablet));
                        showcaseView.setContentText(getString(R.string.settings_tablet_general));
                        showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                        showcaseView.setButtonPosition(lps);
                        showcaseView.setTextPostion(textX, textY);
                    }
                }, 200);
            }
        });

        final android.support.v4.app.Fragment currentSupportSettingsContent =
            supportFragmentManager.findFragmentById(R.id.settingsContainer);

        if (currentSupportSettingsContent != null && currentSupportSettingsContent instanceof AppContainerFragment) {

            showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
                @Override
                public void configShowCaseView(final ShowcaseView showcaseView) {

                    // Find the currently active content fragment
                    final Fragment currentNormalSettingsContent =
                        fragmentManager.findFragmentById(R.id.settingsContainer);

                    final View noAppsMessageView = currentSupportSettingsContent.getView()
                        .findViewById(R.id.noAppsMessage);
                    final View appsViewPager = currentSupportSettingsContent.getView()
                        .findViewById(R.id.appsViewPager);

                    final Target noAppsMessageTarget = new ViewTarget(noAppsMessageView, 1.1f);
                    final Target appsViewPagerTarget = new ViewTarget(appsViewPager, 1.4f);

                    // Calculate position for the help text
                    final int noAppsMessageViewTextX = findViewById(R.id.settings_button).getRight() + margin * 2;
                    final int noAppsMessageViewTextY = findViewById(R.id.settings_button).getBottom() + margin;

                    final int appsViewPagerTextX = findViewById(R.id.settings_button).getRight() + margin * 2;
                    final int appsViewPagerTextY = findViewById(R.id.settings_button).getBottom() + margin;

                    if (currentSupportSettingsContent instanceof GirafFragment) {

                        showcaseView.setContentTitle(getString(R.string.settings_giraf_apps));

                        if (noAppsMessageView.getVisibility() == View.VISIBLE) {
                            showcaseView.setShowcase(noAppsMessageTarget, true);
                            showcaseView.setContentText(getString(R.string.settings_giraf_here_long));
                            showcaseView.setTextPostion(noAppsMessageViewTextX, noAppsMessageViewTextY);
                        } else {
                            showcaseView.setShowcase(appsViewPagerTarget, true);
                            showcaseView.setContentText(getString(R.string.settings_giraf_here_short));
                            showcaseView.setTextPostion(appsViewPagerTextX, appsViewPagerTextY);
                        }

                    } else if (currentSupportSettingsContent instanceof AndroidFragment) {

                        showcaseView.setContentTitle(getString(R.string.settings_android_apps));

                        if (noAppsMessageView.getVisibility() == View.VISIBLE) {
                            showcaseView.setShowcase(noAppsMessageTarget, true);
                            showcaseView.setContentText(getString(R.string.settings_android_here_long));
                            showcaseView.setTextPostion(noAppsMessageViewTextX, noAppsMessageViewTextY);
                        } else {
                            showcaseView.setShowcase(appsViewPagerTarget, true);
                            showcaseView.setContentText(getString(R.string.settings_android_here_short));
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

                // Scroll back to the original position
                settingsListView.smoothScrollToPositionFromTop(originalPos, 0, 120);
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

    @Override
    public User getUser() {
        return currentUser;
    }

}