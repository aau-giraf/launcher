package dk.aau.cs.giraf.launcher.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.SettingsActivity;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.widgets.AppImageView;
import dk.aau.cs.giraf.models.core.Application;
import dk.aau.cs.giraf.models.core.User;
import dk.aau.cs.giraf.models.core.authentication.Role;
import dk.aau.cs.giraf.utilities.IntentConstants;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * This class is used for creation of AppImageViews containing infomation about the application
 * that the AppImageView contains.
 */
public class AppViewCreationUtility {

    //private static HashMap<String, AppInfo> mAppInfoHashMap;

    /**
     * Loads the AppInfo object of app from the list, into the {@code mAppInfoHashMap} hash map, making
     * them accessible with only the ID string of the app.
     * synchronized: Because mAppInfoHashMap is used on GUI thread and in LoadApplicationTask
     *
     * @param context  The context of the current activity
     * @param appsList The array of accessible apps
     */
    public static synchronized ArrayList<AppInfo> updateAppInfoList(final Context context,
                                                                    final Application[] appsList)
    {
        final ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();

        for (Application app : appsList) {
            AppInfo appInfo = new AppInfo(app);

            appInfo.loadIcon(context);
            //appInfo.setBgColor(context.getResources().getColor(R.color.app_color_transparent));

            appInfoList.add(appInfo);
        }

        return appInfoList;
    }

    /**
     * This function adds the icon and name of an app to a View and returns it.
     *
     * @param context      The context of the current activity
     * @param targetLayout The layout that the apps are being added too
     * @param appName      The name of the App
     * @param appIcon      The Icon of the App
     * @return targetView
     */
    private static View addContentToView(Context context, GridLayout targetLayout, String appName, Drawable appIcon) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View targetView = inflater.inflate(R.layout.apps_container, targetLayout, false);

        ImageView appIconView = (ImageView) targetView.findViewById(R.id.example_grid_layout);
        TextView appTextView = (TextView) targetView.findViewById(R.id.app_text);

        appTextView.setText(appName);
        appIconView.setImageDrawable(appIcon);

        return targetView;
    }

    /**
     * The main function for creating an AppImageView to be added to a targetlayout with apps in it.
     * includes information about the onClickListener, the Application and the User
     *
     * @param context      The context of the current activity
     * @param appInfo      The appinfo instance of the Application to be added
     * @param targetLayout The layout we want to add the AppImageView to.
     * @return AppImageView
     */
    public static AppImageView createAppImageView(
        final Context context,
        final User currentUser,
        final AppInfo appInfo,
        GridLayout targetLayout,
        View.OnClickListener listener)
    {

        final AppImageView appImageView = new AppImageView(context, appInfo);
        final View appView = addContentToView(context, targetLayout, appInfo.getName(), appInfo.getIconImage());

        setAppBackground(appView, appInfo.getBgColor());

        appImageView.setImageBitmap(createBitmapFromLayoutWithText(context, appView,
            Constants.APP_ICON_DIMENSION_DEF, Constants.APP_ICON_DIMENSION_DEF));
        appImageView.setTag(String.valueOf(appInfo.getApp().getId()));
        //appImageView.setOnDragListener(new GAppDragger());

        if (listener == null) {
            appImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                    if (view.getContext() != null) {
                        Animation animation = AnimationUtils.loadAnimation(view.getContext(),
                            R.anim.app_pressed_animation);
                        if (animation != null) {
                            view.startAnimation(animation);
                        }
                    }

                    AppInfo appInfo;

                    /*
                    * This block is synchronized on the AppViewCreationUtility class to prevent
                    * race conditions where mAppInfoHashMap is reinitialized in the background with
                    * updateAppInfoList
                    */

                    /*synchronized (AppViewCreationUtility.class) {
                        appInfo = mAppInfoHashMap.get(view.getTag());
                    }*/

                    appInfo = ((AppImageView) view).appInfo;

                    if (appInfo.getPackage().isEmpty()) {
                        if (!currentUser.isRole(Role.User)) {
                            Constants.offlineNotify.show(((FragmentActivity) context).getSupportFragmentManager(),
                                "DIALOG_TAG");
                        }
                    } else if (appInfo.getPackage().equals(Constants.ADD_APP_ICON_FAKE_PACKAGE_NAME)) {
                        Intent intent = new Intent(context, SettingsActivity.class);
                        intent.putExtra(Constants.ENTER_ADD_APP_MANAGER_BOOL, true);
                        intent.putExtra(IntentConstants.CURRENT_USER, currentUser);
                        LauncherUtility.secureStartActivity(view.getContext(), intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);

                        intent.setComponent(new ComponentName(appInfo.getPackage(), appInfo.getActivity()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                        intent.putExtra(IntentConstants.CURRENT_USER, currentUser);
                        intent.putExtra(Constants.APP_COLOR, appInfo.getBgColor());
                        intent.putExtra(Constants.APP_PACKAGE_NAME, appInfo.getPackage());
                        intent.putExtra(Constants.APP_ACTIVITY_NAME, appInfo.getActivity());
                        intent.putExtra(Constants.STARTED_BY, Constants.LAUNCHER_TAG);
                        // Verify the intent will resolve to at least one activity
                        LauncherUtility.secureStartActivity(view.getContext(), intent);
                    }
                }
            });

        } else {
            appImageView.setOnClickListener(listener);
        }

        return appImageView;
    }

    /**
     * Sets the background of the app.
     *
     * @param wrapperView     The view the app is located inside.
     * @param backgroundColor The color to use for the background.
     */
    private static void setAppBackground(View wrapperView, int backgroundColor) {
        LinearLayout appViewLayout = (LinearLayout) wrapperView.findViewById(R.id.app_bg);

        RoundRectShape roundRect = new RoundRectShape(new float[] {15, 15, 15, 15, 15, 15, 15, 15}, new RectF(), null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRect);

        shapeDrawable.getPaint().setColor(backgroundColor);

        appViewLayout.setBackgroundDrawable(shapeDrawable);
    }

    /**
     * This function create a Bitmap to put into the AppImageView.
     * The Bitmap is scaled to be a certain size when it is returned.
     *
     * @param context    The context of the current activity.
     * @param view       The AppImageView that the bitmap should be inserted into.
     * @param widthInDp  The width of the Bitmap in density pixels.
     * @param heightInDp The height of the bitmap in density pixels.
     * @return the final bitmap of the application to be inserted into the AppImageView.
     */
    private static Bitmap createBitmapFromLayoutWithText(Context context, View view, int widthInDp, int heightInDp) {
        int width = LauncherUtility.intToDp(context, widthInDp);
        int height = LauncherUtility.intToDp(context, heightInDp);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
            view.getMeasuredHeight(),
            Bitmap.Config.ARGB_8888);
        //Create a canvas with the specified bitmap to draw into
        Canvas canvas = new Canvas(bitmap);

        //Render this view (and all of its children) to the given Canvas
        view.draw(canvas);
        view.setVisibility(View.GONE);
        return bitmap;
    }

}
