package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import dk.aau.cs.giraf.gui.GButtonSettings;
import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GColorAdapter;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.gui.GToast;
import dk.aau.cs.giraf.gui.GWidgetCalendar;
import dk.aau.cs.giraf.gui.GWidgetConnectivity;
import dk.aau.cs.giraf.gui.GWidgetLogout;
import dk.aau.cs.giraf.gui.GWidgetProfileSelection;
import dk.aau.cs.giraf.gui.GWidgetUpdater;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.SideBarLayout;
import dk.aau.cs.giraf.launcher.settings.SettingsActivity;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.settingslib.settingslib.SettingsUtility;

public class HomeActivity extends Activity {

	private static Context mContext;

    private Profile mLoggedInGuardian;
	private Profile mCurrentUser;
	private Helper mHelper;
	private Application mLauncher;

    private static HashMap<String,AppInfo> mAppInfos;
    private List<Application> mCurrentLoadedApps;

    private boolean mAppsAdded = false;
    private boolean mWidgetRunning = false;
    private boolean mDrawerAnimationRunning = false;
    private int mIconSize;

	private GWidgetUpdater mWidgetUpdater;
    private GProfileSelector mProfileSelectorWidget;
    private GDialog mLogoutDialog;

	private RelativeLayout mHomeDrawerView;
    private RelativeLayout mHomeBarLayout;
    private SideBarLayout mSideBarView;
    private LinearLayout mAppsContainer;
    private ScrollView mAppsScrollView;
    private EasyTracker mEasyTracker;
    private Timer mAppsUpdater;

    private RelativeLayout.LayoutParams mAppsScrollViewParams;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		HomeActivity.mContext = this;
        mHelper = LauncherUtility.getOasisHelper(mContext);

        mCurrentUser = mHelper.profilesHelper.getProfileById(getIntent().getExtras().getInt(Constants.GUARDIAN_ID));
        mLoggedInGuardian = mHelper.profilesHelper.getProfileById(getIntent().getExtras().getInt(Constants.GUARDIAN_ID));
		mLauncher = mHelper.applicationHelper.getApplicationById(mCurrentUser.getId());

        loadViews();
		loadDrawer();
		loadWidgets();
		loadHomeDrawerColorGrid();
        setupLogoutDialog();

        // Start logging this activity
        mEasyTracker.getInstance(this).activityStart(this);
	}

    private void StartObservingApps() {
        mAppsUpdater = new Timer();
        AppsObserver timerTask = new AppsObserver();
        mAppsUpdater.scheduleAtFixedRate(timerTask, 5000, 5000);

        Log.d(Constants.ERROR_TAG, "Applications are being observed.");
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop logging this activity
        mEasyTracker.getInstance(this).activityStop(this);
    }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

        if (!mAppsAdded && mAppsContainer.getViewTreeObserver() != null) {
            mAppsContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    // Ensure you call it only once :
                    mAppsContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    loadApplications();
                }
            });
        } else if (mAppsContainer.getViewTreeObserver() == null) {
            Log.e(Constants.ERROR_TAG, "ViewTreeObserver is null.");
        }
	}

	@Override
	protected void onPause() {
		super.onPause();
        mAppsUpdater.cancel();
        Log.d(Constants.ERROR_TAG, "Applications are no longer observed.");
		mWidgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_STOP);
	}

	@Override
	protected void onResume() {
		super.onResume();
        StartObservingApps();
        reloadApplications();
		mWidgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_START);
	}

    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }


    private void reloadApplications(){
        if (!mAppsAdded) return;
        mCurrentLoadedApps = null; // Force loadApplications to redraw
        loadApplications();
    }

    /**
     * Load the user's applications into the app container.
     */
    private void loadApplications(){
        List<Application> girafAppsList = LauncherUtility.getVisibleGirafApps(mContext, mCurrentUser); // For home activity
        SharedPreferences prefs = LauncherUtility.getSharedPreferencesForCurrentUser(mContext);
        Set<String> androidAppsPackagenames = prefs.getStringSet(Constants.SELECTED_ANDROID_APPS, new HashSet<String>());
        List<Application> androidAppsList = LauncherUtility.convertPackageNamesToApplications(mContext, androidAppsPackagenames);
        girafAppsList.addAll(androidAppsList);
        if (mCurrentLoadedApps == null || mCurrentLoadedApps.size() != girafAppsList.size()){
            getIconSize(); // Update mIconSize
            mAppInfos = LauncherUtility.loadGirafApplicationsIntoView(mContext, girafAppsList, mAppsContainer, mIconSize, null);
            //Remember that the apps have been added, so they are not added again by the listener
            if (mAppInfos == null){
                mAppsAdded = false;
                TextView noAppsMessage = (TextView) findViewById(R.id.noAppsMessage);
                noAppsMessage.setVisibility(View.VISIBLE);
            }
            else{
                mAppsAdded = true;
            }
        }
        mCurrentLoadedApps = girafAppsList;
    }

	/**
	 * Load the user's paintgrid in the drawer.
	 */
	private void loadHomeDrawerColorGrid() {
		GridView AppColors = (GridView) findViewById(R.id.appcolors);
		// Removes blue highlight and scroll on AppColors grid
		AppColors.setEnabled(false);
		AppColors.setAdapter(new GColorAdapter(this));
	}

    /**
     * Finds all views used
     */
    private void loadViews() {
        mHomeBarLayout = (RelativeLayout) this.findViewById(R.id.HomeBarLayout);
        mSideBarView = (SideBarLayout)this.findViewById(R.id.SideBarLayout);
        mAppsContainer = (LinearLayout)this.findViewById(R.id.appContainer);
        mAppsScrollView = (ScrollView) this.findViewById(R.id.appScrollView);

        // Show warning if DEBUG_MODE is true
        LauncherUtility.ShowDebugInformation(this);
    }

    /**
     * Setup the logout dialog
     */
    private void setupLogoutDialog() {
        String logoutHeadline = mContext.getResources().getString(R.string.Log_out);
        String logoutDescription = mContext.getResources().getString(R.string.Log_out_description);
        mLogoutDialog = new GDialogMessage(mContext, R.drawable.large_switch_profile, logoutHeadline, logoutDescription, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LauncherUtility.logOutIntent(mContext));
                mLogoutDialog.dismiss();
                ((Activity) mContext).finish();
            }
        });
        mLogoutDialog.setOwnerActivity((Activity) mContext);
    }

	/**
	 * Load the drawer and its functionality.
     * It is currently disabled, since it was found that end users do not require changing the color of apps.
     * Because of this, it is considered to be goldplating.
     * It could be implemented again, but it would have to be implemented with sending colors to all apps and
     * making sure that these colors fit with the overall theme of the system.
     * You can read more in the report about the Launcher from 2014.
	 */
	private void loadDrawer() {
		// If result = true, the onTouch-function will be run again.
        /*
		mHomeBarLayout.setOnTouchListener(new View.OnTouchListener() {

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

        mAppsScrollView.setOnTouchListener(new View.OnTouchListener() {

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

        // This closes the drawer after starting to drag a color and
        // opens it again once you stop dragging.
        mHomeBarLayout.setOnDragListener(new View.OnDragListener() {
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

    private void popBackDrawer(){
        int to;

        if(!mSideBarView.isSideBarHidden)
        {
            to = -mHomeDrawerView.getWidth();

            // then animate the view translating from (0, 0)
            TranslateAnimation ta = new TranslateAnimation(0, to, 0, 0);
            ta.setDuration(500);

            if(!mDrawerAnimationRunning){
                mSideBarView.startAnimation(ta);
                mDrawerAnimationRunning = true;
            }

            ta.setAnimationListener(new TranslateAnimation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                        // Sets the left margin of the scrollview based on the width of the homebar
                        mAppsScrollViewParams = new RelativeLayout.LayoutParams(mAppsScrollView.getLayoutParams());
                        mAppsScrollViewParams.leftMargin = mHomeBarLayout.getWidth();
                        mAppsScrollView.setLayoutParams(mAppsScrollViewParams);
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

    private void placeDrawer(){
        int to;

        if(mSideBarView.isSideBarHidden)
            to = mHomeDrawerView.getWidth();
        else
            to = -mHomeDrawerView.getWidth();

        // then animate the view translating from (0, 0)
        TranslateAnimation ta = new TranslateAnimation(0, to, 0, 0);
        ta.setDuration(500);
        if(!mDrawerAnimationRunning){
            mSideBarView.startAnimation(ta);
            mDrawerAnimationRunning = true;
        }

        ta.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

                // Sets the left margin of the scrollview based on the width of the homebar
                mAppsScrollViewParams = new RelativeLayout.LayoutParams(mAppsScrollView.getLayoutParams());
                mAppsScrollViewParams.leftMargin = mHomeBarLayout.getWidth();
                mAppsScrollView.setLayoutParams(mAppsScrollViewParams);
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

	/**
	 * Load the widgets placed on the drawer.
	 */

	private void loadWidgets() {
        GWidgetCalendar calendarWidget = (GWidgetCalendar) findViewById(R.id.calendarwidget);
        GWidgetConnectivity connectivityWidget = (GWidgetConnectivity) findViewById(R.id.connectivitywidget);
        GWidgetLogout logoutWidget = (GWidgetLogout) findViewById(R.id.logoutwidget);
        GWidgetProfileSelection profileSelectionWidget = (GWidgetProfileSelection) findViewById(R.id.profile_widget);
        GButtonSettings settingsButton = (GButtonSettings) findViewById(R.id.settingsbutton);
		mHomeDrawerView = (RelativeLayout) findViewById(R.id.HomeDrawer);

        if(mCurrentUser.getRole() != Profile.Roles.GUARDIAN)
            mProfileSelectorWidget = new GProfileSelector(mContext, mLoggedInGuardian, mCurrentUser);
        else
            mProfileSelectorWidget = new GProfileSelector(mContext, mLoggedInGuardian, null);

		mWidgetUpdater = new GWidgetUpdater();
		mWidgetUpdater.addWidget(calendarWidget);
		mWidgetUpdater.addWidget(connectivityWidget);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SettingsActivity.class);
                intent.putExtra(Constants.GUARDIAN_ID, mLoggedInGuardian.getId());
                if(mCurrentUser.getRole() == Profile.Roles.GUARDIAN)
                    intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);
                else
                    intent.putExtra(Constants.CHILD_ID, mCurrentUser.getId());

                startActivity(intent);
            }
        });

        profileSelectionWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSelectorWidget.show();
            }
        });

        SetProfileSelector();

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
	 * Finds the background color of a given app, and if no color exists for the app, it is given one.
	 * @param appInfo of the app to find the background color for.
	 * @return The background color of the app.
	 */
	private int appBgColor(AppInfo appInfo) {
		int[] colors = getResources().getIntArray(R.array.appcolors);
        //TODO: The OasisLib group still needs to fix the settings format and more.
//        ProfileApplication profileApplication = mHelper.profileApplicationHelper.getProfileApplicationByProfileIdAndApplicationId(appInfo.getApp(), mCurrentUser);
//        Setting<String, String, String> launcherSettings = profileApplication.getSettings();
//
//		// If settings for the given app exists.
//		if (launcherSettings != null && launcherSettings.containsKey(String.valueOf(appInfo.getApp().getId()))) {
//			HashMap<String, String> appSetting = launcherSettings.get(String.valueOf(appInfo.getApp().getId()));
//
//			// If color settings for the given app exists.
//			if (appSetting != null && appSetting.containsKey(Constants.COLOR_BG)) {
//				return Integer.parseInt(appSetting.get(Constants.COLOR_BG));
//			}
//		}
        //Randomize a color, if no setting exist, and save it.
		int position = (new Random()).nextInt(colors.length);

		// No settings existed, save the new.
        //saveNewBgColor(colors[position], appInfo);

        return colors[position];
	}

	/**
	 * Saves a new color in the launcher settings.
	 * @param color Color to save.
	 * @param appInfo the appInfo of the app to save for.
	 */
	private void saveNewBgColor(int color, AppInfo appInfo) {
        //TODO: Currently, settings is just a blob of data. The OasisLib group is fixing the format, so we'll need to adjust depending on what format they're going with
//        ProfileApplication profileApplication = mHelper.profileApplicationHelper.getProfileApplicationByProfileIdAndApplicationId(appInfo.getApp(), mCurrentUser);
//		Setting<String, String, String> launcherSettings = profileApplication.getSettings();
//
//		if (launcherSettings == null) {
//			launcherSettings = new Setting<String, String, String>();
//		}
//
//		// If no app specific settings exist.
//		if (!launcherSettings.containsKey(String.valueOf(appInfo.getApp().getId()))) {
//			launcherSettings.addValue(String.valueOf(appInfo.getApp().getId()), Constants.COLOR_BG, String.valueOf(color));
//		} else if (!launcherSettings.get(String.valueOf(appInfo.getApp().getId())).containsKey(Constants.COLOR_BG)) {
//			/* If no app specific color settings exist.*/
//			launcherSettings.get(String.valueOf(appInfo.getApp().getId())).put(Constants.COLOR_BG, String.valueOf(color));
//		}
//
//        //TODO: The OasisLib group still needs to fix the settings format and more.
//		mLauncher.setSettings(launcherSettings);
//        //TODO: This function no longer exists. Now it can modify an application, regardless of user.
//		mHelper.applicationHelper.modifyAppByProfile(mLauncher, mCurrentUser);
    }

    private void getIconSize() {
        SharedPreferences prefs = SettingsUtility.getLauncherSettings(mContext, LauncherUtility.getSharedPreferenceUser(mContext));
        int size = prefs.getInt(Constants.ICON_SIZE_PREF, 200);
        mIconSize = SettingsUtility.convertToDP(this, size);
    }

    public static AppInfo getAppInfo(String id) {
        return mAppInfos.get(id);
    }

    /**
     * This is used to set the onClickListener for a new ProfileSelector
     * It must be used everytime a new selector is set.
     * */
    private void SetProfileSelector()
    {
        mProfileSelectorWidget.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProfileController pc = new ProfileController(mContext);
                mCurrentUser = pc.getProfileById((int)l);
                mProfileSelectorWidget.dismiss();

                if(mCurrentUser.getRole() != Profile.Roles.GUARDIAN)
                    mProfileSelectorWidget = new GProfileSelector(mContext, mLoggedInGuardian, mCurrentUser);
                else
                    mProfileSelectorWidget = new GProfileSelector(mContext, mLoggedInGuardian, null);

                SetProfileSelector();
            }
        });
        reloadApplications();
    }

    /**
     * Timer task for observing if new apps has been added
     */
    private class AppsObserver extends TimerTask {
        @Override
        public void run() {
            // run this on UI thread since UI might need to get updated
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadApplications();
                }
            });
            Log.d(Constants.ERROR_TAG, "Applications checked");
        }

    }
}