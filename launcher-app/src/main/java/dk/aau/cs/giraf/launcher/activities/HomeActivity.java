package dk.aau.cs.giraf.launcher.activities;

import android.content.*;
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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.analytics.tracking.android.EasyTracker;
import com.viewpagerindicator.CirclePageIndicator;

import dk.aau.cs.giraf.activity.GirafActivity;
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
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.User;
import dk.aau.cs.giraf.models.core.Application;
import dk.aau.cs.giraf.models.core.authentication.PermissionType;
import dk.aau.cs.giraf.showcaseview.ShowcaseManager;
import dk.aau.cs.giraf.showcaseview.ShowcaseView;
import dk.aau.cs.giraf.showcaseview.targets.ViewTarget;
import dk.aau.cs.giraf.utilities.NetworkUtilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The primary activity of Launcher. Allows the user to start other GIRAF apps and access the settings
 * activity. It requires a user id in the parent intent.
 */
public class HomeActivity extends GirafActivity implements AppsFragmentInterface,
    GirafNotifyDialog.Notification, GirafConfirmDialog.Confirmation,
    GirafProfileSelectorDialog.OnSingleProfileSelectedListener, ShowcaseManager.ShowcaseCapable
{
    private static final String IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY_HOME_ACTIVITY";
    private static final int CHANGE_USER_SELECTOR_DIALOG = 100;
    private long currentUserId;
    private long loggedInGuardianId;
    private RequestQueue queue;

    private LoadHomeActivityApplicationTask loadHomeActivityApplicationTask;
    private boolean appObserverReceiverRegistered = false;
    private ArrayList<AppInfo> currentLoadedApps;

    private GWidgetUpdater widgetUpdater;

    // Used to implement help functionality (ShowcaseView)
    private ShowcaseManager showcaseManager;
    private boolean isFirstRun;

    private Handler handler = new Handler();
    private int delay = 5000;
    private boolean offlineMode;
    /**
     * Used in onResume and onPause for handling showcaseview for first run.
     */
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    private ViewPager appViewPager;
    private ScrollView sidebarScrollView;

    private final int methodIdLogout = 1;

    @Override
    public long getCurrentUserId() { return this.currentUserId; }

    @Override
    public long getLoggedInGuardianId() {
        return this.loggedInGuardianId;
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
        queue = RequestQueueHandler.getInstance(this.getApplicationContext()).getRequestQueue();

        //Offline mode stuff
        offlineMode = offlineMode();
        Constants.offlineNotify = GirafNotifyDialog.newInstance(
            getString(R.string.dialog_offline_notify_title),
            getString(R.string.dialog_offline_notify_message),
            Constants.METHOD_ID_OFFLINE_NOTIFY);

        if (currentUserId == 0L) {
            currentUserId = loggedInGuardianId;
        }

        // Fetch references to view objects
        sidebarScrollView = (ScrollView) this.findViewById(R.id.sidebar_scrollview);
        appViewPager = (ViewPager) this.findViewById(R.id.appsViewPager);

        loadWidgets();

        // Show warning if DEBUG_MODE is true
        if (LauncherUtility.isDebugging()) {
            LauncherUtility.showDebugInformation(this);
        }

        GetRequest<User> userGetRequest = new GetRequest<User>(currentUserId, User.class, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                // Get the row and column size for the grids in the AppViewPager
                final int rowsSize = response.getSettings().getAppsGridSizeRows();
                final int columnsSize = response.getSettings().getAppsGridSizeColumns();

                appViewPager.setAdapter(new AppsFragmentAdapter(getSupportFragmentManager(),
                    currentLoadedApps, rowsSize, columnsSize));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(userGetRequest);

        final GirafButton helpGirafButton = new GirafButton(this,
            getResources().getDrawable(R.drawable.icon_help));
        helpGirafButton.setId(R.id.help_button);
        helpGirafButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShowcase();
            }
        });

        offlineModeFeedback();

        //Setup a reoccuring check of network status aka. offline mode
        // this also acts when changing from or to offline mode
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean offlineModeNow = offlineMode();
                if (offlineMode != offlineModeNow) {
                    offlineMode = offlineModeNow;
                    reloadApplications();
                    offlineModeFeedback();
                }
                handler.postDelayed(this, delay);
            }
        }, delay);

        // Start logging this activity
        EasyTracker.getInstance(this).activityStart(this);
    }

    /**
     * Registers a broadcastReceiver that are notified when an app is installed or removed.
     */
    private void startObservingApps() {
        //Makes sure this Receiver is only registered once
        if(!appObserverReceiverRegistered) {
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    appsChangedScan();
                }
            };
            //Sets up the filter to only trigger when these actions are received
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            registerReceiver(broadcastReceiver, intentFilter);
            appObserverReceiverRegistered = true;
            Log.d(Constants.ERROR_TAG, "Applications are being observed.");
        }
    }

    /**
     * Finds out if the current loaded apps and the app list is different and updates the UI.
     */
    private void appsChangedScan() {
        
        final List<Application> girafAppsList = ApplicationControlUtility.getAvailableGirafAppsForUser(
            HomeActivity.this, currentUserId); // For home_activity activity
        final SharedPreferences prefs = LauncherUtility.getSharedPreferencesForCurrentUser(
            HomeActivity.this, currentUserId);
        final Set<String> androidAppsPackagenames = prefs.getStringSet(
            getString(R.string.selected_android_apps_key), new HashSet<String>());
        final List<Application> androidAppsList = ApplicationControlUtility.convertPackageNamesToApplications(
            HomeActivity.this, androidAppsPackagenames);
        girafAppsList.addAll(androidAppsList);
        if (AppInfo.isAppListsDifferent(currentLoadedApps, girafAppsList)) {
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
     * This method is called whenever the launcher home screen is returned to.
     * For example when returning from an app or the settings page.
     */
    @Override
    protected void onResume() {
        offlineMode = offlineMode();
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
            this.findViewById(android.R.id.content).getViewTreeObserver()
                .addOnGlobalLayoutListener(globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        showShowcase();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(IS_FIRST_RUN_KEY, false);
                        editor.commit();

                        synchronized (HomeActivity.this) {
                            HomeActivity.this.findViewById(android.R.id.content)
                                .getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);

                            globalLayoutListener = null;
                        }
                    }
                });
        }
    }

    /**
     * Stops the timer looking for updates in the set of available apps.
     * ToDo Find out if this code is still needed
     * @see HomeActivity#startObservingApps()
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (widgetUpdater != null) {
            widgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_STOP);
        }

        // Cancel any loading task still running
        if (loadHomeActivityApplicationTask != null) {
            loadHomeActivityApplicationTask.cancel(true);
        }

        synchronized (HomeActivity.this) {
            HomeActivity.this.findViewById(android.R.id.content)
                .getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
            globalLayoutListener = null;
        }

        if (showcaseManager != null) {
            showcaseManager.stop();
        }

    }

    /**
     * Force loadApplications to redraw but setting mCurrentlyLoadedApps to null.
     */
    private void reloadApplications() {
        currentLoadedApps = null;
        loadApplications();
    }

    /**
     * Load the user's applications into the app container.
     */
    private void loadApplications() {
        loadHomeActivityApplicationTask = new LoadHomeActivityApplicationTask(this, currentUserId,
            loggedInGuardianId, appViewPager, null, offlineMode);
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

        // Check if the user is a guardian
        if (currentUserId.hasPermission(PermissionType.Guardian) || currentUserId.hasPermission(PermissionType.SuperUser)){
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    intent.putExtra(Constants.GUARDIAN_ID, loggedInGuardianId.getId());
                    intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);
                    startActivity(intent);
                }
            });


            // Set the change user button to open the change user dialog
            changeUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GirafProfileSelectorDialog changeUser = GirafProfileSelectorDialog.newInstance(HomeActivity.this,
                        currentUserId.getId(), false, false, getString(R.string.home_activity_change_to_citizen_msg),
                        CHANGE_USER_SELECTOR_DIALOG);
                    changeUser.show(getSupportFragmentManager(), "" + CHANGE_USER_SELECTOR_DIALOG);
                }
            });

        } else { // The uer had citizen permissions
            settingsButton.setVisibility(View.INVISIBLE);
            changeUserButton.setVisibility(View.INVISIBLE);

        }

        // Set the profile picture

        profilePictureView.setImageModel(currentUserId, this.getResources().getDrawable(R.drawable.no_profile_pic));
        profilePictureView.setTitle(currentUserId.getScreenName());

        // Set the logout button to show the logout dialog
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GirafConfirmDialog girafConfirmDialog = GirafConfirmDialog.newInstance(getString(R.string.logout_msg),
                    getString(R.string.home_activity_logout), methodIdLogout, getString(R.string.logout_msg),
                    R.drawable.icon_logout, getString(R.string.cancel_msg), R.drawable.icon_cancel);
                girafConfirmDialog.show(getSupportFragmentManager(), "logout_dialog");
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShowcase();
            }
        });
    }

    /**
     * Method used for displaying offline mode feedback.
     */
    protected void offlineModeFeedback() {
        if (offlineMode) {
            findViewById(R.id.offlineModeText).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.offlineModeText).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Method for checking if there is connection to the internet
     *
     * @return true if in offline mode i.e. no connection to the outside world
     */
    protected boolean offlineMode() {
        return !NetworkUtilities.isNetworkAvailable(this);
    }

    @Override
    public void confirmDialog(int methodId) {
        if(methodId == methodIdLogout) {
            startActivity(LauncherUtility.logOutIntent(HomeActivity.this));
            Toast.makeText(this, getString(R.string.home_activity_logged_out), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Gets called by the GirafNotify Dialog with the ID of the dialog.
     *
     * @param id the ID.
     */
    @Override
    public void noticeDialog(int id) {
        if(id == Constants.METHOD_ID_OFFLINE_NOTIFY) {
            Constants.offlineNotify.dismiss();
        }
    }

    @Override
    public void onProfileSelected(final int input, final User profile) {

        if (input == CHANGE_USER_SELECTOR_DIALOG) {
            // Update the profile
            currentUserId = profile;

            // Reload the widgets in the left side of the screen
            loadWidgets();

            // Reload the application container, as a new user has been selected.
            reloadApplications();
        }
    }

    @Override
    public void showShowcase() {
        // Create a relative location for the next button
        final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

                // TODO: Last minute fix (Find a better way to call this once the layout is complete)
                // (i.e. dont use postDelayed)
                showcaseView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        final GirafPictogramItemView profilePictureView = (GirafPictogramItemView)
                            findViewById(R.id.profile_widget);

                        final ViewTarget settingsButtonTarget = new ViewTarget(profilePictureView, 1.1f);

                        final int[] coords = new int[2];

                        profilePictureView.getLocationOnScreen(coords);

                        // Calculate position for the help text
                        final int textX = coords[0] + margin * 8;
                        final int textY = coords[1] + profilePictureView.getMeasuredHeight() / 2;

                        showcaseView.setShowcase(settingsButtonTarget, true);
                        showcaseView.setContentTitle(getString(R.string.home_activity_current_user_title));
                        showcaseView.setContentText(getString(R.string.home_activity_current_user_text));
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
                    showcaseView.setContentTitle(getString(R.string.home_activity_change_to_citizen));
                    showcaseView.setContentText(getString(R.string.home_activity_click_here_before));
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
                showcaseView.setContentTitle(getString(R.string.home_activity_logout));
                showcaseView.setContentText(getString(R.string.home_activity_here_you_can_logout));

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
                    showcaseView.setContentTitle(getString(R.string.settings));
                    showcaseView.setContentText(getString(R.string.home_activity_here_you_can_setup));
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
         * @param onClickListener the onClickListener that each created app should have.
         *                        In this case we feed it the global variable listener
         * @param offlineMode     Indicate if the launcher is in offline mode
         */
        public LoadHomeActivityApplicationTask(Context context, User currentUser, User guardian, ViewPager
            appsViewPager, View.OnClickListener onClickListener, boolean offlineMode)
        {
            super(context, currentUser, guardian, appsViewPager, onClickListener, offlineMode, true);
        }

        /**
         * This method needs to be overridden since we need to inform the superclass of
         * exactly which apps should be generated.
         * In this case it is both Giraf and Android applications.
         *
         * @param applications the applications that the task should generate AppImageViews for
         * @return The Hashmap of AppInfos that describe the added applications.
         */
        @Override
        protected ArrayList<AppInfo> doInBackground(Application... applications) {

            List<Application> girafAppsList = ApplicationControlUtility
                .getAvailableGirafAppsForUser(context, currentUser); // For home_activity
            SharedPreferences prefs = LauncherUtility.getSharedPreferencesForCurrentUser(context, currentUser);
            Set<String> androidAppsPackagenames = prefs.getStringSet(
                getString(R.string.selected_android_apps_key), new HashSet<String>());
            List<Application> androidAppsList = ApplicationControlUtility
                .convertPackageNamesToApplications(context, androidAppsPackagenames);
            girafAppsList.addAll(androidAppsList);

            applications = girafAppsList.toArray(applications);

            return super.doInBackground(applications);
        }

        /**
         * Once we have loaded applications, we wait for new app changes.
         */
        @Override
        protected void onPostExecute(final ArrayList<AppInfo> appInfoList) {
            super.onPostExecute(appInfoList);
            currentLoadedApps = appInfoList;

            CirclePageIndicator titleIndicator = (CirclePageIndicator) findViewById(R.id.pageIndicator);
            titleIndicator.setViewPager(appViewPager);

            startObservingApps();
        }
    }
}
