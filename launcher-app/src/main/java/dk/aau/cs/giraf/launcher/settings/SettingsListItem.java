package dk.aau.cs.giraf.launcher.settings;

import android.graphics.drawable.Drawable;

/**
 * Abstract version of items used in the settings list (to the left in SettingsActivity).
 */
public abstract class SettingsListItem {
    protected final String title;
    protected final Drawable icon;

    /**
     * Constructor for any item in the settings list.
     *
     * @param title     The title of the setting item
     * @param icon      Icon to display for the setting item
     */
    public SettingsListItem(String title, Drawable icon) {
        this.title = title;
        this.icon = icon;
    }
}
