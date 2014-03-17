package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.List;

import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.oasis.lib.models.App;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public class AppInfo extends App {

	/**
	 * The intent used to start the application.
	 */
	Intent mIntent;

	/**
	 * The application icon.
	 */
	private Drawable mIcon;

	/**
	 * Get the icon image of the app.
	 * @return The icon image.
	 */
	public Drawable getIconImage() {
		return this.mIcon;
	}

	/**
	 * ID of the guardian who is using the launcher.
	 */
	private Profile mGuardian;
	
	/**
	 * Creates a new AppInfo from a given parent app.
	 * @param parentApp App to get data from.
	 */
	public AppInfo(App parentApp) {
		super.id = parentApp.getId();
		super.activity = parentApp.getActivity();
		super.aPackage = parentApp.getaPackage();
		super.icon = parentApp.getIcon();
		super.name = parentApp.getName();
		super.settings = parentApp.getSettings();
	}

	/**
	 * Set ID for the current guardian using the system.
	 * @param guardian The guardian using the system.
	 */
	public void setGuardian(Profile guardian) {
		if (guardian.getPRole() == Profile.pRoles.GUARDIAN.ordinal()) {
			mGuardian = guardian;
		}
	}
	
	/**
	 * The application icon background color.
	 */
	private int mBgColor;
	
	/**
	 * Set the background color.
	 * @param color The new background color.
	 */
	public void setBgColor(int color) {
		this.mBgColor = color;
	}

	/**
	 * Get the color of the app icon background.
	 * @return The background color.
	 */
	public int getBgColor() {
		return this.mBgColor;
	}

	/**
	 * Get the ID of the currently logged in guardian.
	 * @return The guardian ID.
	 */
	public Long getGuardianID() {
		return mGuardian.getId();
	}

	/**
	 * Getter for the title of the app. 
	 * Cuts the name off, to make sure it's not too long to show in the launcher.
	 * @return shortened name for the app
	 */
	public String getShortenedName() {
		if(name.length() > 6){
			return name.subSequence(0, 5) + "...";
		} else {
			return name;
		}
	}

	/**
	 * Loads information needed by the app.
	 * @param context Context of the current activity.
	 */
	public void load(Context context, Profile guardian) {
		setGuardian(guardian);
		
		loadIcon(context);
	}

	/**
	 * Finds the icon of the app.
	 * @param context Context of the current activity.
	 */
	private void loadIcon(Context context) {
		// Is supposed to allow for custom icons, but does not currently support this.

		List<ResolveInfo> systemApps = LauncherUtility.getDeviceApps(context);

		for (ResolveInfo app : systemApps) {
			if (app.activityInfo.packageName.equals(this.aPackage)) {
				mIcon = app.loadIcon(context.getPackageManager());
				break;
			}
		}
	}

	
}