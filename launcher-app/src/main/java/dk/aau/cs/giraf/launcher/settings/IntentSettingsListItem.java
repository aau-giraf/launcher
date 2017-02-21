package dk.aau.cs.giraf.launcher.settings;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class IntentSettingsListItem extends SettingsListItem {
    protected final Intent intent;

    /**
     * Constructor for any item in the settings list.
     *
     * @param title     The title of the setting item
     * @param icon      Icon to display for the setting item
     * @param intent    The intent to be started as a new activity
     */
    public IntentSettingsListItem(String title, Drawable icon, Intent intent) {
        super(title, icon);
        this.intent = intent;
    }
}
