package dk.aau.cs.giraf.launcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import dk.aau.cs.giraf.gui.*;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.App;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.Setting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HomeActivity extends Activity {

	private static Context mContext;

	private Profile mCurrentUser; 
	private Helper mHelper;
	private App mLauncher;

    private static HashMap<String,AppInfo> mAppInfos;

    private boolean mAppsAdded = false;
    private boolean mWidgetRunning = false;

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

    private RelativeLayout.LayoutParams mAppsScrollViewParams;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		HomeActivity.mContext = this;
		mHelper = new Helper(mContext);
		
		mCurrentUser = mHelper.profilesHelper.getProfileById(getIntent().getExtras().getLong(Constants.GUARDIAN_ID));
		mLauncher = mHelper.appsHelper.getAppByPackageNameAndProfileId(mCurrentUser.getId());

        loadViews();
		loadDrawer();
		loadWidgets();
		loadHomeDrawerColorGrid();
        setupLogoutDialog();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

        if (!mAppsAdded) {
            mAppsContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    // Ensure you call it only once :
                    mAppsContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    loadApplications();
                }
            });
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
		List<App> girafAppsList = LauncherUtility.getVisibleGirafApps(mContext, mCurrentUser);

		if (girafAppsList != null) {
			mAppInfos = new HashMap<String,AppInfo>();

            //Fill AppInfo hashmap with AppInfo objects for each app
			loadAppInfos(girafAppsList);

            //Calculate how many apps the screen can fit on each row, and how much space is available for horizontal padding
            int containerWidth = ((ScrollView) mAppsContainer.getParent()).getWidth();
            int appsPrRow = containerWidth / Constants.APP_ICON_DIMENSION;
            int paddingWidth = (containerWidth % Constants.APP_ICON_DIMENSION) / (appsPrRow + 1);

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int containerHeight = ((ScrollView) mAppsContainer.getParent()).getHeight();
            int appsPrColumn = containerHeight / Constants.APP_ICON_DIMENSION;
            int paddingHeight = (containerHeight % Constants.APP_ICON_DIMENSION) / (appsPrColumn + 1);

            //Add the first row to the container
            LinearLayout currentAppRow = new LinearLayout(mContext);
            currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
            currentAppRow.setPadding(0, paddingHeight, 0, paddingHeight);
            mAppsContainer.addView(currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (Map.Entry<String,AppInfo> entry : mAppInfos.entrySet()) {
                View newAppView = createAppView(entry.getValue());
                newAppView.setPadding(paddingWidth, 0, 0, 0);
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

		} else {
			Log.e(Constants.ERROR_TAG, "App list is null");
		}
	}

    /**
     * Loads the AppInfo object of app from the list, into the {@code mAppInfos} hashmap, making
     * them accesible with only the ID string of the app.
     * @param appsList The list of accessible apps
     */
    private void loadAppInfos(List<App> appsList) {
        mAppInfos = new HashMap<String,AppInfo>();

        for (App app : appsList) {
            AppInfo appInfo = new AppInfo(app);

            appInfo.load(mContext, mCurrentUser);
            appInfo.setBgColor(appBgColor(appInfo.getId()));

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
        mAppsScrollView = (ScrollView)this.findViewById(R.id.horizontalScrollView);
    }

    /**
     * Setup the logout dialog
     */
    private void setupLogoutDialog() {
        String logoutHeadline = mContext.getResources().getString(R.string.Log_out);
        String logoutDescription = mContext.getResources().getString(R.string.Log_out_description);
        mLogoutDialog = new GDialog(mContext, R.drawable.large_switch_profile, logoutHeadline, logoutDescription, new View.OnClickListener() {
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
	 */
	private void loadDrawer() {
		// If result = true, the onTouch-function will be run again.
		mHomeBarLayout.setOnTouchListener(new View.OnTouchListener() {
            int offset = 0;

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
	}

    private void placeDrawer()
    {
        final int to;

        if(mSideBarView.isSideBarHidden)
            to = Constants.DRAWER_WIDTH;
        else
            to = -Constants.DRAWER_WIDTH;

        // then animate the view translating from (0, 0)
        TranslateAnimation ta = new TranslateAnimation(0, to, 0, 0);
        ta.setDuration(500);
        mSideBarView.startAnimation(ta);

        ta.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // Sets the left margin of the scrollview based on the width of the homebar
                mAppsScrollViewParams = new RelativeLayout.LayoutParams(mAppsScrollView.getLayoutParams());
                mAppsScrollViewParams.leftMargin = LauncherUtility.intToDP(mContext, mHomeBarLayout.getWidth());
                mAppsScrollView.setLayoutParams(mAppsScrollViewParams);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
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
	 * @param appID ID of the app to find the background color for.
	 * @return The background color of the app.
	 */
	private int appBgColor(Long appID) {
		int[] colors = getResources().getIntArray(R.array.appcolors);
		Setting<String, String, String> launcherSettings = mLauncher.getSettings();

		// If settings for the given app exists.
		if (launcherSettings != null && launcherSettings.containsKey(String.valueOf(appID))) {
			HashMap<String, String> appSetting = launcherSettings.get(String.valueOf(appID));

			// If color settings for the given app exists.
			if (appSetting != null && appSetting.containsKey(Constants.COLOR_BG)) {
				return Integer.parseInt(appSetting.get(Constants.COLOR_BG));
			}
		}

		//Randomize a color, if no setting exist, and save it. 
		int position = (new Random()).nextInt(colors.length);

		// No settings existed, save the new.
		saveNewBgColor(colors[position], appID);

		return colors[position];
	}

	/**
	 * Saves a new color in the launcher settings.
	 * @param color Color to save.
	 * @param appID ID of the app to save for.
	 */
	private void saveNewBgColor(int color, long appID) {
		Setting<String, String, String> launcherSettings = mLauncher.getSettings();

		if (launcherSettings == null) {
			launcherSettings = new Setting<String, String, String>();
		}

		// If no app specific settings exist.
		if (!launcherSettings.containsKey(String.valueOf(appID))) {
			launcherSettings.addValue(String.valueOf(appID), Constants.COLOR_BG, String.valueOf(color));
		} else if (!launcherSettings.get(String.valueOf(appID)).containsKey(Constants.COLOR_BG)) {
			/* If no app specific color settings exist.*/
			launcherSettings.get(String.valueOf(appID)).put(Constants.COLOR_BG, String.valueOf(color));
		}

		mLauncher.setSettings(launcherSettings);
		mHelper.appsHelper.modifyAppByProfile(mLauncher, mCurrentUser);
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

        appTextView.setText(appInfo.getShortenedName());
        appIconView.setImageDrawable(appInfo.getIconImage());
        setAppBackground(appView, appInfo.getBgColor());

        appView.setTag(String.valueOf(appInfo.getId()));
        appView.setOnDragListener(new GAppDragger());

        appView.setOnClickListener(new ProfileLauncher());

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