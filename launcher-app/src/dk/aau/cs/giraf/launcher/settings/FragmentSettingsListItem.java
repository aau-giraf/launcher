package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.graphics.drawable.Drawable;

public class FragmentSettingsListItem extends SettingsListItem {
    protected final Fragment fragment;
    protected final android.support.v4.app.Fragment supportFragment;
    /**
     * Constructor for any item in the settings list
     *
     * @param title     The title of the setting item
     * @param icon      Icon to display for the setting item
     * @param fragment  The fragment to be started by FragmentManager
     */
    public FragmentSettingsListItem(String title, Drawable icon, Fragment fragment) {
        super(title, icon);
        this.fragment = fragment;
        this.supportFragment = null;
    }

    public FragmentSettingsListItem(String title, Drawable icon, android.support.v4.app.Fragment fragment) {
        super(title, icon);
        this.fragment = null;
        this.supportFragment = fragment;
    }
}
