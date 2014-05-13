package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.List;

import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public class AppInfo extends Application {

    /** The application icon. */
    private Drawable mIcon;

    public Application getApp() {
        return this;
    }

    /**
     * Get the icon image of the app.
     *
     * @return The icon image.
     */
    public Drawable getIconImage() {
        return this.mIcon;
    }

    /**
     * Creates a new AppInfo from a given parent app.
     *
     * @param parentApp App to get data from.
     */
    public AppInfo(Application parentApp) {
        this.setId(parentApp.getId());
        this.setName(parentApp.getName());
        this.setVersion(parentApp.getVersion());
        this.setPackage(parentApp.getPackage());
        this.setActivity(parentApp.getActivity());
        this.setDescription(parentApp.getDescription());
        this.setAuthor(parentApp.getAuthor());
    }

    /**
     * The application icon background color.
     */
    private int mBgColor;

    /**
     * Set the background color.
     *
     * @param color The new background color.
     */
    public void setBgColor(int color) {
        this.mBgColor = color;
    }

    /**
     * Get the color of the app icon background.
     *
     * @return The background color.
     */
    public int getBgColor() {
        return this.mBgColor;
    }

    /**
     * Getter for the title of the app.
     * Cuts the name off, to make sure it's not too long to show in the launcher.
     *
     * @return shortened name for the app
     */
    public String getShortenedName() {
        if (this.getName().length() > 6) {
            return this.getName().subSequence(0, 5) + "...";
        } else {
            return this.getName();
        }
    }

    /**
     * Loads information needed by the app.
     *
     * @param context Context of the current activity.
     */
    public void load(Context context) {
        loadIcon(context);
    }

    /**
     * Finds the icon of the app.
     *
     * @param context Context of the current activity.
     */
    private void loadIcon(Context context) {
        // Is supposed to allow for custom icons, but does not currently support this.

        List<ResolveInfo> systemApps = LauncherUtility.getDeviceApps(context);

        for (ResolveInfo app : systemApps) {
            if (app.activityInfo.name.equals(this.getActivity())) {
                mIcon = app.loadIcon(context.getPackageManager());
                break;
            }
        }
    }


}
