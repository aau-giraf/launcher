package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.graphics.drawable.Drawable;

/**
 * Created by Stefan on 25-04-14.
 */
public class SettingsListItem {
    String mAppName;
    Drawable mAppIcon;
    Fragment mAppFragment;

    public SettingsListItem(String _appName, Drawable _appIcon, Fragment _fragment, Drawable _appColor){
        this.mAppName = _appName;
        this.mAppIcon = _appIcon;
        this.mAppFragment = _fragment;
    }
}
