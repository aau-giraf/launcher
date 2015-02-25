package dk.aau.cs.giraf.launcher.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import dk.aau.cs.giraf.gui.GButtonSettings;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.gui.GWidgetLogout;
import dk.aau.cs.giraf.gui.GWidgetProfileSelection;
import dk.aau.cs.giraf.gui.GWidgetUpdater;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.launcher.layoutcontroller.DrawerLayout;
import dk.aau.cs.giraf.launcher.settings.SettingsActivity;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.launcher.settings.components.ApplicationGridResizer;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * The primary activity of Launcher. Allows the user to start other GIRAF apps and access the settings
 * activity. It requires a user id in the parent intent.
 */
public class HomeActivity extends FragmentActivity implements AppsFragmentInterface {

    private Profile mCurrentUser;
    private Profile mLoggedInGuardian;
    private Helper mHelper;
    private LoadHomeActivityApplicationTask loadHomeActivityApplicationTask;

    private ArrayList<AppInfo> mCurrentLoadedApps;

    private boolean mIsAppsContainerInitialized = false;
    private boolean mWidgetRunning = false;
    private boolean mDrawerAnimationRunning = false;


    private GWidgetUpdater mWidgetUpdater;
    private GWidgetProfileSelection mWidgetProfileSelection;
    private GProfileSelector mProfileSelectorDialog;
    private GDialog mLogoutDialog;

    private RelativeLayout mDrawerContentView;
    //private RelativeLayout mSidebarView;
    private DrawerLayout mDrawerView;
    //private LinearLayout mAppsContainer;
    //private ScrollView mAppsScrollView;
    private ViewPager mAppViewPager;
    //private AppsFragmentAdapter appsFragmentAdapter;

    private Timer mAppsUpdater;

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

        mHelper = LauncherUtility.getOasisHelper(this);

        mCurrentUser = mHelper.profilesHelper.getProfileById(getIntent().getExtras().getInt(Constants.GUARDIAN_ID));
        mLoggedInGuardian = mHelper.profilesHelper.getProfileById(getIntent().getExtras().getInt(Constants.GUARDIAN_ID));

        // Fetch references to view objects
        mDrawerView = (DrawerLayout) this.findViewById(R.id.DrawerView);
        mAppViewPager = (ViewPager) this.findViewById(R.id.appsViewPager);

        loadDrawer();                 //Temporarily disabled, see JavaDoc.
        loadWidgets();
        setupLogoutDialog();

        // Show warning if DEBUG_MODE is true
        if (LauncherUtility.isDebugging()) {
            LauncherUtility.showDebugInformation(this);
        }

        final int rowsSize = ApplicationGridResizer.getGridRowSize(this, mCurrentUser);
        final int columnsSize = ApplicationGridResizer.getGridColumnSize(this, mCurrentUser);

        mAppViewPager.setAdapter(new AppsFragmentAdapter(getSupportFragmentManager(), mCurrentLoadedApps, rowsSize, columnsSize));

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
     * Loads app icons into the activity. Before this point in the activity lifecycle, it is not
     * possible to determine the size of the app container, making et much more difficult to calculate
     * icon spacing.
     *
     * @param hasFocus {@code true} if the activity has focus.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!mIsAppsContainerInitialized && mAppViewPager.getViewTreeObserver() != null) {
            mAppViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    // Ensure you call it only once :
                    mAppViewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    loadApplications();
                }
            });
        } else if (mAppViewPager.getViewTreeObserver() == null) {
            Log.e(Constants.ERROR_TAG, "ViewTreeObserver is null.");
        }
    }

    /**
     * Redraws the application container and resumes the timer looking for updates in the set of
     * available apps.
     *
     * @see HomeActivity#startObservingApps()
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mIsAppsContainerInitialized) {
            reloadApplications();
        }
        //startObservingApps();
        if (mWidgetUpdater != null) {
            mWidgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_START);
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
        mIsAppsContainerInitialized = true; //This makes the Launcher load applications if it was paused before loading them.
        if (mAppsUpdater != null) {
            mAppsUpdater.cancel();
            Log.d(Constants.ERROR_TAG, "Applications are no longer observed.");
        }

        if (mWidgetUpdater != null) {
            mWidgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_STOP);
        }
        if (loadHomeActivityApplicationTask != null) {
            loadHomeActivityApplicationTask.cancel(true);
        }

        //mAppViewPager.setAdapter(null);
    }


    /**
     * Does nothing, to prevent the user from returning to the authentication_activity or native OS.
     */
    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
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
        loadHomeActivityApplicationTask = new LoadHomeActivityApplicationTask(this, mCurrentUser, mLoggedInGuardian, mAppViewPager, null);
        loadHomeActivityApplicationTask.execute();

    }

    /**
     * Initialises the logout dialog.
     */
    private void setupLogoutDialog() {
        String logoutHeadline = this.getResources().getString(R.string.Log_out);
        String logoutDescription = this.getResources().getString(R.string.Log_out_description);
        mLogoutDialog = new GDialogMessage(this, R.drawable.large_switch_profile, logoutHeadline, logoutDescription, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LauncherUtility.logOutIntent(HomeActivity.this));
                mLogoutDialog.dismiss();
                HomeActivity.this.finish();
            }
        });
        mLogoutDialog.setOwnerActivity(this);
    }

    /**
     * Load the drawer and its functionality.
     * This has been temporarily disabled, as it turned out that the clients had no use for it.
     * It is left in, as it may become useful at a later date.
     * You can read more in the report about the Launcher from 2014.
     */
    private void loadDrawer() {
        /*
        // If result = true, the onTouch-function will be run again.
		mSidebarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                boolean result = true;

                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_DOWN:
                        placeDrawer();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return result;
            }
        });
        */

        /*mAppsScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                boolean result = true;

                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_DOWN:
                        popBackDrawer();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return result;
            }
        });
        */
        /*
        // This closes the drawer after starting to drag a color and
        // opens it again once you stop dragging.
        mSidebarView.setOnDragListener(new View.OnDragListener() {
            int offset = 0;
            @Override
            public boolean onDrag(View v, DragEvent e) {
                boolean result = true;

                switch (e.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        placeDrawer();
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        placeDrawer();
                        break;
                }
                return result;
            }
        });
        */
    }

    /**
     * Supporting function for {@link HomeActivity#loadDrawer()}
     */
    private void popBackDrawer() {
        int to;

        if (!mDrawerView.isSideBarHidden) {
            to = -mDrawerContentView.getWidth();

            // then animate the view translating from (0, 0)
            TranslateAnimation ta = new TranslateAnimation(0, to, 0, 0);
            ta.setDuration(500);

            if (!mDrawerAnimationRunning) {
                mDrawerView.startAnimation(ta);
                mDrawerAnimationRunning = true;
            }

            ta.setAnimationListener(new TranslateAnimation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Sets the left margin of the scrollview based on the width of the homebar
                    //mAppsScrollView.setLeft(mDrawerContentView.getWidth());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mDrawerAnimationRunning = false;
                }
            });
        }
    }

    /**
     * Supporting function for {@link HomeActivity#loadDrawer()}
     */
    /*
    private void placeDrawer(){
        int to;

        if(mDrawerView.isSideBarHidden)
            to = mDrawerContentView.getWidth();
        else
            to = -mDrawerContentView.getWidth();

        // then animate the view translating from (0, 0)
        TranslateAnimation ta = new TranslateAnimation(0, to, 0, 0);
        ta.setDuration(500);
        if(!mDrawerAnimationRunning){
            mDrawerView.startAnimation(ta);
            mDrawerAnimationRunning = true;
        }

        ta.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Sets the left margin of the scrollview based on the width of the homebar
                mAppsScrollView.setLeft(mDrawerContentView.getWidth());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDrawerAnimationRunning = false;
            }
        });
    }
    */

    /**
     * Loads the sidebar's widgets.
     *
     * @see dk.aau.cs.giraf.gui.GWidgetConnectivity
     * @see dk.aau.cs.giraf.gui.GWidgetLogout
     * @see dk.aau.cs.giraf.gui.GWidgetProfileSelection
     * @see dk.aau.cs.giraf.gui.GButtonSettings
     */
    private void loadWidgets() {
        GWidgetLogout logoutWidget = (GWidgetLogout) findViewById(R.id.logoutwidget);
        mWidgetProfileSelection = (GWidgetProfileSelection) findViewById(R.id.profile_widget);
        GButtonSettings settingsButton = (GButtonSettings) findViewById(R.id.settingsbutton);
        mDrawerContentView = (RelativeLayout) findViewById(R.id.DrawerContentView);

        /*Setup the profile selector dialog. If the current user is not a guardian, the guardian is used
          as the current user.*/
        if (mCurrentUser.getRole() != Profile.Roles.GUARDIAN)
            mProfileSelectorDialog = new GProfileSelector(this, mLoggedInGuardian, mCurrentUser);
        else
            mProfileSelectorDialog = new GProfileSelector(this, mLoggedInGuardian, null);

        //Set up widget updater, which updates the widget's view regularly, according to its status.
        mWidgetUpdater = new GWidgetUpdater();
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
        } else {
            settingsButton.setVisibility(View.GONE);
        }


        mWidgetProfileSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentUser.getRole().getValue() < Profile.Roles.CHILD.getValue()) {
                    mProfileSelectorDialog.show();
                }
            }
        });
        mWidgetProfileSelection.setImageBitmap(mCurrentUser.getImage());

        updatesProfileSelector();

        logoutWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mWidgetRunning) {
                    mWidgetRunning = true;
                    mLogoutDialog.show();
                    mWidgetRunning = false;
                }
            }
        });
    }

    /**
     * Updates the user's preferred icon size from SharedPreferences. This preference it set in Launcher's
     * PreferenceFragment. The size is saved in {@link HomeActivity#mIconSize}.
     */

    /**
     * Updates the ProfileSelector. It is needed when a new user has been selected, as a different
     * listener is needed, and the app container has to be reloaded.
     */
    private void updatesProfileSelector() {
        mProfileSelectorDialog.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProfileController pc = new ProfileController(HomeActivity.this);
                mCurrentUser = pc.getProfileById((int) l);
                mProfileSelectorDialog.dismiss();

                if (mCurrentUser.getRole() != Profile.Roles.GUARDIAN)
                    mProfileSelectorDialog = new GProfileSelector(HomeActivity.this, mLoggedInGuardian, mCurrentUser);
                else
                    mProfileSelectorDialog = new GProfileSelector(HomeActivity.this, mLoggedInGuardian, null);

                mWidgetProfileSelection.setImageBitmap(mCurrentUser.getImage());

                updatesProfileSelector();

                // Reload the widgets in the left side of the screen
                loadWidgets();

                // Reload the application container, as a new user has been selected.
                reloadApplications();
            }
        });
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
            List<Application> girafAppsList = ApplicationControlUtility.getAvailableGirafAppsForUser(HomeActivity.this, mCurrentUser); // For home_activity activity
            SharedPreferences prefs = LauncherUtility.getSharedPreferencesForCurrentUser(HomeActivity.this, mCurrentUser);
            Set<String> androidAppsPackagenames = prefs.getStringSet(getString(R.string.selected_android_apps_key), new HashSet<String>());
            List<Application> androidAppsList = ApplicationControlUtility.convertPackageNamesToApplications(HomeActivity.this, androidAppsPackagenames);
            girafAppsList.addAll(androidAppsList);
            if (mCurrentLoadedApps == null || mCurrentLoadedApps.size() != girafAppsList.size()) {
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
         */
        public LoadHomeActivityApplicationTask(Context context, Profile currentUser, Profile guardian, ViewPager appsViewPager, View.OnClickListener onClickListener) {
            super(context, currentUser, guardian, appsViewPager, onClickListener);
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
            ArrayList<AppInfo> appInfos = super.doInBackground(applications);

            return appInfos;
        }

        /**
         * Once we have loaded applications, we start observing for new apps
         */
        @Override
        protected void onPostExecute(ArrayList<AppInfo> appInfos) {
            super.onPostExecute(appInfos);

            mCurrentLoadedApps = appInfos;
            mIsAppsContainerInitialized = true;

            startObservingApps();
        }
    }
}