package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.oasis.lib.models.Application;

/**
 * The Application class from OasisLib is extended, since Launcher requires some additional information.
 * This includes the icon and background color of an app, along with methods for getting and setting these.
 *
 */
public class AppInfo extends Application implements Parcelable {

    /**
     * The application icon background color.
     */
    private int mBgColor;

    /** The application icon. */
    private Drawable mIcon;

    /**
     * This function returns the application that this AppInfo extends.
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

    public AppInfo(Parcel in){

        /* Data is put in, in this order:
        Int:
        private int id;
        private int mBgColor;
        private int author;
        String:
        private String name;
        private String version;
        private String pack;
        private String activity;
        private String description;
        */
        String[] stringdata = new String[5];
        int[] intdata = new int[3];

        in.readStringArray(stringdata);
        in.readIntArray(intdata);

        // Integers
        this.setId(intdata[0]);
        this.setBgColor(intdata[1]);
        this.setAuthor(intdata[2]);

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
    public void writeToParcel(Parcel dest, int flags)
    {
        // Write integers
        dest.writeIntArray(new int[]{this.getId(),this.getBgColor(), this.getAuthor() });

        // Write strings
        dest.writeStringArray(new String[]{this.getName(), this.getVersion(), this.getPackage(), this.getActivity(), this.getDescription()});
    }

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
                mIcon = app.loadIcon(context.getPackageManager());
                break;
            }
        }
    }


}
