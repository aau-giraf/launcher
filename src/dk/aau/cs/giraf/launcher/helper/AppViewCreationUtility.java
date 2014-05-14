package dk.aau.cs.giraf.launcher.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.GAppDragger;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Vagner on 13-05-14.
 */
public class AppViewCreationUtility {

    private static HashMap<String,AppInfo> mAppInfoHashMap;
    private final static String DEFAULT_PACKAGE_FILTER = "";
    private final static boolean DEFAULT_FILTER_INCLUSION = true;

    /**
     * Loads the AppInfo object of app from the list, into the {@code mAppInfoHashMap} hash map, making
     * them accessible with only the ID string of the app.
     * @param context The context of the current activity
     * @param appsList The array of accessible apps
     */
    public static HashMap<String,AppInfo> updateAppInfoHashMap(Context context, Application[] appsList) {
        mAppInfoHashMap = new HashMap<String,AppInfo>();

        for (Application app : appsList) {
            AppInfo appInfo = new AppInfo(app);

            appInfo.load(context);
            appInfo.setBgColor(context.getResources().getColor(R.color.app_color_transparent));

            mAppInfoHashMap.put(String.valueOf(appInfo.getId()), appInfo);
        }
        return mAppInfoHashMap;
    }

    /**
     * This function adds the icon and name of an app to a View and returns it
     * @param context The context of the current activity
     * @param targetLayout The layout that the apps are being added too
     * @param appName The name of the App
     * @param appIcon The Icon of the App
     * @return
     */
    private static View addContentToView(Context context, LinearLayout targetLayout, String appName, Drawable appIcon){
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View targetView = inflater.inflate(R.layout.apps, targetLayout, false);

        ImageView appIconView = (ImageView) targetView.findViewById(R.id.app_icon);
        TextView appTextView = (TextView) targetView.findViewById(R.id.app_text);

        appTextView.setText(appName);
        appIconView.setImageDrawable(appIcon);

        return targetView;
    }

    /**
     * The main function for creating an AppImageView to be added to a targetlayout with apps in it.
     * includes information about the onClickListener, the Application and the User
     * @param context The context of the current activity
     * @param appInfo The appinfo instance of the Application to be added
     * @param targetLayout The layout we want to add the AppImageView to.
     * @return
     */
    protected static AppImageView createAppImageView(Context context, final Profile currentUser, final Profile guardian, AppInfo appInfo, LinearLayout targetLayout, View.OnClickListener listener) {

        AppImageView appImageView = new AppImageView(context);
        View appView = addContentToView(context, targetLayout, appInfo.getName(), appInfo.getIconImage());

        setAppBackground(appView, appInfo.getBgColor());

        /**Note that createBitmapFromLayoutWithText is used when the icons are being resized in LauncherSettings
        and is thus a part of the SettingsUtility and not AppViewCreationUtility.*/
        appImageView.setImageBitmap(SettingsUtility.createBitmapFromLayoutWithText(context, appView, Constants.APP_ICON_DIMENSION_DEF, Constants.APP_ICON_DIMENSION_DEF));
        appImageView.setTag(String.valueOf(appInfo.getApp().getId()));
        appImageView.setOnDragListener(new GAppDragger());

        if(listener == null)
        {
            appImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.press_app);
                        v.startAnimation(animation);
                    } catch (NullPointerException e){
                        // could not get context, no animation.
                    }
                    AppInfo app = mAppInfoHashMap.get((String) v.getTag());
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName(app.getPackage(), app.getActivity()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    if(currentUser.getRole() == Profile.Roles.CHILD)
                        intent.putExtra(Constants.CHILD_ID, currentUser.getId());
                    else
                        intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);

                    intent.putExtra(Constants.GUARDIAN_ID, guardian.getId());
                    intent.putExtra(Constants.APP_COLOR, app.getBgColor());
                    intent.putExtra(Constants.APP_PACKAGE_NAME, app.getPackage());
                    intent.putExtra(Constants.APP_ACTIVITY_NAME, app.getActivity());

                    // Verify the intent will resolve to at least one activity
                    LauncherUtility.secureStartActivity(v.getContext(), intent);
                }
            });

        }
        else
        {
            appImageView.setOnClickListener(listener);
        }

        return appImageView;
    }

    /**
     * Sets the background of the app.
     * @param wrapperView The view the app is located inside.
     * @param backgroundColor The color to use for the background.
     */
    private static void setAppBackground(View wrapperView, int backgroundColor) {
        LinearLayout appViewLayout = (LinearLayout) wrapperView.findViewById(R.id.app_bg);

        RoundRectShape roundRect = new RoundRectShape( new float[] {15,15, 15,15, 15,15, 15,15}, new RectF(), null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRect);

        shapeDrawable.getPaint().setColor(backgroundColor);

        appViewLayout.setBackgroundDrawable(shapeDrawable);
    }

}
