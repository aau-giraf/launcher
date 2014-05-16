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

    private SettingsListItem(String _packageName, String _appName, Drawable _appIcon,
                             Fragment _fragment, Intent _intent)
    {
        this.mPackageName = _packageName;
        this.mAppName = _appName;
        this.mAppIcon = _appIcon;
        this.mAppFragment = _fragment;
        this.mIntent = _intent;
    }

    /**
     * Object used in by list adapter to render a row in ListView.
     * This constructor can be used to add a fragment to be displayed
     * inside SettingsActivity by package name with custom name and icon.
     * @see dk.aau.cs.giraf.launcher.settings.SettingsActivity
     * @see dk.aau.cs.giraf.launcher.settings.SettingsListAdapter
     * @param _packageName Package name of the application to add.
     * @param _appName Name of the application.
     * @param _appIcon Icon of the application to be showed in ListView.
     * @param _fragment The fragment to be started by FragmentManager.
     */
    public SettingsListItem(String _packageName, String _appName, Drawable _appIcon, Fragment _fragment){
        this(_packageName, _appName, _appIcon, _fragment, null);
    }

    /**
     * Object used in by list adapter to render a row in ListView.
     * This constructor can be used to add an external application by package name
     * with custom name and icon started through an intent.
     * @see dk.aau.cs.giraf.launcher.settings.SettingsListAdapter
     * @param _packageName Package name of the application to add.
     * @param _appName Name of the application.
     * @param _appIcon Icon of the application to be showed in ListView.
     * @param _intent The intent to be started as a new activity.
     */
    public SettingsListItem(String _packageName, String _appName, Drawable _appIcon, Intent _intent){
        this(_packageName, _appName, _appIcon, null, _intent);
    }

    /**
     * Object used in by list adapter to render a row in ListView.
     * This constructor can be used to add a fragment to be displayed
     * inside SettingsActivity with custom name and icon.
     * @see dk.aau.cs.giraf.launcher.settings.SettingsActivity
     * @see dk.aau.cs.giraf.launcher.settings.SettingsListAdapter
     * @param _appName Name of the application.
     * @param _appIcon Icon of the application to be showed in ListView.
     * @param _fragment The fragment to be started by FragmentManager.
     */
    public SettingsListItem(String _appName, Drawable _appIcon, Fragment _fragment){
        this(null, _appName, _appIcon, _fragment, null);
    }

    /**
     * Object used in by list adapter to render a row in ListView.
     * This constructor can be used when adding an external application
     * with custom name and icon.
     * @see dk.aau.cs.giraf.launcher.settings.SettingsListAdapter
     * @param _appName Name of the application.
     * @param _appIcon Icon of the application to be showed in ListView.
     * @param _intent The intent to be started as a new activity.
     */
    public SettingsListItem(String _appName, Drawable _appIcon, Intent _intent) {
        this(null, _appName, _appIcon, null, _intent);
    }
}
