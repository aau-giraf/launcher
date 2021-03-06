package dk.aau.cs.giraf.launcher.widgets;

import android.content.Context;
import android.widget.ImageView;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;

/**
 * When apps are created as a view and filled into a target layout,
 * this is the type they are made of. The class adds a few methods to the native ImageView
 */
public class AppImageView extends ImageView {

    private boolean checked = false;
    /**
     * The AppInfo for the ImageView.
     */
    public final AppInfo appInfo;

    /**
     * A constructor for the class.
     *
     * @param context The context of the current activity
     */
    public AppImageView(final Context context, final AppInfo appInfo) {
        super(context);
        this.appInfo = appInfo;
    }

    /**
     * Set an AppView as checked.
     * This is used in SettingsActivity for displaying which apps a user has chosen.
     * Also calls updateColor to set the background color to the correct one.
     *
     * @param checked the value.
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
        updateColor();
    }

    /**
     * Sets the right background color of the AppImageView
     * If checked is not set, sets the background color to transparent
     * If checked is set, sets the background color to light orange.
     * This is only relevant for SettingsActivity to show which apps have been chosen by a user.
     */
    private void updateColor() {
        if (checked) {
            //setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
            setBackgroundResource(R.drawable.app_image_view_marked_background);
        } else {
            setBackgroundDrawable(null);
        }
    }

    /**
     * Toogles an AppView as checked or unchecked.
     * This is used in SettingsActivity for displaying which apps a user has chosen.
     * Also calls updateColor to set the background color to the correct one.
     *
     * @return Whether checked was set to true or false
     */
    public boolean toggle() {

        checked = !checked;

        updateColor();

        return checked;
    }
}
