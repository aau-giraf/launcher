package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * An instance of this class is an item on the list of settings to the left in SettingsActivity.
 * It contains much additional information needed to handle the settings of the contents of the item.
 */
public class SettingsListItem {

    /**
     * The variables needed by the class.
     * The packagename of the application this item is hosting.
     * The name of the application this item is hosting.
     * The icon of the application this item is hosting.
     * The Fragment that should be loaded when this item is pressed.
     * The intent that should be launched when this item is pressed.
     */
    String mPackageName;
    String mAppName;
    String mSummary;
    Drawable mAppIcon;
    Fragment mAppFragment;
    Intent mIntent;

    /**
     * the constructor of the class
     * @param packageName Package name of the application to add.
     * @param appName Name of the application.By using
     * @param appIcon Icon of the application to be showed in ListView.
     * @param fragment The fragment to be started by FragmentManager.
     * @param intent The intent to be started as a new activity.
     */
    private SettingsListItem(String packageName, String appName, Drawable appIcon, Fragment fragment, Intent intent, String summary){
        this.mPackageName = packageName;
        this.mAppName = appName;
        this.mAppIcon = appIcon;
        this.mAppFragment = fragment;
        this.mIntent = intent;
        this.mSummary = summary;
    }

    /**
     * Object used in by list adapter to render a row in ListView.
     * This constructor can be used to add a fragment to be displayed
     * inside SettingsActivity by package name with custom name and icon.
     * @see dk.aau.cs.giraf.launcher.settings.SettingsActivity
     * @see dk.aau.cs.giraf.launcher.settings.SettingsListAdapter
     * @param packageName Package name of the application to add.
     * @param appName Name of the application.
     * @param appIcon Icon of the application to be showed in ListView.
     * @param _fragment The fragment to be started by FragmentManager.
     */
    public SettingsListItem(String packageName, String appName, Drawable appIcon, Fragment _fragment, String summaary ){
        this(packageName, appName, appIcon, _fragment, null, summaary);
    }

    /**
     * Object used in by list adapter to render a row in ListView.
     * This constructor can be used to add an external application by package name
     * with custom name and icon started through an intent.
     * @see dk.aau.cs.giraf.launcher.settings.SettingsListAdapter
     * @param packageName Package name of the application to add.
     * @param appName Name of the application.
     * @param appIcon Icon of the application to be showed in ListView.
     * @param intent The intent to be started as a new activity.
     */
    public SettingsListItem(String packageName, String appName, Drawable appIcon, Intent intent, String summary){
        this(packageName, appName, appIcon, null, intent, summary);
    }

    /**
     * Object used in by list adapter to render a row in ListView.
     * This constructor can be used to add a fragment to be displayed
     * inside SettingsActivity with custom name and icon.
     * @see dk.aau.cs.giraf.launcher.settings.SettingsActivity
     * @see dk.aau.cs.giraf.launcher.settings.SettingsListAdapter
     * @param appName Name of the application.
     * @param appIcon Icon of the application to be showed in ListView.
     * @param fragment The fragment to be started by FragmentManager.
     */
    public SettingsListItem(String appName, Drawable appIcon, Fragment fragment, String summary){
        this(null, appName, appIcon, fragment, null, summary);
    }

    /**
     * Object used in by list adapter to render a row in ListView.
     * This constructor can be used when adding an external application
     * with custom name and icon.
     * @see dk.aau.cs.giraf.launcher.settings.SettingsListAdapter
     * @param appName Name of the application.
     * @param appIcon Icon of the application to be showed in ListView.
     * @param intent The intent to be started as a new activity.
     */
    public SettingsListItem(String appName, Drawable appIcon, Intent intent, String summary) {
        this(null, appName, appIcon, null, intent, summary);
    }
}
