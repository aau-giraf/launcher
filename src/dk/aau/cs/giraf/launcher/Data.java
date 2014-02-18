package dk.aau.cs.giraf.launcher;

public final class Data {
	
	// SharedPreferences keys for log in data
	public static final String TIMERKEY = "TIMING";
	public static final String DATEKEY = "DATE";
	
	// Keys for inserting and retrieving data in Intents
	public static final String GUARDIANID = "currentGuardianID";
	public static final String CHILDID = "currentChildID";
	public static final String APP_PACKAGENAME = "appPackageName";
	public static final String APP_ACTIVITYNAME = "appActivityName";
	public static final String APP_COLOR = "appBackgroundColor";

	// Keys for settings
	public static final String COLOR_BG = "backgroundColor";

	// Constants denoting user roles
	public static final long ROLE_GUARDIAN = 1L;
	public static final long ROLE_CHILD = 3L;
	
	// Logo activity
	public static final int TIME_TO_DISPLAY_LOGO = 2000;

	// Authentication activity
	// Eight hours.
	public static final long TIME_TO_STAY_LOGGED_IN = 28800000;
	
	// Home activity values.
	public static final int DRAWER_WIDTH = 400;
	public static final int GRID_CELL_WIDTH = 290;
	public static final int APPS_PER_PAGE = 9;
	public static final int MAX_ROWS_PER_PAGE = 3;
	public static final int MAX_COLUMNS_PER_PAGE = 4;
	public static final int DRAWER_SNAPLENGTH = 40;
	
	// Home activity graphics values
	public static final int HOMEBAR_LANDSCAPE_WIDTH = 200;
	public static final int HOMEBAR_LANDSCAPE_HEIGHT = 100;
	public static final int HOMEBAR_PORTRAIT_WIDTH = 100;
	public static final int HOMEBAR_PORTRAIT_HEIGHT = 200;
	
	public static final int PROFILEPIC_LANDSCAPE_WIDTH = 70;
	public static final int PROFILEPIC_LANDSCAPE_HEIGHT = 91;
	
	public static final int PROFILEPIC_PORTRAIT_WIDTH = 100;
	public static final int PROFILEPIC_PORTRAIT_HEIGHT = 130;
	
	public static final int HOMEBAR_LANDSCAPE_PADDING = 15;
	public static final int HOMEBAR_PORTRAIT_PADDING = 15;

	public static final int WIDGET_CONNECTIVITY_MARGIN_LANDSCAPE_LEFT = 0;
	public static final int WIDGET_CONNECTIVITY_MARGIN_LANDSCAPE_TOP = 106;
	public static final int WIDGET_CONNECTIVITY_MARGIN_LANDSCAPE_RIGHT = 0;
	public static final int WIDGET_CONNECTIVITY_MARGIN_LANDSCAPE_BOTTOM = 0;
	
	public static final int WIDGET_CONNECTIVITY_MARGIN_PORTRAIT_LEFT = 0;
	public static final int WIDGET_CONNECTIVITY_MARGIN_PORTRAIT_TOP = 0;
	public static final int WIDGET_CONNECTIVITY_MARGIN_PORTRAIT_RIGHT = 0;
	public static final int WIDGET_CONNECTIVITY_MARGIN_PORTRAIT_BOTTOM = 0;
	
	public static final int WIDGET_CALENDAR_MARGIN_LANDSCAPE_LEFT = 0;
	public static final int WIDGET_CALENDAR_MARGIN_LANDSCAPE_TOP = 15;
	public static final int WIDGET_CALENDAR_MARGIN_LANDSCAPE_RIGHT = 0;
	public static final int WIDGET_CALENDAR_MARGIN_LANDSCAPE_BOTTOM = 0;
	
	public static final int WIDGET_CALENDAR_MARGIN_PORTRAIT_LEFT = 0;
	public static final int WIDGET_CALENDAR_MARGIN_PORTRAIT_TOP = 0;
	public static final int WIDGET_CALENDAR_MARGIN_PORTRAIT_RIGHT = 25;
	public static final int WIDGET_CALENDAR_MARGIN_PORTRAIT_BOTTOM = 0;
	
	public static final int WIDGET_LOGOUT_MARGIN_LANDSCAPE_LEFT = 0;
	public static final int WIDGET_LOGOUT_MARGIN_LANDSCAPE_TOP = 390;
	public static final int WIDGET_LOGOUT_MARGIN_LANDSCAPE_RIGHT = 0;
	public static final int WIDGET_LOGOUT_MARGIN_LANDSCAPE_BOTTOM = 0;
	
	public static final int WIDGET_LOGOUT_MARGIN_PORTRAIT_LEFT = 0;
	public static final int WIDGET_LOGOUT_MARGIN_PORTRAIT_TOP = 0;
	public static final int WIDGET_LOGOUT_MARGIN_PORTRAIT_RIGHT = 25;
	public static final int WIDGET_LOGOUT_MARGIN_PORTRAIT_BOTTOM = 0;
	
	// Error logging
	public static final String ERRORTAG = "launcher";

}
