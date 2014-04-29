package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dk.aau.cs.giraf.gui.GColorAdapter;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.gui.GWidgetCalendar;
import dk.aau.cs.giraf.gui.GWidgetConnectivity;
import dk.aau.cs.giraf.gui.GWidgetLogout;
import dk.aau.cs.giraf.gui.GWidgetUpdater;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.GAppDragger;
import dk.aau.cs.giraf.launcher.layoutcontroller.SideBarLayout;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public class HomeActivity extends Activity {

	private static Context mContext;

	private Profile mCurrentUser; 
	private Helper mHelper;
	private Application mLauncher;

    private static HashMap<String,AppInfo> mAppInfos;

    private boolean mAppsAdded = false;
    private boolean mWidgetRunning = false;
    private boolean mDrawerAnimationRunning = false;

	private GWidgetUpdater mWidgetUpdater;
	private GWidgetCalendar mCalendarWidget;
	private GWidgetConnectivity mConnectivityWidget;
	private GWidgetLogout mLogoutWidget;

    private GDialog mLogoutDialog;

	private RelativeLayout mHomeDrawerView;
    private RelativeLayout mHomeBarLayout;
    private SideBarLayout mSideBarView;
	private LinearLayout mProfilePictureView;
    private LinearLayout mAppsContainer;
    private ScrollView mAppsScrollView;
    private EasyTracker mEasyTracker;

    private RelativeLayout.LayoutParams mAppsScrollViewParams;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		HomeActivity.mContext = this;
        mHelper = LauncherUtility.getOasisHelper(mContext);

        mCurrentUser = mHelper.profilesHelper.getProfileById(getIntent().getExtras().getInt(Constants.GUARDIAN_ID));
		mLauncher = mHelper.applicationHelper.getApplicationById(mCurrentUser.getId());

        loadViews();
		loadDrawer();
		loadWidgets();
		loadHomeDrawerColorGrid();
        setupLogoutDialog();

        // Start logging this activity
        mEasyTracker.getInstance(this).activityStart(this);
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
		mWidgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_STOP);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWidgetUpdater.sendEmptyMessage(GWidgetUpdater.MSG_START);
	}

    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }

	/**
	 * Load the user's applications into the app container.
	 */
	private void loadApplications() {
        //Get the list of apps to show in the container
        //List<Application> girafAppsList = LauncherUtility.getAvailableGirafAppsButLauncher(mContext);
		List<Application> girafAppsList = LauncherUtility.getVisibleGirafApps(mContext, mCurrentUser);

        TextView noAppsMessage = (TextView) findViewById(R.id.noAppsMessage);
        if (girafAppsList != null && !girafAppsList.isEmpty()) {
            mAppInfos = new HashMap<String,AppInfo>();

            // Tell the user that apps are being loaded
            Toast toast = Toast.makeText(this, getString(R.string.loading_apps_message), 1000);
            toast.show();

            //Fill AppInfo hash map with AppInfo objects for each app
			loadAppInfos(girafAppsList);

            //Calculate how many apps the screen can fit on each row, and how much space is available for horizontal padding
            int containerWidth = ((ScrollView) mAppsContainer.getParent()).getWidth();
            int appsPrRow = getAmountOfApps(containerWidth);
            int paddingWidth = getAppPadding(containerWidth, appsPrRow);

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int containerHeight = ((ScrollView) mAppsContainer.getParent()).getHeight();
            int appsPrColumn = getAmountOfApps(containerHeight);
            int paddingHeight = getAppPadding(containerHeight, appsPrColumn);

            //Add the first row to the container
            LinearLayout currentAppRow = new LinearLayout(mContext);
            currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
            currentAppRow.setPadding(0, paddingHeight, 0, paddingHeight);
            mAppsContainer.addView(currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (Map.Entry<String,AppInfo> entry : mAppInfos.entrySet()) {
                View newAppView = createAppView(entry.getValue());
                newAppView.setPadding(paddingWidth, 0, 0, 0);
                newAppView.setScaleX(0.9f);
                newAppView.setScaleY(0.9f);
                currentAppRow.addView(newAppView);

                if (currentAppRow.getChildCount() == appsPrRow) {
                    currentAppRow = new LinearLayout(mContext);
                    currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
                    currentAppRow.setPadding(0, 0, 0, paddingHeight);
                    mAppsContainer.addView(currentAppRow);
                }
            }

            //Remember that the apps have been added, so they are not added again by the listener
            mAppsAdded = true;

            // If apps are loaded, show a toast.
            toast = Toast.makeText(this, getString(R.string.apps_loaded_message), 2000);
            toast.show();
        } else {
            // show no apps available message
            noAppsMessage.setVisibility(View.VISIBLE);
            Log.e(Constants.ERROR_TAG, "App list is null");
		}
	}


    private int getAmountOfApps(int containerHeight) {
        return containerHeight / Constants.APP_ICON_DIMENSION;
    }

    private int getAppPadding(int containerWidth, int appsPrRow) {
        return (containerWidth % Constants.APP_ICON_DIMENSION) / (appsPrRow + 1);
    }

    /**
     * Loads the AppInfo object of app from the list, into the {@code mAppInfos} hashmap, making
     * them accesible with only the ID string of the app.
     * @param appsList The list of accessible apps
     */
    private void loadAppInfos(List<Application> appsList) {
        mAppInfos = new HashMap<String,AppInfo>();

        for (Application app : appsList) {
            AppInfo appInfo = new AppInfo(app);

            appInfo.load(mContext, mCurrentUser);
            appInfo.setBgColor(appBgColor(appInfo));

            mAppInfos.put(String.valueOf(appInfo.getId()), appInfo);
        }
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
        mProfilePictureView = (LinearLayout)this.findViewById(R.id.profile_pic);
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
        mLogoutDialog.setOwnerActivity((Activity)mContext);
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
		mCalendarWidget = (GWidgetCalendar) findViewById(R.id.calendarwidget);
		mConnectivityWidget = (GWidgetConnectivity) findViewById(R.id.connectivitywidget);
		mLogoutWidget = (GWidgetLogout) findViewById(R.id.logoutwidget);
		mHomeDrawerView = (RelativeLayout) findViewById(R.id.HomeDrawer);
        mProfilePictureView = (LinearLayout) findViewById(R.id.profile_pic);

		mWidgetUpdater = new GWidgetUpdater();
		mWidgetUpdater.addWidget(mCalendarWidget);
		mWidgetUpdater.addWidget(mConnectivityWidget);

		mLogoutWidget.setOnClickListener(new View.OnClickListener() {
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

    /**
     * @param appInfo
     * @return
     */
    private View createAppView(AppInfo appInfo) {
        View appView;

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        appView = inflater.inflate(R.layout.apps, mAppsContainer, false);

        ImageView appIconView = (ImageView) appView.findViewById(R.id.app_icon);
        TextView appTextView = (TextView) appView.findViewById(R.id.app_text);

        appTextView.setText(appInfo.getName());
        appIconView.setImageDrawable(appInfo.getIconImage());
        setAppBackground(appView, appInfo.getBgColor());

        appView.setTag(String.valueOf(appInfo.getApp().getId()));
        appView.setOnDragListener(new GAppDragger());
        if(mCurrentUser.getRole() == Profile.Roles.GUARDIAN)
            appView.setOnClickListener(new ProfileLauncher());
        else{appView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInfo app = HomeActivity.getAppInfo((String)v.getTag());
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName(app.getPackage(), app.getActivity()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                intent.putExtra(Constants.CHILD_ID, mCurrentUser.getId());
                intent.putExtra(Constants.APP_COLOR, app.getBgColor());
                intent.putExtra(Constants.APP_PACKAGE_NAME, app.getPackage());
                intent.putExtra(Constants.APP_ACTIVITY_NAME, app.getActivity());

                // Verify the intent will resolve to at least one activity
                LauncherUtility.secureStartActivity(v.getContext(), intent);
            }
            });
        }

        return appView;
    }

    /**
     * Sets the background of the app.
     * @param wrapperView The view the app is located inside.
     * @param backgroundColor The color to use for the background.
     */
    private void setAppBackground(View wrapperView, int backgroundColor) {
        LinearLayout appViewLayout = (LinearLayout) wrapperView.findViewById(R.id.app_bg);

        RoundRectShape roundRect = new RoundRectShape( new float[] {15,15, 15,15, 15,15, 15,15}, new RectF(), null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRect);

        shapeDrawable.getPaint().setColor(backgroundColor);

        appViewLayout.setBackgroundDrawable(shapeDrawable);
    }

    public static AppInfo getAppInfo(String id) {
        return mAppInfos.get(id);
    }
}