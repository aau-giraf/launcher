package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.MainActivity;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.launcher.settings.SettingsActivity;
import dk.aau.cs.giraf.launcher.settings.components.ApplicationGridResizer;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * This is the main class that loads applications as AppImageViews into a given targetlayout
 * When using this class, you must derive from it, and specify in the derived class exactly
 * what applications you want to generate AppImageViews for.
 * For examples:
 *
 * @see dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AndroidFragment.LoadAndroidApplicationTask
 * @see dk.aau.cs.giraf.launcher.settings.settingsappmanagement.GirafFragment.loadGirafApplicationTask
 * @see dk.aau.cs.giraf.launcher.activities.HomeActivity.LoadHomeActivityApplicationTask
 */
public abstract class LoadApplicationTask extends AsyncTask<Application, View, ArrayList <AppInfo>> {

    protected Profile currentUser;
    protected Profile guardian;
    protected Context context;
    protected ViewPager appsViewPager;
    protected View.OnClickListener onClickListener;

    protected Set<String> selectedApps;
    protected ProgressBar progressbar;

    /**
     * the contructor for the class
     *
     * @param context         The context for the current activity
     * @param currentUser     The user of the current activity. If set to null, the user will be found based on the context
     * @param guardian        The guardian of the current user.
     * @param appsViewPager    The layout that the AppImageViews should be put into
     * @param onClickListener The onClickListener attached to each AppImageView. These vary depending on the purpose of the layout they are loaded into.
     */
    public LoadApplicationTask(Context context, Profile currentUser, Profile guardian, ViewPager appsViewPager, View.OnClickListener onClickListener) {
        this.context = context;
        this.currentUser = currentUser;
        this.guardian = guardian;
        this.appsViewPager = appsViewPager;
        this.onClickListener = onClickListener;
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

        /*
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressbar.setLayoutParams(params);
        progressbar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progressbar));
        */

        //parent.addView(progressbar);
        //targetLayout.removeAllViews();
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
    protected ArrayList<AppInfo> doInBackground(Application... applications)
    {
        // Thread starts working and gets the settings for the given user.
        SharedPreferences preferences = LauncherUtility.getSharedPreferencesForCurrentUser(context, currentUser);
        selectedApps = preferences.getStringSet(context.getResources().getString(R.string.selected_android_apps_key), new HashSet<String>());

        // Only creates AppImageViews if there actually are applications to generate
        HashMap<String, AppInfo> appInfoHash = new HashMap<String, AppInfo>();

        if (applications != null && applications.length != 0)
        {
            //if the currentuser was null, find the user based on the context
            if (currentUser == null)
                currentUser = LauncherUtility.getCurrentUser(context);



            //update the HashMap with information of the apps being generated and sort it
            appInfoHash = AppViewCreationUtility.updateAppInfoHashMap(context, applications);

            ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>(appInfoHash.values());
            Collections.sort(appInfoList, new AppComparator(context));

            //Get the width and height of the targetlayout to be populated
            //int containerWidth = ((ScrollView) targetLayout.getParent()).getWidth();
            //int containerHeight = ((ScrollView) targetLayout.getParent()).getHeight();

            //If we are in portrait swap width and height
            /*if (containerHeight > containerWidth) {
                int temp = containerWidth;
                containerWidth = containerHeight;
                containerHeight = temp;
                Log.d(Constants.ERROR_TAG, "Portrait mode detected. Width and height swapped.");
            }
            */

            //Calculate how many apps the screen can fit on each row, and how much space is available for horizontal padding
            /*
            int appsPrRow = getAmountOfAppsWithinBounds(containerWidth, iconSize);

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int appsPrColumn = getAmountOfAppsWithinBounds(containerHeight, iconSize);
            int paddingHeight = getLayoutPadding(containerHeight, appsPrColumn, iconSize);
            */





            /*
            //Add the first row to the list of rows to add to the container
            LinearLayout currentAppRow = createNewRow(appsPrRow, paddingHeight, true);
            appRowsToAdd.add(currentAppRow);

            //Insert apps into the rows, and add new rows as needed
            for (AppInfo appInfo : appInfoList) {
                //If the current row is full, insert the current row into the list of rows to add and create a new row
                if (currentAppRow.getChildCount() == appsPrRow)
                {
                    currentAppRow = createNewRow(appsPrRow, paddingHeight, false);
                    appRowsToAdd.add(currentAppRow);
                }

                //Create a new AppImageView and set its properties
                AppImageView newAppView = AppViewCreationUtility.createAppImageView(context, currentUser, guardian, appInfo, targetLayout, onClickListener);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.setMargins(2, 2, 2, 2);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                currentAppRow.addView(newAppView);
                if (isCancelled()) {
                    break;
                }
            }

            //If last row is not full, fill it with empty elements, to get the icon alignment right
            int appsInLastRow = (appInfoList.size() % appsPrRow);
            if (appsInLastRow > 0) {
                while (appsInLastRow < appsPrRow) {
                    AppImageView newAppView = new AppImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                    params.setMargins(2, 2, 2, 2);
                    params.weight = 1f;
                    newAppView.setLayoutParams(params);
                    newAppView.setTag(Constants.NO_APP_TAG);
                    currentAppRow.addView(newAppView);
                    appsInLastRow++;
                }
            }
            */
            return appInfoList;
        } else {
            // show no apps available message
            Log.e(Constants.ERROR_TAG, "App list is null");
        }
        //mark the applications if they were selected by the user
        //NOTE: This is only relevant for SettingsAcitivty, which markApplications checks for itself.
        //markApplications(appInfoHash);

        return null;
    }

    /**
     * Carries out after the main task has completed in doInBackground.
     * Hides the progressbar, adds the rows in the list of rows to be added to the targetlayout.
     * Also clears the parent of the targetlayout of progressbars
     *
     * @param appInfos
     */
    @Override
    protected void onPostExecute(ArrayList<AppInfo> appInfos) {


        progressbar.setVisibility(View.INVISIBLE);

        final int rowsSize = ApplicationGridResizer.getGridRowSize(this.context, currentUser);
        final int columnsSize = ApplicationGridResizer.getGridColumnSize(this.context, currentUser);

        if (appInfos!= null && appInfos.size() > 0)
        {
            changeVisibilityOfNoAppsMessage(View.GONE);
        }
        else
        {
            changeVisibilityOfNoAppsMessage(View.VISIBLE);
        }

        ((AppsFragmentAdapter)this.appsViewPager.getAdapter()).swapApps(appInfos , rowsSize, columnsSize);

    }

    public android.support.v4.app.FragmentManager getFragmentMangerForAppsFragmentAdapter()
    {
        return ((FragmentActivity)context).getSupportFragmentManager();
    }

    /**
     * If the task was cancelled, and the progressbar is not null, hide the progressbar
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (progressbar != null)
            progressbar.setVisibility(View.GONE);
    }

    /**
     * Gets the correct parent of the targetlayout to add the Progressbar to
     *
     * @return
     */
    private ViewGroup getProgressBarParent() {

        ViewGroup parent = (ViewGroup) appsViewPager.getParent();

        while (parent instanceof ScrollView) {
            parent = (ViewGroup) parent.getParent();
        }

        return parent;
    }

    /**
     * Check for stray progressbars still running and remove them if needed.
     */
    private void removeStrayProgressbars() {
        ViewGroup parent = getProgressBarParent();

        ArrayList<Integer> delete = new ArrayList<Integer>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i) instanceof ProgressBar) {
                delete.add(i);
            }
        }

        if (delete.size() > 0) {
            for (int i = delete.size() - 1; i >= 0; i--) {
                parent.removeViewAt(delete.get(i));
            }
        }
    }

    /**
     * Sets the visibility of the noAppsText message shown if no apps where added to the targetlayout
     * Takes into consideration if we are in SettingsActivity or HomeActivity
     *
     * @param visibility
     */
    private void changeVisibilityOfNoAppsMessage(int visibility) {

        View noAppsTextView = ((Activity) context).findViewById(R.id.noAppsMessage);
        noAppsTextView.setVisibility(visibility);

    }



    /**
     * This function goes through all the applications and marks the relevant ones as selected.
     * The relevant ones are the ones for which a ProfileApplication based on the user and the application exists or
     * the sharedPreferences for the user contains the app.
     * Since this is only relevant for SettingsActivity, the function terminates if the context is an instance of something else.
     *
     * @param appInfos the list of AppInfos that we are checking to be marked
     */
    /*
    private void markApplications(final HashMap<String, AppInfo> appInfos) {
        if (context instanceof SettingsActivity) {
            final ProfileApplicationController pac = new ProfileApplicationController(context);
            for (final LinearLayout row : appRowsToAdd) {
                for (int j = 0; j < row.getChildCount(); j++) {
                    AppImageView appImageView = (AppImageView) row.getChildAt(j);
                    if (appImageView.getTag() != Constants.NO_APP_TAG) {
                        AppInfo app = null;
                        try {
                            app = appInfos.get(appImageView.getTag().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (app != null && (doesProfileApplicationExist(pac, app.getApp(), currentUser) || selectedApps.contains(app.getActivity()))) {
                            appImageView.setChecked(true);
                        }
                    }
                }
            }
        }
    }
    */
    /**
     * This function creates a new row to be added to the targetlayout, with all the properties it needs
     *
     * @param appsPrRow     The amount of apps there can be contained in a row
     * @param paddingHeight The amount of padding that should be added to the row
     * @param isFirstRow    Should be set to true if this is the first row being added, false otherwise
     * @return the row that we are about to add.
     */
    /*
    private LinearLayout createNewRow(int appsPrRow, int paddingHeight, boolean isFirstRow) {
        LinearLayout newAppRow = new LinearLayout(context);
        newAppRow = new LinearLayout(context);
        newAppRow.setWeightSum(appsPrRow);
        newAppRow.setOrientation(LinearLayout.HORIZONTAL);
        if (!isFirstRow)
            newAppRow.setPadding(0, 0, 0, paddingHeight);
        else
            newAppRow.setPadding(0, paddingHeight, 0, paddingHeight);

        newAppRow.setLayoutParams(targetLayout.getLayoutParams());

        return newAppRow;
    }
    */
    /**
     * Returns the amount of applications that can fit within a container
     *
     * @param containerSize The size of the container we fill AppImageViews into
     * @param iconSize      The size of the AppImageViews we fill the container with
     * @return the amount of applications that can fit within a container
     */
    protected static int getAmountOfAppsWithinBounds(int containerSize, int iconSize) {
        return containerSize / iconSize;
    }

    /**
     * This function returns the amount of padding that the row should receive.
     *
     * @param containerSize the size of the row
     * @param appsPrRow     The amount of apps that can be contained in a row
     * @param iconSize      The size of the AppImageViews
     * @return the amount of padding that the row should receive.
     */
    protected static int getLayoutPadding(int containerSize, int appsPrRow, int iconSize) {
        return (containerSize % iconSize) / (appsPrRow + 1);
    }
}
