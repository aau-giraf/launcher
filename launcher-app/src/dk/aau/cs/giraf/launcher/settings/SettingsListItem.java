package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by Stefan on 25-04-14.
 */
public class SettingsListItem {
    String mPackageName;
    String mAppName;
    Drawable mAppIcon;
    Fragment mAppFragment;
    Intent mIntent;

    private SettingsListItem(String packageName, String appName, Drawable appIcon,
                             Fragment fragment, Intent intent)
    {
        this.mPackageName = packageName;
        this.mAppName = appName;
        this.mAppIcon = appIcon;
        this.mAppFragment = fragment;
        this.mIntent = intent;
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
    public SettingsListItem(String packageName, String appName, Drawable appIcon, Fragment _fragment){
        this(packageName, appName, appIcon, _fragment, null);
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
    public SettingsListItem(String packageName, String appName, Drawable appIcon, Intent intent){
        this(packageName, appName, appIcon, null, intent);
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
    public SettingsListItem(String appName, Drawable appIcon, Fragment fragment){
        this(null, appName, appIcon, fragment, null);
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
    public SettingsListItem(String appName, Drawable appIcon, Intent intent) {
        this(null, appName, appIcon, null, intent);
    }
}
