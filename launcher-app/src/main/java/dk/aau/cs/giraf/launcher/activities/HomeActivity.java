package dk.aau.cs.giraf.launcher.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Application;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.gui.GWidgetUpdater;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafConfirmDialog;
import dk.aau.cs.giraf.gui.GirafNotifyDialog;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;
import dk.aau.cs.giraf.gui.GirafProfileSelectorDialog;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.launcher.settings.SettingsActivity;
import dk.aau.cs.giraf.launcher.settings.components.ApplicationGridResizer;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.showcaseview.ShowcaseManager;
import dk.aau.cs.giraf.showcaseview.ShowcaseView;
import dk.aau.cs.giraf.showcaseview.targets.ViewTarget;
import dk.aau.cs.giraf.utilities.NetworkUtilities;

/**
 * The primary activity of Launcher. Allows the user to start other GIRAF apps and access the settings
 * activity. It requires a user id in the parent intent.
 */
public class HomeActivity extends GirafActivity implements AppsFragmentInterface, GirafNotifyDialog.Notification, GirafConfirmDialog.Confirmation, GirafProfileSelectorDialog.OnSingleProfileSelectedListener, ShowcaseManager.ShowcaseCapable {

    private static final String IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY_HOME_ACTIVITY";
    private static final int CHANGE_USER_SELECTOR_DIALOG = 100;
    private Profile mCurrentUser;
    private Profile mLoggedInGuardian;

    private LoadHomeActivityApplicationTask loadHomeActivityApplicationTask;

    private ArrayList<AppInfo> mCurrentLoadedApps;

    private GWidgetUpdater widgetUpdater;

    // Used to implement help functionality (ShowcaseView)
    private ShowcaseManager showcaseManager;
    private boolean isFirstRun;

    private Handler h = new Handler();
    private int delay = 5000;
    private boolean offlineMode;
    /**
     * Used in onResume and onPause for handling showcaseview for first run
     */
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    private ViewPager mAppViewPager;
    private ScrollView sidebarScrollView;

    private Timer mAppsUpdater;

    private final int METHOD_ID_LOGOUT = 1;

    @Override
    public Profile getCurrentUser() {
        return this.mCurrentUser;
    }

    @Override
    public Profile getLoggedInGuardian() {
        return this.mLoggedInGuardian;
    }

    /**
     * Sets up the activity. Specifically view variables are instantiated, the login button listener
     * is set, and the instruction animation is set up.
     *
     * @param savedInstanceState Information from the last launch of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        //Offline mode stuff
        offlineMode = offlineMode();
        Constants.offlineNotify = GirafNotifyDialog.newInstance(
                getString(R.string.dialog_offline_notify_title),
                getString(R.string.dialog_offline_notify_message),
                Constants.METHOD_ID_OFFLINE_NOTIFY);

        Helper mHelper = LauncherUtility.getOasisHelper(this);

        mCurrentUser = mHelper.profilesHelper.getById(getIntent().getExtras().getLong(Constants.CHILD_ID, -1));
        mLoggedInGuardian = mHelper.profilesHelper.getById(getIntent().getExtras().getLong(Constants.GUARDIAN_ID));

        if (mCurrentUser == null) {
            mCurrentUser = mLoggedInGuardian;
        }

        // Fetch references to view objects
        sidebarScrollView = (ScrollView) this.findViewById(R.id.sidebar_scrollview);
        mAppViewPager = (ViewPager) this.findViewById(R.id.appsViewPager);

        loadWidgets();

        // Show warning if DEBUG_MODE is true
        if (LauncherUtility.isDebugging()) {
            LauncherUtility.showDebugInformation(this);
        }

        // Get the row and column size for the grids in the AppViewPager
        final int rowsSize = ApplicationGridResizer.getGridRowSize(this, mCurrentUser);
        final int columnsSize = ApplicationGridResizer.getGridColumnSize(this, mCurrentUser);

        mAppViewPager.setAdapter(new AppsFragmentAdapter(getSupportFragmentManager(), mCurrentLoadedApps, rowsSize, columnsSize));

        final GirafButton helpGirafButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_help));
        helpGirafButton.setId(R.id.help_button);
        helpGirafButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleShowcase();
            }
        });

        offlineModeFeedback();
        
        //Setup a reoccuring check of network status aka. offline mode
        // this also acts when changing from or to offline mode
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean offlineModeNow = offlineMode();
                if (offlineMode != offlineModeNow) {
                    offlineMode = offlineModeNow;
                    reloadApplications();
                    offlineModeFeedback();
                }
                h.postDelayed(this, delay);
            }
        }, delay);

        // Start logging this activity
        EasyTracker.getInstance(this).activityStart(this);
    }

    /**
     * Starts a timer that looks for updates in the set of available applications every 5 seconds.
     */
    private void startObservingApps() {
        mAppsUpdater = new Timer();
        AppsObserver timerTask = new AppsObserver();
        mAppsUpdater.scheduleAtFixedRate(timerTask, 5000, 5000);

        Log.d(Constants.ERROR_TAG, "Applications are being observed.");
    }

    /**
     * Stops Google Analytics logging.
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Stop logging this activity
        EasyTracker.getInstance(this).activityStop(this);
    }

    /**
     *  This method is called whenever the launcher home screen is returned to
     *  for example when returning from an app or the setitng page
     */
    @Override
    protected void onResume() {
        offlineMode = !NetworkUtilities.isNetworkAvailable(this);
        offlineModeFeedback();
        super.onResume();

        // Reload applications (Some applications might have been (un)installed)
        reloadApplications();

        if (widgetUpdater != null) {
            widgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_START);
        }

        // Check if this is the first run of the app
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.isFirstRun = prefs.getBoolean(IS_FIRST_RUN_KEY, true);

        // If it is the first run display ShowcaseView
        if (isFirstRun) {
            this.findViewById(android.R.id.content).getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    showShowcase();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(IS_FIRST_RUN_KEY, false);
                    editor.commit();

                    synchronized (HomeActivity.this) {

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            HomeActivity.this.findViewById(android.R.id.content).getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
                        } else {
                            HomeActivity.this.findViewById(android.R.id.content).getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
                        }

                        globalLayoutListener = null;
                    }
                }
            });
        }
    }

    /**
     * Stops the timer looking for updates in the set of available apps.
     *
     * @see HomeActivity#startObservingApps()
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (mAppsUpdater != null) {
            mAppsUpdater.cancel();
            Log.d(Constants.ERROR_TAG, "Applications are no longer observed.");
        }

        if (widgetUpdater != null) {
            widgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_STOP);
        }

        // Cancel any loading task still running
        if (loadHomeActivityApplicationTask != null) {
            loadHomeActivityApplicationTask.cancel(true);
        }

        synchronized (HomeActivity.this) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                HomeActivity.this.findViewById(android.R.id.content).getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
            } else {
                HomeActivity.this.findViewById(android.R.id.content).getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
            }
            globalLayoutListener = null;
        }

        if (showcaseManager != null) {
            showcaseManager.stop();
        }

    }

    /**
     * Force loadApplications to redraw but setting mCurrentlyLoadedApps to null
     */
    private void reloadApplications() {
        mCurrentLoadedApps = null;
        loadApplications();
    }

    /**
     * Load the user's applications into the app container.
     */
    private void loadApplications() {
        loadHomeActivityApplicationTask = new LoadHomeActivityApplicationTask(this, mCurrentUser, mLoggedInGuardian, mAppViewPager, null, offlineMode);
        loadHomeActivityApplicationTask.execute();
    }

    /**
     * Loads the sidebar's widgets.
     *
     * @see dk.aau.cs.giraf.gui.GWidgetConnectivity
     * @see dk.aau.cs.giraf.gui.GWidgetProfileSelection
     * @see dk.aau.cs.giraf.gui.GirafButton
     */
    private void loadWidgets() {

        // Fetch references to buttons
        final GirafButton logoutButton = (GirafButton) findViewById(R.id.logout_button);
        final GirafButton settingsButton = (GirafButton) findViewById(R.id.settings_button);
        final GirafButton changeUserButton = (GirafButton) findViewById(R.id.change_user_button);
        final GirafButton helpButton = (GirafButton) findViewById(R.id.help_button);

        final GirafPictogramItemView profilePictureView = (GirafPictogramItemView) findViewById(R.id.profile_widget);

        //Set up widget updater, which updates the widget's view regularly, according to its status.
        widgetUpdater = new GWidgetUpdater();

        // Check if the user was a guardian
        if (mCurrentUser.getRole().getValue() < Profile.Roles.CHILD.getValue()) {
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    intent.putExtra(Constants.GUARDIAN_ID, mLoggedInGuardian.getId());
                    if (mCurrentUser.getRole() == Profile.Roles.GUARDIAN)
                        intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);
                    else
                        intent.putExtra(Constants.CHILD_ID, mCurrentUser.getId());

                    startActivity(intent);
                }
            });

            // Set the change user button to open the change user dialog
            changeUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GirafProfileSelectorDialog changeUser = GirafProfileSelectorDialog.newInstance(HomeActivity.this, mCurrentUser.getId(), false, false, "Vælg den borger du vil skifte til.", CHANGE_USER_SELECTOR_DIALOG);
                    changeUser.show(getSupportFragmentManager(), "" + CHANGE_USER_SELECTOR_DIALOG);
                }
            });

        } else { // The uer had citizen permissions
            settingsButton.setVisibility(View.INVISIBLE);
            changeUserButton.setVisibility(View.INVISIBLE);

        }

        // Set the profile picture
        profilePictureView.setImageModel(mCurrentUser, this.getResources().getDrawable(R.drawable.no_profile_pic));
        profilePictureView.setTitle(mCurrentUser.getName());

        // Set the logout button to show the logout dialog
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GirafConfirmDialog girafConfirmDialog = GirafConfirmDialog.newInstance("Log ud", "Vil du logge ud?", METHOD_ID_LOGOUT, "Log ud", R.drawable.icon_logout, "Fortryd", R.drawable.icon_cancel);
                girafConfirmDialog.show(getSupportFragmentManager(), "logout_dialog");
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShowcase();
            }
        });
    }

    /**
     * Method used for displaying offline mode feedback
     */
    protected void offlineModeFeedback(){
        if (offlineMode){
            findViewById(R.id.offlineModeText).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.offlineModeText).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Method for checking if there is connection to the internet
     * @return true if in offline mode i.e. no connection to the outside world
     */
    protected boolean offlineMode(){
        return !NetworkUtilities.isNetworkAvailable(this);
    }

    @Override
    public void confirmDialog(int methodID) {
        switch (methodID) {
            case METHOD_ID_LOGOUT: {
                startActivity(LauncherUtility.logOutIntent(HomeActivity.this));
                Toast.makeText(this, "Logget ud", Toast.LENGTH_LONG).show();
                finish();
                break;
            }
        }
    }

    /**
     * Gets called by the GirafNotify Dialog with the ID of the dialog
     * @param i
     */
    @Override
    public void noticeDialog(int i) {
        switch (i){
            case Constants.METHOD_ID_OFFLINE_NOTIFY:
                Constants.offlineNotify.dismiss();
                break;
        }
    }

    @Override
    public void onProfileSelected(final int i, final Profile profile) {

        if (i == CHANGE_USER_SELECTOR_DIALOG) {

            // Update the profile
            mCurrentUser = profile;

            // Reload the widgets in the left side of the screen
            loadWidgets();

            // Reload the application container, as a new user has been selected.
            reloadApplications();
        }

    }

    @Override
    public void showShowcase() {

        // Create a relative location for the next button
        final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        final int margin = ((Number) (getResources().getDisplayMetrics().density * 24)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        final GirafButton settingsButton = (GirafButton) findViewById(R.id.settings_button);
        final GirafButton changeUserButton = (GirafButton) findViewById(R.id.change_user_button);

        showcaseManager = new ShowcaseManager();

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                // TODO: Last minute fix (Find a better way to call this once the layout is complete) (i.e. dont use postDelayed)
                showcaseView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        final GirafPictogramItemView profilePictureView = (GirafPictogramItemView) findViewById(R.id.profile_widget);

                        final ViewTarget settingsButtonTarget = new ViewTarget(profilePictureView, 1.1f);

                        final int[] coords = new int[2];

                        profilePictureView.getLocationOnScreen(coords);

                        // Calculate position for the help text
                        final int textX = coords[0] + margin * 8;
                        final int textY = coords[1] + profilePictureView.getMeasuredHeight() / 2;

                        showcaseView.setShowcase(settingsButtonTarget, true);
                        showcaseView.setContentTitle("Nuværende bruger");
                        showcaseView.setContentText("Her kan du se den nuværende bruger af enheden");
                        showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                        showcaseView.setButtonPosition(lps);
                        showcaseView.setTextPostion(textX, textY);

                    }
                }, 100);
            }
        });

        if (changeUserButton.getVisibility() == View.VISIBLE) {
            showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
                @Override
                public void configShowCaseView(final ShowcaseView showcaseView) {

                    sidebarScrollView.scrollTo(0, changeUserButton.getTop());

                    final ViewTarget changeUserButtonTarget = new ViewTarget(changeUserButton, 1.4f);

                    final int[] coords = new int[2];

                    changeUserButton.getLocationOnScreen(coords);

                    // Calculate position for the help text
                    final int textX = coords[0] + margin * 6;
                    final int textY = coords[1];

                    showcaseView.setShowcase(changeUserButtonTarget, true);
                    showcaseView.setContentTitle("Skift til borger");
                    showcaseView.setContentText("Tryk her før du giver enheden videre til en borger");
                    showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                    showcaseView.setButtonPosition(lps);
                    showcaseView.setTextPostion(textX, textY);
                }
            });
        }

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                final GirafButton logoutButton = (GirafButton) findViewById(R.id.logout_button);

                sidebarScrollView.scrollTo(0, logoutButton.getBottom());

                final ViewTarget logoutButtonTarget = new ViewTarget(logoutButton, 1.4f);

                final int[] coords = new int[2];

                logoutButton.getLocationOnScreen(coords);

                // Calculate position for the help text
                final int textX = coords[0] + margin * 6;
                final int textY = coords[1];

                showcaseView.setShowcase(logoutButtonTarget, true);
                showcaseView.setContentTitle("Log ud");
                showcaseView.setContentText("Her kan du logge ud");

                if (changeUserButton.getVisibility() != View.VISIBLE) {
                    showcaseView.setStyle(R.style.GirafLastCustomShowcaseTheme);
                } else {
                    showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                }

                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        if (settingsButton.getVisibility() == View.VISIBLE) {
            showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
                @Override
                public void configShowCaseView(final ShowcaseView showcaseView) {

                    sidebarScrollView.scrollTo(0, settingsButton.getBottom());

                    final ViewTarget settingsButtonTarget = new ViewTarget(settingsButton, 1.4f);

                    final int[] coords = new int[2];

                    settingsButton.getLocationOnScreen(coords);

                    // Calculate position for the help text
                    final int textX = coords[0] + margin * 6;
                    final int textY = coords[1];

                    showcaseView.setShowcase(settingsButtonTarget, true);
                    showcaseView.setContentTitle("Indstillinger");
                    showcaseView.setContentText("Her kan du indstille hvor mange og hvilke apps der skal vises mm.");
                    showcaseView.setStyle(R.style.GirafLastCustomShowcaseTheme);
                    showcaseView.setButtonPosition(lps);
                    showcaseView.setTextPostion(textX, textY);
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

    /**
     * Task for observing if the set of available apps has changed.
     * Is only instantiated after apps have been loaded the first time.
     *
     * @see HomeActivity#loadApplications()
     */
    private class AppsObserver extends TimerTask {

        /**
         * The main method of the apps observer.
         * Retrieves the apps that should be displayed and compares them with the ones that are currently being displayed.
         * Runs loadApplications() if there are differences.
         */
        @Override
        public void run() {
            final List<Application> girafAppsList = ApplicationControlUtility.getAvailableGirafAppsForUser(HomeActivity.this, mCurrentUser); // For home_activity activity
            final SharedPreferences prefs = LauncherUtility.getSharedPreferencesForCurrentUser(HomeActivity.this, mCurrentUser);
            final Set<String> androidAppsPackagenames = prefs.getStringSet(getString(R.string.selected_android_apps_key), new HashSet<String>());
            final List<Application> androidAppsList = ApplicationControlUtility.convertPackageNamesToApplications(HomeActivity.this, androidAppsPackagenames);
            girafAppsList.addAll(androidAppsList);
            if (AppInfo.isAppListsDifferent(mCurrentLoadedApps, girafAppsList)) {
                // run this on UI thread since UI might need to get updated
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadApplications();
                    }
                });
            }
            Log.d(Constants.ERROR_TAG, "Applications checked");
        }

    }

    /**
     * This class carries out all the work of populating the appView with clickable applications.
     * It inherits from LoadApplicationTask, which does most of the work.
     * However, since there are some special things that need to be handled in the case of all applications,
     * we must inherit the class, override it's methods and do what we need to do in addition to the superclass
     */
    private class LoadHomeActivityApplicationTask extends LoadApplicationTask {

        /**
         * The contructor of the class
         *
         * @param context         The context of the current activity
         * @param currentUser     The current user (if the current user is a guardian, this is set to null)
         * @param guardian        The guardian of the current user (or just the current user, if the user is a guardian)
         * @param appsViewPager   The layout to be populated with AppImageViews
         * @param onClickListener the onClickListener that each created app should have. In this case we feed it the global variable listener
         * @param offlineMode     Indicate if the launcher is in offline mode
         */
        public LoadHomeActivityApplicationTask(Context context, Profile currentUser, Profile guardian, ViewPager appsViewPager, View.OnClickListener onClickListener, boolean offlineMode) {
            super(context, currentUser, guardian, appsViewPager, onClickListener, offlineMode, true);
        }

        /**
         * We override onPreExecute to cancel the AppObserver if it is running
         */
        @Override
        protected void onPreExecute() {
            if (mAppsUpdater != null)
                mAppsUpdater.cancel();

            super.onPreExecute();
        }

        /**
         * This method needs to be overridden since we need to inform the superclass of exactly which apps should be generated.
         * In this case it is both Giraf and Android applications.
         *
         * @param applications the applications that the task should generate AppImageViews for
         * @return The Hashmap of AppInfos that describe the added applications.
         */
        @Override
        protected ArrayList<AppInfo> doInBackground(Application... applications) {

            List<Application> girafAppsList = ApplicationControlUtility.getAvailableGirafAppsForUser(context, currentUser); // For home_activity
            SharedPreferences prefs = LauncherUtility.getSharedPreferencesForCurrentUser(context, currentUser);
            Set<String> androidAppsPackagenames = prefs.getStringSet(getString(R.string.selected_android_apps_key), new HashSet<String>());
            List<Application> androidAppsList = ApplicationControlUtility.convertPackageNamesToApplications(context, androidAppsPackagenames);
            girafAppsList.addAll(androidAppsList);

            applications = girafAppsList.toArray(applications);

            return super.doInBackground(applications);
        }

        /**
         * Once we have loaded applications, we start observing for new apps
         */
        @Override
        protected void onPostExecute(final ArrayList<AppInfo> appInfos) {
            super.onPostExecute(appInfos);
            mCurrentLoadedApps = appInfos;

            CirclePageIndicator titleIndicator = (CirclePageIndicator) findViewById(R.id.pageIndicator);
            titleIndicator.setViewPager(mAppViewPager);

            startObservingApps();
        }
    }
}
