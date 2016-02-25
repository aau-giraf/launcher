package dk.aau.cs.giraf.launcher.helper;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;

public final class Constants {
	// Launcher TAG
    public static final String TAG = HomeActivity.class.getName();
    public static final String NO_APP_TAG = "-1";
	
	// SharedPreferences keys for log in data
    public static final String LOGIN_SESSION_INFO = "TIMING";
	public static final String LOGIN_TIME = "DATE";
	
	// Keys for inserting and retrieving data in Intents
	public static final String GUARDIAN_ID = "currentGuardianID";
	public static final String CHILD_ID = "currentChildID";
    public static final long NO_CHILD_SELECTED_ID = -1;
	public static final String APP_PACKAGE_NAME = "appPackageName";
	public static final String APP_ACTIVITY_NAME = "appActivityName";
    public static final String APP_COLOR = "appBackgroundColor";
	
	// Main activity
    public static final int LOGO_ANIMATION_DURATION = 2000;

	// Authentication activity
	// Eight hours.
	public static final long TIME_TO_STAY_LOGGED_IN = 28800000;
	
	// Home activity values.
	public static final int APP_ICON_DIMENSION_DEF = 200;
	
	// Error logging
	public static final String ERROR_TAG = "launcher";
}
