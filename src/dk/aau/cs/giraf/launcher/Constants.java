package dk.aau.cs.giraf.launcher;

public final class Constants {
	
	// SharedPreferences keys for log in data
	public static final String TIMER_KEY = "TIMING";
	public static final String DATE_KEY = "DATE";
	
	// Keys for inserting and retrieving data in Intents
	public static final String GUARDIAN_ID = "currentGuardianID";
	public static final String CHILD_ID = "currentChildID";
	public static final String APP_PACKAGE_NAME = "appPackageName";
	public static final String APP_ACTIVITY_NAME = "appActivityName";
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
	public static final int DRAWER_SNAP_LENGTH = 40;
	
	// Home activity graphics values
	public static final int HOMEBAR_LANDSCAPE_WIDTH = 200;
	public static final int HOMEBAR_LANDSCAPE_HEIGHT = 100;
	public static final int HOMEBAR_PORTRAIT_WIDTH = 100;
	public static final int HOMEBAR_PORTRAIT_HEIGHT = 200;
	
	public static final int PROFILE_PIC_LANDSCAPE_WIDTH = 70;
	public static final int PROFIL_EPIC_LANDSCAPE_HEIGHT = 91;
	
	public static final int PROFILE_PIC_PORTRAIT_WIDTH = 100;
	public static final int PROFILE_PIC_PORTRAIT_HEIGHT = 130;
	
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
	public static final String ERROR_TAG = "launcher";

    /**
     * The duration each image of the instruction animation should be displayed. Unit is milliseconds.
     */
    public static final int INSTRUCTION_FRAME_DURATION = 30;

    /**
     * Array with images for the instruction animation
     */
    public static final int[] INSTRUCTION_ANIMATION = { R.drawable.ani_00000, R.drawable.ani_00001, R.drawable.ani_00002, R.drawable.ani_00003,
            R.drawable.ani_00004, R.drawable.ani_00005, R.drawable.ani_00006, R.drawable.ani_00007,
            R.drawable.ani_00008, R.drawable.ani_00009, R.drawable.ani_00010, R.drawable.ani_00011,
            R.drawable.ani_00012, R.drawable.ani_00013, R.drawable.ani_00014, R.drawable.ani_00015,
            R.drawable.ani_00016, R.drawable.ani_00017, R.drawable.ani_00018, R.drawable.ani_00019,
            R.drawable.ani_00020, R.drawable.ani_00021, R.drawable.ani_00022, R.drawable.ani_00023,
            R.drawable.ani_00024, R.drawable.ani_00025, R.drawable.ani_00026, R.drawable.ani_00027,
            R.drawable.ani_00028, R.drawable.ani_00029, R.drawable.ani_00030, R.drawable.ani_00031,
            R.drawable.ani_00032, R.drawable.ani_00033, R.drawable.ani_00034, R.drawable.ani_00035,
            R.drawable.ani_00036, R.drawable.ani_00037, R.drawable.ani_00038, R.drawable.ani_00039,
            R.drawable.ani_00040, R.drawable.ani_00041, R.drawable.ani_00042, R.drawable.ani_00043,
            R.drawable.ani_00044, R.drawable.ani_00045, R.drawable.ani_00046, R.drawable.ani_00047,
            R.drawable.ani_00048, R.drawable.ani_00049, R.drawable.ani_00050, R.drawable.ani_00051,
            R.drawable.ani_00052, R.drawable.ani_00053, R.drawable.ani_00054, R.drawable.ani_00055,
            R.drawable.ani_00056, R.drawable.ani_00057, R.drawable.ani_00058, R.drawable.ani_00059,
            R.drawable.ani_00060, R.drawable.ani_00061, R.drawable.ani_00062, R.drawable.ani_00063,
            R.drawable.ani_00064, R.drawable.ani_00065, R.drawable.ani_00066, R.drawable.ani_00067,
            R.drawable.ani_00068, R.drawable.ani_00069, R.drawable.ani_00070, R.drawable.ani_00071,
            R.drawable.ani_00072, R.drawable.ani_00073, R.drawable.ani_00074, R.drawable.ani_00075,
            R.drawable.ani_00076, R.drawable.ani_00077, R.drawable.ani_00078, R.drawable.ani_00079,
            R.drawable.ani_00080, R.drawable.ani_00081, R.drawable.ani_00082, R.drawable.ani_00083,
            R.drawable.ani_00084, R.drawable.ani_00085, R.drawable.ani_00086, R.drawable.ani_00087,
            R.drawable.ani_00088, R.drawable.ani_00089 };

}