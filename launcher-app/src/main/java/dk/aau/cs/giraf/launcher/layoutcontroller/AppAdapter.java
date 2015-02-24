package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * This class was used to add an adapter to the AppImageViews in HomeActivity
 * It was used to handle the dragging of colors over the apps, which was disabled, since the clients did not want it
 * We still kept the classes, just in case future groups would want or need to reenable it.
 */
public class AppAdapter extends ArrayAdapter<AppInfo> {

    private final ArrayList<AppInfo> mApps;

    /**
     * Constructs a new adapter to handle the presentation of apps in the launcher.
     *
     * @param context Context the adapter is created in.
     * @param apps    The apps to show.
     */
    public AppAdapter(final Context context, final ArrayList<AppInfo> apps) {
        super(context, 0, apps);
        this.mApps = apps;
    }

    /**
     * This function is overridden to add more infomation to the view affected by the adapter
     * @param position the position of the view
     * @param convertView the converted view
     * @param parent the parent of the view
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final AppInfo appInfo = mApps.get(position);

        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.apps_container, parent, false);
        }

        ImageView appIconView = (ImageView) convertView.findViewById(R.id.app_icon);
        TextView appTextView = (TextView) convertView.findViewById(R.id.app_text);

        appTextView.setText(appInfo.getShortenedName());
        appIconView.setImageDrawable(appInfo.getIconImage());
        setAppBackground(convertView, appInfo.getBgColor());

        convertView.setTag(String.valueOf(appInfo.getId()));
        convertView.setOnDragListener(new GAppDragger());

        return convertView;
    }

    /**
     * Sets the background of the app.
     *
     * @param wrapperView     The view the app is located inside.
     * @param backgroundColor The color to use for the background.
     */
    public static void setAppBackground(View wrapperView, int backgroundColor) {
        LinearLayout convertViewLayout = (LinearLayout) wrapperView.findViewById(R.id.app_bg);

        RoundRectShape roundRect = new RoundRectShape(new float[]{15, 15, 15, 15, 15, 15, 15, 15}, new RectF(), null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRect);

        shapeDrawable.getPaint().setColor(backgroundColor);

        convertViewLayout.setBackgroundDrawable(shapeDrawable);
    }

    /**
     * Sets the background color of a given app and saves the color.
     *
     * @param context     Context of the current activity.
     * @param convertView View to change color for.
     * @param color       Color to change to.
     * @param appID       ID of the app to change for.
     */
    public static void saveAppBackground(Context context, View convertView, int color, long appID) {
        setAppBackground(convertView, color);

        final Helper helper = LauncherUtility.getOasisHelper(context);
        final Profile currentUser = LauncherUtility.getCurrentUser(context);

        //TODO: The OasisLib group still needs to fix their dataformat
//		Application launcher = helper.applicationHelper.getApplicationById(currentUser.getId());
//		Setting<String, String, String> launchSetting = launcher.getSettings();
//        AppInfo appInfo = HomeActivity.getAppInfo(String.valueOf(convertView.getTag()));
//        appInfo.setBgColor(color);
//
//        HashMap<String, String> appSettings = launchSetting.get(String.valueOf(appID));
//
//		appSettings.remove(Constants.COLOR_BG);
//		appSettings.put(Constants.COLOR_BG, String.valueOf(color));
//
//        //TODO: Again, this is due to OasisLib group.
//		launcher.setSettings(launchSetting);
        //TODO: Once again, it is now not possible to modify an app according to a user.
        //Update: You are apparently supposed to use helper.profileApplicationHelper, but
        //dont do anything till we know what settings we're actually saving.

        //PREVIOUS CODE!
        //helper.applicationHelper.modifyAppByProfile(launcher, currentUser);
    }
}