package dk.aau.cs.giraf.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import dk.aau.cs.giraf.gui.GColorAdapter;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GWidgetCalendar;
import dk.aau.cs.giraf.gui.GWidgetConnectivity;
import dk.aau.cs.giraf.gui.GWidgetLogout;
import dk.aau.cs.giraf.gui.GWidgetUpdater;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.App;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.Setting;

public class HomeActivity extends Activity {

	private static Context mContext;

	private Profile mCurrentUser; 
	private Helper mHelper;
	private App mLauncher;
	private LinearLayout appContainer;
	private TextView mNameView;
	private ImageView mProfilePictureView;

	private int mProfilePictureWidthLandscape;
	private int mProfilePictureHeightLandscape;
	private int mProfilePictureWidthPortrait;
	private int mProfilePictureHeightPortrait;
	private int mLandscapeBarWidth;
	private int mNumberOfApps;

    private static HashMap<String,AppInfo> appInfos;

    private boolean mWidgetRunning = false;

	private GWidgetUpdater mWidgetTimer;
	private GWidgetCalendar mCalendarWidget;
	private GWidgetConnectivity mConnectivityWidget;
	private GWidgetLogout mLogoutWidget;

    private GDialog mLogoutDialog;

	private RelativeLayout mHomeDrawer;
	private RelativeLayout mHomeBarLayout;
	private LinearLayout mPictureLayout;	

	private RelativeLayout.LayoutParams mHomeBarParams;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		mLandscapeBarWidth = LauncherUtility.intToDP(this, Constants.HOMEBAR_LANDSCAPE_WIDTH);

		HomeActivity.mContext = this;
		mHelper = new Helper(mContext);
		
		mCurrentUser = mHelper.profilesHelper.getProfileById(getIntent().getExtras().getLong(Constants.GUARDIAN_ID));
		
		mLauncher = mHelper.appsHelper.getAppByPackageNameAndProfileId(mCurrentUser.getId());

		mNameView = (TextView)this.findViewById(R.id.nameView);
		mNameView.setText(mCurrentUser.getFirstname() + " " + mCurrentUser.getSurname());

		mPictureLayout = (LinearLayout)this.findViewById(R.id.profile_pic);
		mProfilePictureView = (ImageView)this.findViewById(R.id.imageview_profilepic);
		mHomeBarLayout = (RelativeLayout)this.findViewById(R.id.HomeBarLayout);

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

		mProfilePictureWidthLandscape = LauncherUtility.intToDP(mContext, 100);
		mProfilePictureHeightLandscape = LauncherUtility.intToDP(mContext, 100);
		mProfilePictureWidthPortrait = LauncherUtility.intToDP(mContext, 100);
		mProfilePictureHeightPortrait = LauncherUtility.intToDP(mContext, 100);

		loadDrawer();
		loadWidgets();
		loadPaintGrid();
		loadApplications();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		this.drawBar();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWidgetTimer.sendEmptyMessage(GWidgetUpdater.MSG_STOP);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWidgetTimer.sendEmptyMessage(GWidgetUpdater.MSG_START);
	}

    @Override
    public void onBackPressed() {
        //Do nothing, as the user should be able to back out of this activity
    }

	/**
	 * Calculates the current number of columns to use, based on the current number of apps.
	 * @return Number of columns to use, based on the current number of apps
	 */
	private int calculateNumOfColumns() {
		if (mNumberOfApps > Constants.APPS_PER_PAGE) {
			return (int) Math.ceil((mNumberOfApps) / (Constants.MAX_ROWS_PER_PAGE));
		} else {
			return Constants.MAX_COLUMNS_PER_PAGE;
		}
	}

	/**
	 * Repaints the bar
	 */
	private void drawBar() {
		RelativeLayout homebarLayout = (RelativeLayout)this.findViewById(R.id.HomeBarLayout);

		RelativeLayout.LayoutParams homebarLayoutParams = (RelativeLayout.LayoutParams)homebarLayout.getLayoutParams();

		int barHeightLandscape = LauncherUtility.intToDP(mContext, Constants.HOMEBAR_LANDSCAPE_HEIGHT);
		int barHeightPortrait = LauncherUtility.intToDP(mContext, Constants.HOMEBAR_PORTRAIT_HEIGHT);

		/* Get the size of the screen */
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;
		int screenHeight = size.y;

		if (LauncherUtility.isLandscape(mContext)) {
			homebarLayout.setBackground(getResources().getDrawable(R.drawable.homebar_back_land));
			homebarLayoutParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
			homebarLayoutParams.width = barHeightLandscape;
		} else {
			homebarLayout.setBackground(getResources().getDrawable(R.drawable.homebar_back_port));
			homebarLayoutParams.height = barHeightPortrait;
			homebarLayoutParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
		}

		homebarLayout.setLayoutParams(homebarLayoutParams);

		this.drawWidgets();
	}

	/**
	 * Repaints the widgets
	 */
	public void drawWidgets() {
		ViewGroup.LayoutParams profilePictureViewParams = mProfilePictureView.getLayoutParams();
		RelativeLayout.LayoutParams connectivityWidgetParams = (LayoutParams) mConnectivityWidget.getLayoutParams();
		RelativeLayout.LayoutParams calendarWidgetParams = (LayoutParams) mCalendarWidget.getLayoutParams();
		RelativeLayout.LayoutParams logoutWidgetParams = (LayoutParams) mLogoutWidget.getLayoutParams();

		if (LauncherUtility.isLandscape(mContext)) {
			mNameView.setVisibility(View.INVISIBLE);

			profilePictureViewParams.width = LauncherUtility.intToDP(mContext, Constants.PROFILE_PIC_LANDSCAPE_WIDTH);
			profilePictureViewParams.height = LauncherUtility.intToDP(mContext, Constants.PROFIL_EPIC_LANDSCAPE_HEIGHT);
			mHomeBarLayout.setPadding(LauncherUtility.intToDP(mContext, Constants.HOMEBAR_LANDSCAPE_PADDING),
					LauncherUtility.intToDP(mContext, Constants.HOMEBAR_LANDSCAPE_PADDING),
					LauncherUtility.intToDP(mContext, Constants.HOMEBAR_LANDSCAPE_PADDING),
					LauncherUtility.intToDP(mContext, Constants.HOMEBAR_LANDSCAPE_PADDING));

			connectivityWidgetParams.setMargins(LauncherUtility.intToDP(mContext, Constants.WIDGET_CONNECTIVITY_MARGIN_LANDSCAPE_LEFT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CONNECTIVITY_MARGIN_LANDSCAPE_TOP),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CONNECTIVITY_MARGIN_LANDSCAPE_RIGHT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CONNECTIVITY_MARGIN_LANDSCAPE_BOTTOM));

			calendarWidgetParams.setMargins(LauncherUtility.intToDP(mContext, Constants.WIDGET_CALENDAR_MARGIN_LANDSCAPE_LEFT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CALENDAR_MARGIN_LANDSCAPE_TOP),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CALENDAR_MARGIN_LANDSCAPE_RIGHT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CALENDAR_MARGIN_LANDSCAPE_BOTTOM));
			calendarWidgetParams.addRule(RelativeLayout.BELOW, mConnectivityWidget.getId());

			calendarWidgetParams.addRule(RelativeLayout.LEFT_OF, 0);

			logoutWidgetParams.setMargins(LauncherUtility.intToDP(mContext, Constants.WIDGET_LOGOUT_MARGIN_LANDSCAPE_LEFT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_LOGOUT_MARGIN_LANDSCAPE_TOP),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_LOGOUT_MARGIN_LANDSCAPE_RIGHT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_LOGOUT_MARGIN_LANDSCAPE_BOTTOM));

			logoutWidgetParams.addRule(RelativeLayout.BELOW, mCalendarWidget.getId());
			logoutWidgetParams.addRule(RelativeLayout.LEFT_OF, 0);
		} else {
			/**
			 * future todo: implement portrait mode and fix the below code
			 */
			connectivityWidgetParams.setMargins(LauncherUtility.intToDP(mContext, Constants.WIDGET_CONNECTIVITY_MARGIN_PORTRAIT_LEFT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CONNECTIVITY_MARGIN_PORTRAIT_TOP),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CONNECTIVITY_MARGIN_PORTRAIT_RIGHT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CONNECTIVITY_MARGIN_PORTRAIT_BOTTOM));

			calendarWidgetParams.setMargins(LauncherUtility.intToDP(mContext, Constants.WIDGET_CALENDAR_MARGIN_PORTRAIT_LEFT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CALENDAR_MARGIN_PORTRAIT_TOP),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CALENDAR_MARGIN_PORTRAIT_RIGHT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_CALENDAR_MARGIN_PORTRAIT_BOTTOM));

			calendarWidgetParams.addRule(RelativeLayout.BELOW, 0);
			calendarWidgetParams.addRule(RelativeLayout.LEFT_OF, mConnectivityWidget.getId());

			logoutWidgetParams.setMargins(LauncherUtility.intToDP(mContext, Constants.WIDGET_LOGOUT_MARGIN_PORTRAIT_LEFT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_LOGOUT_MARGIN_PORTRAIT_TOP),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_LOGOUT_MARGIN_PORTRAIT_RIGHT),
					LauncherUtility.intToDP(mContext, Constants.WIDGET_LOGOUT_MARGIN_PORTRAIT_BOTTOM));

			logoutWidgetParams.addRule(RelativeLayout.BELOW, 0);
			logoutWidgetParams.addRule(RelativeLayout.LEFT_OF, mCalendarWidget.getId());

			profilePictureViewParams.width = LauncherUtility.intToDP(mContext, Constants.PROFILE_PIC_PORTRAIT_WIDTH);
			profilePictureViewParams.height = LauncherUtility.intToDP(mContext, Constants.PROFILE_PIC_PORTRAIT_HEIGHT);

			mHomeBarLayout.setPadding(LauncherUtility.intToDP(mContext, Constants.HOMEBAR_PORTRAIT_PADDING),
					LauncherUtility.intToDP(mContext, Constants.HOMEBAR_PORTRAIT_PADDING),
					LauncherUtility.intToDP(mContext, Constants.HOMEBAR_PORTRAIT_PADDING),
					LauncherUtility.intToDP(mContext, Constants.HOMEBAR_PORTRAIT_PADDING));

			mNameView.setVisibility(View.VISIBLE);
		}
		mProfilePictureView.setLayoutParams(profilePictureViewParams);	
		mConnectivityWidget.setLayoutParams(connectivityWidgetParams);
		mCalendarWidget.setLayoutParams(calendarWidgetParams);
		mLogoutWidget.setLayoutParams(logoutWidgetParams);
	}

	/**
	 * Load the user's applications into the layout.
	 */
	private void loadApplications() {		
		List<App> girafAppsList = LauncherUtility.getVisibleGirafApps(mContext, mCurrentUser);

		if (girafAppsList != null) {
			appInfos = new HashMap<String,AppInfo>();

			for (App app : girafAppsList) {
				AppInfo appInfo = new AppInfo(app);

				appInfo.load(mContext, mCurrentUser);
				appInfo.setBgColor(appBgColor(appInfo.getId()));
				
				appInfos.put(String.valueOf(appInfo.getId()), appInfo);
			}

            appContainer = (LinearLayout)this.findViewById(R.id.appContainer);

            for (Map.Entry<String,AppInfo> entry : appInfos.entrySet()) {
                appContainer.addView(createAppView(entry.getValue()));
            }

			mNumberOfApps = appInfos.size();
		} else {
			Log.e(Constants.ERROR_TAG, "App list is null");
		}
	}

	/**
	 * Load the user's paintgrid in the drawer.
	 */
	private void loadPaintGrid() {
		GridView AppColors = (GridView) findViewById(R.id.appcolors);
		// Removes blue highlight and scroll on AppColors grid
		AppColors.setEnabled(false);
		AppColors.setAdapter(new GColorAdapter(this));
	}

	/**
	 * Load the drawer and its functionality.
	 */
	private void loadDrawer() {
		// If result = true, the onTouch-function will be run again.
		findViewById(R.id.HomeBarLayout).setOnTouchListener(new View.OnTouchListener() {
			int offset = 0;
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int margin = 0;
				boolean result = true;

				switch (e.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
						offset = (int) e.getX();
						result = true;
						break;
					case MotionEvent.ACTION_MOVE:
						mHomeBarParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
						margin = mHomeBarParams.leftMargin + ((int) e.getX() - offset);

						if (margin < Constants.DRAWER_SNAP_LENGTH) {
							margin = 0;
						} else if (margin > (Constants.DRAWER_WIDTH - Constants.DRAWER_SNAP_LENGTH)) {
							margin = Constants.DRAWER_WIDTH;
						} else if (margin > Constants.DRAWER_WIDTH) {
							margin = Constants.DRAWER_WIDTH;
						}

						mHomeBarParams.setMargins(margin, 0, 0, 0);
						v.setLayoutParams(mHomeBarParams);

						View homeDrawerView = findViewById(R.id.HomeDrawer);
						RelativeLayout.LayoutParams homeDrawerLayoutParams = (RelativeLayout.LayoutParams) homeDrawerView.getLayoutParams();
						homeDrawerLayoutParams.setMargins((margin-(Constants.DRAWER_WIDTH*2)), 0, 0, 0);
						homeDrawerView.setLayoutParams(homeDrawerLayoutParams);
						result = true;

						/* Setting width of the horizontalscrollview */
						HorizontalScrollView hScrollView = (HorizontalScrollView)findViewById(R.id.horizontalScrollView);
						LayoutParams scrollParams = (LayoutParams) hScrollView.getLayoutParams();
						Display display = getWindowManager().getDefaultDisplay();
						Point size = new Point();
						display.getSize(size);
						
						/* removing 100 additional here to accomodate for "4 columns behaviour"
						 * which occure when there are <= 9 apps on the screen and we dont want to be able to scroll
						 */
						scrollParams.width = (size.x - (margin + 100));
						hScrollView.setLayoutParams(scrollParams);
						break;
					case MotionEvent.ACTION_UP:
						break;
				}
				return result;
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
		mHomeDrawer = (RelativeLayout) findViewById(R.id.HomeDrawer);

		mWidgetTimer = new GWidgetUpdater();
		mWidgetTimer.addWidget(mCalendarWidget);
		mWidgetTimer.addWidget(mConnectivityWidget);

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
        appView = inflater.inflate(R.layout.apps, appContainer, false);

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

        appViewLayout.setBackground(shapeDrawable);
    }

    public static AppInfo getAppInfo(String id) {
        return appInfos.get(id);
    }
}