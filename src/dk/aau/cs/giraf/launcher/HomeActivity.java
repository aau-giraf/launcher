package dk.aau.cs.giraf.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
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

    private boolean appsAdded = false;
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

        appContainer = (LinearLayout)this.findViewById(R.id.appContainer);

		loadDrawer();
		loadWidgets();
		loadPaintGrid();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		this.drawBar();

        if (!appsAdded) {
            appContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    // Ensure you call it only once :
                    appContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    loadApplications();
                }
            });
        }
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
        //Do nothing, as the user should not be able to back out of this activity
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
			homebarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.homebar_back_land));
			homebarLayoutParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
			homebarLayoutParams.width = barHeightLandscape;
		} else {
			homebarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.homebar_back_port));
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
	 * Load the user's applications into the app container.
	 */
	private void loadApplications() {
        //Get the list of apps to show in the container
		List<App> girafAppsList = LauncherUtility.getVisibleGirafApps(mContext, mCurrentUser);

		if (girafAppsList != null) {
			appInfos = new HashMap<String,AppInfo>();

            //Fill AppInfo hashmap with AppInfo objects for each app
			loadAppInfos(girafAppsList);

            //Calculate how many apps the screen can fit on each row, and how much space is available for horizontal padding
            int containerWidth = ((ScrollView)appContainer.getParent()).getWidth();
            int appsPrRow = containerWidth / Constants.APP_ICON_DIMENSION;
            int paddingWidth = (containerWidth % Constants.APP_ICON_DIMENSION) / (appsPrRow + 1);

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int containerHeight = ((ScrollView)appContainer.getParent()).getHeight();
            int appsPrColumn = containerHeight / Constants.APP_ICON_DIMENSION;
            int paddingHeight = (containerHeight % Constants.APP_ICON_DIMENSION) / (appsPrColumn + 1);

            //Add the first row to the container
            LinearLayout currentAppRow = new LinearLayout(mContext);
            currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
            currentAppRow.setPadding(0, paddingHeight, 0, paddingHeight);
            appContainer.addView(currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (Map.Entry<String,AppInfo> entry : appInfos.entrySet()) {
                View newAppView = createAppView(entry.getValue());
                newAppView.setPadding(paddingWidth, 0, 0, 0);
                currentAppRow.addView(newAppView);

                if (currentAppRow.getChildCount() == appsPrRow) {
                    currentAppRow = new LinearLayout(mContext);
                    currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
                    currentAppRow.setPadding(0, 0, 0, paddingHeight);
                    appContainer.addView(currentAppRow);
                }
            }

            //Remember that the apps have been added, so they are not added again by the listener
            appsAdded = true;

		} else {
			Log.e(Constants.ERROR_TAG, "App list is null");
		}
	}

    /**
     * Loads the AppInfo object of app from the list, into the {@code appInfos} hashmap, making
     * them accesible with only the ID string of the app.
     * @param appsList The list of accessible apps
     */
    private void loadAppInfos(List<App> appsList) {
        appInfos = new HashMap<String,AppInfo>();

        for (App app : appsList) {
            AppInfo appInfo = new AppInfo(app);

            appInfo.load(mContext, mCurrentUser);
            appInfo.setBgColor(appBgColor(appInfo.getId()));

            appInfos.put(String.valueOf(appInfo.getId()), appInfo);
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
				boolean result = true;

				switch (e.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
						offset = (int) e.getX();
						break;
					case MotionEvent.ACTION_MOVE:
					case MotionEvent.ACTION_UP:
                        placeDrawer(offset, e, v);
						break;
				}
				return result;
			}
        });

        // This closes the drawer after starting to drag a color and
        // opens it again once you stop dragging.
        findViewById(R.id.HomeBarLayout).setOnDragListener(new View.OnDragListener() {
            int offset = 0;

            @Override
            public boolean onDrag(View v, DragEvent e) {
                boolean result = true;

                switch (e.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        offset = (int) e.getX();
                        popBackDrawer(offset, e, v, true);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        offset = (int) e.getX();
                        popBackDrawer(offset, e, v, false);
                        break;
                }
                return result;
            }
        });
	}

    /**
     * this function places the drawer correctly, depending on if it is still being dragged (ACTION_MOVE) or not (ACTION_UP)
     * @param offset The offset of the screen to the right. This depends on how far it has been dragged
     * @param e The MotionEvent that called the function. This will either be ACTION_MOVE or ACTION_UP
     * @param v The view that the function is called in. This is be the current view.
     */
    private void placeDrawer( int offset, MotionEvent e, View v)
    {
        mHomeBarParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
        int margin = mHomeBarParams.leftMargin + ((int) e.getX() - offset);

        if(e.getActionMasked() == MotionEvent.ACTION_MOVE){
            if (margin < Constants.DRAWER_SNAP_LENGTH) {
                margin = 0;
            } else if (margin > (Constants.DRAWER_WIDTH - Constants.DRAWER_SNAP_LENGTH)) {
                margin = Constants.DRAWER_WIDTH;
            } else if (margin > Constants.DRAWER_WIDTH) {
                margin = Constants.DRAWER_WIDTH;
            }
        }
        else if(e.getActionMasked() == MotionEvent.ACTION_UP){
            if (margin < Constants.DRAWER_WIDTH/2) {
                margin = 0;
            } else {
                margin = Constants.DRAWER_WIDTH;
            }
        }

        mHomeBarParams.setMargins(margin, 0, 0, 0);
        v.setLayoutParams(mHomeBarParams);

        View homeDrawerView = findViewById(R.id.HomeDrawer);
        RelativeLayout.LayoutParams homeDrawerLayoutParams = (RelativeLayout.LayoutParams) homeDrawerView.getLayoutParams();
        homeDrawerLayoutParams.setMargins((margin - (Constants.DRAWER_WIDTH * 2)), 0, 0, 0);
        homeDrawerView.setLayoutParams(homeDrawerLayoutParams);

        /* Setting width of the scrollview */
        ScrollView hScrollView = (ScrollView)findViewById(R.id.horizontalScrollView);
        LayoutParams scrollParams = (LayoutParams) hScrollView.getLayoutParams();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        /* removing 100 additional here to accomodate for "4 columns behaviour"
         * which occure when there are <= 9 apps on the screen and we dont want to be able to scroll
         */
        scrollParams.width = (size.x - (margin + 100));
        hScrollView.setLayoutParams(scrollParams);
    }

    private void popBackDrawer(int offset, DragEvent e, View v, boolean startedDragging)
    {
        mHomeBarParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
        Integer margin;
        Integer current = offset;

        if(!startedDragging)
            margin = Constants.DRAWER_WIDTH;
        else
            margin = 0;

        while(margin != current){

            mHomeBarParams.setMargins(margin, 0, 0, 0);
            v.setLayoutParams(mHomeBarParams);

            View homeDrawerView = findViewById(R.id.HomeDrawer);
            RelativeLayout.LayoutParams homeDrawerLayoutParams = (RelativeLayout.LayoutParams) homeDrawerView.getLayoutParams();
            homeDrawerLayoutParams.setMargins((margin - (Constants.DRAWER_WIDTH * 2)), 0, 0, 0);
            homeDrawerView.setLayoutParams(homeDrawerLayoutParams);

            /* Setting width of the scrollview */
            ScrollView hScrollView = (ScrollView)findViewById(R.id.horizontalScrollView);
            LayoutParams scrollParams = (LayoutParams) hScrollView.getLayoutParams();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            /* removing 100 additional here to accomodate for "4 columns behaviour"
             * which occure when there are <= 9 apps on the screen and we dont want to be able to scroll
             */
            scrollParams.width = (size.x - (margin + 100));
            hScrollView.setLayoutParams(scrollParams);

            //v.animate().translationX(margin-current).setDuration(1000);

            if(margin < current)
                current--;
            else
                current++;
            ;

        }
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

        appViewLayout.setBackgroundDrawable(shapeDrawable);
    }

    public static AppInfo getAppInfo(String id) {
        return appInfos.get(id);
    }
}