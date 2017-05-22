package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.models.core.Application;

import java.util.Collections;
import java.util.List;

/**
 * The Application class from OasisLib is extended, since Launcher requires some additional information.
 * This includes the icon and background color of an app, along with methods for getting and setting these.
 */
public class AppInfo extends Application implements Parcelable {

    /**
     * The application icon background color.
     */
    private int iconBackgroundColor;

    /**
     * The application icon.
     */
    private Drawable icon;

    /**
     * This function returns the application that this AppInfo extends.
     *
     * @return The Application of the AppInfo.
     */
    public Application getApp() {
        return this;
    }

    /**
     * Get the icon image of the app.
     *
     * @return The icon image.
     */
    public Drawable getIconImage() {
        return this.icon;
    }

    /**
     * Set the icon image of the app.
     *
     * @param iconImage  The icon image.
     */
    public void setIconImage(Drawable iconImage) {
        this.icon = iconImage;
    }

    /**
     * Creates a new AppInfo from a given parent app.
     *
     * @param parentApp App to get data from.
     */
    public AppInfo(Application parentApp) {
        this.setId(parentApp.getId());
        this.setName(parentApp.getName());
        this.setPackage(parentApp.getPackage());
        this.setActivity(parentApp.getActivity());
    }

    /**
     * Creates a new AppInfo from Parcel information.
     *
     * @param in Parcel with information
     */
    private AppInfo(Parcel in) {

        /* Data is put in, in this order:
        Int:
        private int id;
        private int iconBackgroundColor;
        private int author;
        String:
        private String name;
        private String version;
        private String pack;
        private String activity;
        private String description;
        */
        String[] stringdata = new String[5];
        long[] longdata = new long[3];

        in.readStringArray(stringdata);
        in.readLongArray(longdata);

        // Integers
        this.setId(longdata[0]);
        this.setBgColor((int) longdata[1]);

        // Strings
        this.setName(stringdata[0]);
        this.setPackage(stringdata[2]);
        this.setActivity(stringdata[3]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write strings
        dest.writeStringArray(new String[]{this.getName(),
            this.getPackage(), this.getActivity()});

        // Write integers
        dest.writeLongArray(new long[]{this.getId(), this.getBgColor()});
    }

    /**
     * The Parcelable creator.
     */
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    /**
     * Set the background color.
     *
     * @param color The new background color.
     */
    private void setBgColor(int color) {
        this.iconBackgroundColor = color;
    }

    /**
     * Get the color of the app icon background.
     *
     * @return The background color.
     */
    public int getBgColor() {
        return this.iconBackgroundColor;
    }

    /**
     * Finds the icon of the app.
     *
     * @param context Context of the current activity.
     */
    public void loadIcon(Context context) {
        // Is supposed to allow for custom icons, but does not currently support this.

        List<ResolveInfo> systemApps = ApplicationControlUtility.getAllAppsOnDeviceAsResolveInfoList(context);

        if(ApplicationControlUtility.isAppOnDevice(this,context)){
            for (ResolveInfo app : systemApps) {
                if (app.activityInfo.name.equals(this.getActivity())) {
                    icon = app.loadIcon(context.getPackageManager());
                    break;
                } else{
                    icon = context.getResources().getDrawable(R.drawable.ic_giraf);
                }
            }
        }
        else{
            //ToDo change this icon to a more nice giraf like icon
            icon = context.getResources().getDrawable(dk.aau.cs.giraf.gui.R.drawable.icon_synchronize);
        }


    }


    /**
     * Checks if a given AppInfo list contains different apps from an Application list.
     *
     * @param appInfos A list of appInfo
     * @param applications a list of Applications
     * @return Returns true if the lists contain different apps
     */
    public static boolean isAppListsDifferent(final List<AppInfo> appInfos, final List<Application> applications) {
        if ((appInfos == null || appInfos.isEmpty()) && (applications == null || applications.isEmpty())) {
            return false;
        }
        if ((appInfos != null && applications == null) || appInfos == null) {
            return true;
        }
        if (appInfos.size() != applications.size()) {
            return true;
        } else {
            Collections.sort(appInfos);
            Collections.sort(applications);

            for (int appCounter = 0; appCounter < appInfos.size(); appCounter++) {
                if (!(appInfos.get(appCounter).getId() == applications.get(appCounter).getId())) {
                    return true;
                }
            }

            return false;
        }
    }
}
