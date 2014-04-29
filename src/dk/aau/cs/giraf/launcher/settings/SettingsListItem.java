package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.graphics.drawable.Drawable;

/**
 * Created by Stefan on 25-04-14.
 */
public class SettingsListItem {
    String appName;
    Drawable appIcon;
    Fragment appFragment;
    Drawable appColor;
    Drawable appColorHighlight;

    public SettingsListItem(String _appName, Drawable _appIcon, Fragment _fragment, Drawable _appColor){
        this.appName = _appName;
        this.appIcon = _appIcon;
        this.appFragment = _fragment;
        this.appColor = _appColor;

        appColorHighlight = appColor.getConstantState().newDrawable();
        appColorHighlight.setAlpha(70);
    }
}
