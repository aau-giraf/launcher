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

    public SettingsListItem(String _packageName, String _appName, Drawable _appIcon, Fragment _fragment){
        this.mPackageName = _packageName;
        this.mAppName = _appName;
        this.mAppIcon = _appIcon;
        this.mAppFragment = _fragment;
        this.mIntent = null;
    }

    public SettingsListItem(String _packageName, String _appName, Drawable _appIcon, Intent _intent){
        this.mPackageName = _packageName;
        this.mAppName = _appName;
        this.mAppIcon = _appIcon;
        this.mAppFragment = null;
        this.mIntent = _intent;
    }

    public SettingsListItem(String _appName, Drawable _appIcon, Fragment _fragment){
        this.mPackageName = null;
        this.mAppName = _appName;
        this.mAppIcon = _appIcon;
        this.mAppFragment = _fragment;
        this.mIntent = null;
    }

    public SettingsListItem(String _appName, Drawable _appIcon, Intent _intent) {
        this.mPackageName = null;
        this.mAppName = _appName;
        this.mAppIcon = _appIcon;
        this.mAppFragment = null;
        this.mIntent = _intent;
    }
}
