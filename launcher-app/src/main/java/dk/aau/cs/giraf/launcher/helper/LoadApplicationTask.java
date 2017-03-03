package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import dk.aau.cs.giraf.dblib.models.Application;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.launcher.settings.components.ApplicationGridResizer;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This is the main class that loads applications as AppImageViews into a given targetlayout
 * When using this class, you must derive from it, and specify in the derived class exactly
 * what applications you want to generate AppImageViews for.
 * For examples:
 *
 * @see {@link dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AndroidFragment.LoadAndroidApplicationTask}
 * @see {@link dk.aau.cs.giraf.launcher.activities.HomeActivity.LoadHomeActivityApplicationTask}
 */
public abstract class LoadApplicationTask extends AsyncTask<Application, View, ArrayList<AppInfo>> {

    protected Profile currentUser;
    protected final Profile guardian;
    protected final Context context;
    protected final ViewPager appsViewPager;
    protected final View.OnClickListener onClickListener;
    protected boolean offlineMode = false;
    protected boolean includeAddAppIcon = false;

    protected ProgressBar progressbar;

    /**
     * the contructor for the class
     *
     * @param context         The context for the current activity
     * @param currentUser     The user of the current activity. If set to null, the user will be
     *                        found based on the context
     * @param guardian        The guardian of the current user.
     * @param appsViewPager   The layout that the AppImageViews should be put into
     * @param onClickListener The onClickListener attached to each AppImageView.
     *                        These vary depending on the purpose of the layout they are loaded into.
     */
    public LoadApplicationTask(final Context context, final Profile currentUser, final Profile guardian,
                               final ViewPager appsViewPager, final View.OnClickListener onClickListener)
    {
        this.context = context;
        this.currentUser = currentUser;
        this.guardian = guardian;
        this.appsViewPager = appsViewPager;
        this.onClickListener = onClickListener;
    }

    /**
     * the contructor for the class but including a boolean for offlinemode and includeAddAppIcon
     * The "addAppIcon" shows an icon which leads to the settings tab for adding apps
     *
     * @param context           The context for the current activity
     * @param currentUser       The user of the current activity. If set to null,
     *                          the user will be found based on the context
     * @param guardian          The guardian of the current user.
     * @param appsViewPager     The layout that the AppImageViews should be put into
     * @param onClickListener   The onClickListener attached to each AppImageView.
     *                          These vary depending on the purpose of the layout they are loaded into.
     * @param offlineMode       Indicate if the launcher is in offline mode
     * @param includeAddAppIcon Indicate if the addAppIcon should be shown with the apps
     */
    public LoadApplicationTask(final Context context,
                               final Profile currentUser,
                               final Profile guardian,
                               final ViewPager appsViewPager,
                               final View.OnClickListener onClickListener,
                               final boolean offlineMode,
                               final boolean includeAddAppIcon)
    {
        this(context, currentUser, guardian, appsViewPager, onClickListener);
        this.offlineMode = offlineMode;
        this.includeAddAppIcon = includeAddAppIcon;
    }

    /**
     * The tasks that should be carried out before the main task in doInBackground.
     * This function adds a progress bar to the parent of the targetlayout and
     * removes all other views from the targetlayout itself.
     */
    @Override
    protected void onPreExecute() {

        changeVisibilityOfNoAppsMessage(View.GONE);

        ViewGroup parent = getProgressBarParent();
        progressbar = (ProgressBar) parent.findViewById(R.id.ProgressBar);
        progressbar.setVisibility(View.VISIBLE);
    }

    /**
     * This is the main task to be carried out - populating the targetlayout with AppImageViews.
     * AppImageViews are generated from the given ArrayList of Applications
     * Futhermore comments can be found inside the function
     *
     * @param applications The applications that should populate the targetlayout
     * @return a HashMap containing all the apps that there was generated AppImageViews for.
     */
    @Override
    protected ArrayList<AppInfo> doInBackground(Application... applications) {
        ArrayList<AppInfo> appInfoList = new ArrayList<>();
        // Only creates AppImageViews if there actually are applications to generate
        if (applications != null && applications.length != 0) {
            // If the current user is null, find the user based on the context
            if (currentUser == null) {
                currentUser = LauncherUtility.getCurrentUser(context);
            }
            // update the HashMap with information of the apps being generated and sort it
            appInfoList = AppViewCreationUtility.updateAppInfoList(context, applications);
            //If the launcher is in offline mode, some application should not be available
            if (offlineMode) {
                for (AppInfo a : appInfoList) {
                    if (Constants.OFFLINE_INCAPABLE_APPS.contains(a.getPackage())) {
                        Drawable [] layers = new Drawable[2];
                        layers[0] = a.getIconImage().mutate();
                        layers[0].setAlpha(context.getResources().getInteger(R.integer.giraf_disabled_app_alpha));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            layers[1] = context.getDrawable(R.drawable.icon_redcross);
                        } else {
                            layers[1] = context.getResources().getDrawable(R.drawable.icon_redcross);
                        }
                        a.setIconImage(new LayerDrawable(layers));
                        //Hack which makes the application unlaunchable -- queue evil 4chan laugh
                        a.setPackage("");
                    }
                }
            }
            Collections.sort(appInfoList, new AppComparator(context));
        } else {
            // show no apps available message
            Log.e(Constants.ERROR_TAG, "App list is null");
        }
        if (includeAddAppIcon && currentUser.getRole() != Profile.Roles.CHILD ) {
            Application tmpApp = new Application(context.getResources().getString(R.string.add_app_text), "",
                Constants.ADD_APP_ICON_FAKE_PACKAGE_NAME, "", "");
            AppInfo tmpInfo = new AppInfo(tmpApp);
            tmpInfo.setIconImage(context.getResources().getDrawable(R.drawable.ic_apps));

            appInfoList.add(tmpInfo);
        }
        return appInfoList.isEmpty() ? null : appInfoList;

    }

    /**
     * Carries out after the main task has completed in doInBackground.
     * Hides the progressbar, adds the rows in the list of rows to be added to the targetlayout.
     * Also clears the parent of the targetlayout of progressbars
     *
     * @param appInfoList List of appInfor
     */
    @Override
    protected void onPostExecute(ArrayList<AppInfo> appInfoList) {


        progressbar.setVisibility(View.INVISIBLE);

        final int rowsSize = ApplicationGridResizer.getGridRowSize(this.context, currentUser);
        final int columnsSize = ApplicationGridResizer.getGridColumnSize(this.context, currentUser);

        if (appInfoList != null && appInfoList.size() > 0) {
            changeVisibilityOfNoAppsMessage(View.GONE);
        } else {
            changeVisibilityOfNoAppsMessage(View.VISIBLE);
        }

        ((AppsFragmentAdapter) this.appsViewPager.getAdapter()).swapApps(appInfoList, rowsSize, columnsSize);

    }

    public android.support.v4.app.FragmentManager getFragmentMangerForAppsFragmentAdapter() {
        return ((FragmentActivity) context).getSupportFragmentManager();
    }

    /**
     * If the task was cancelled, and the progressbar is not null, hide the progressbar.
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (progressbar != null)
            progressbar.setVisibility(View.GONE);
    }

    /**
     * Gets the correct parent of the targetlayout to add the Progressbar to.
     *
     * @return ViewGroup
     */
    private ViewGroup getProgressBarParent() {

        ViewGroup parent = (ViewGroup) appsViewPager.getParent();

        while (parent instanceof ScrollView) {
            parent = (ViewGroup) parent.getParent();
        }

        return parent;
    }

    /**
     * Sets the visibility of the noAppsText message shown if no apps where added to the targetlayout.
     * Takes into consideration if we are in SettingsActivity or HomeActivity
     *
     * @param visibility Int visibility
     */
    private void changeVisibilityOfNoAppsMessage(int visibility) {

        View noAppsTextView = ((Activity) context).findViewById(R.id.noAppsMessage);
        noAppsTextView.setVisibility(visibility);

    }
}
