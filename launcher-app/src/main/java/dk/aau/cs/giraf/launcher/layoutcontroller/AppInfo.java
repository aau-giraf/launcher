package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import dk.aau.cs.giraf.dblib.models.Application;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;

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
        this.setVersion(parentApp.getVersion());
        this.setPackage(parentApp.getPackage());
        this.setActivity(parentApp.getActivity());
        this.setDescription(parentApp.getDescription());
        this.setAuthor(parentApp.getAuthor());
    }

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
        this.setAuthor(longdata[2]);

        // Strings
        this.setName(stringdata[0]);
        this.setVersion(stringdata[1]);
        this.setPackage(stringdata[2]);
        this.setActivity(stringdata[3]);
        this.setDescription(stringdata[4]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write strings
        dest.writeStringArray(new String[]{this.getName(), this.getVersion(),
            this.getPackage(), this.getActivity(), this.getDescription()});

        // Write integers
        dest.writeLongArray(new long[]{this.getId(), this.getBgColor(), (int) this.getAuthor()});
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
     * Getter for the title of the app.
     * Cuts the name off, to make sure it's not too long to show in the launcher.
     *
     * @return shortened name for the app
     */
    public String getShortenedName() {
        if (this.getName().length() > 8) {
            return this.getName().subSequence(0, 5) + "...";
        } else {
            return this.getName();
        }
    }

    /**
     * Finds the icon of the app.
     *
     * @param context Context of the current activity.
     */
    public void loadIcon(Context context) {
        // Is supposed to allow for custom icons, but does not currently support this.

        List<ResolveInfo> systemApps = ApplicationControlUtility.getAllAppsOnDeviceAsResolveInfoList(context);

        for (ResolveInfo app : systemApps) {
            if (app.activityInfo.name.equals(this.getActivity())) {
                icon = app.loadIcon(context.getPackageManager());
                break;
            }
        }
    }

    //ToDo Write JavaDoc
    public static boolean isAppListsDifferent(final List<AppInfo> appInfos, final List<Application> applications) {
        if ((appInfos == null || appInfos.isEmpty()) && (applications == null || applications.isEmpty())) {
            return false;
        }
        if (appInfos != null && applications == null || appInfos == null && applications != null) {
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
