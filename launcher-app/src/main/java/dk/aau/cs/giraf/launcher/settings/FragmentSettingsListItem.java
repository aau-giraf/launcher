package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.graphics.drawable.Drawable;

/**
 * Used to allow for both android.app.Fragment and android.support.v4.app.Fragment in the launcher settings
 */
public class FragmentSettingsListItem extends SettingsListItem {
    protected final Fragment fragment;
    protected final android.support.v4.app.Fragment supportFragment;

    /**
     * Constructor for any item in the settings list.
     *
     * @param title    The title of the setting item
     * @param icon     Icon to display for the setting item
     * @param fragment The fragment to be started by FragmentManager
     */
    public FragmentSettingsListItem(final String title, final Drawable icon, final Fragment fragment) {
        super(title, icon);
        this.fragment = fragment;
        this.supportFragment = null;
    }

    /**
     * The constructer.
     * @param title the title
     * @param icon the icon
     * @param fragment the fragment
     */
    public FragmentSettingsListItem(final String title, final Drawable icon,
                                    final android.support.v4.app.Fragment fragment)
    {
        super(title, icon);
        this.fragment = null;
        this.supportFragment = fragment;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public android.support.v4.app.Fragment getSupportFragment() {
        return supportFragment;
    }
}
