package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.settings.SettingsActivity;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileApplicationController;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileApplication;

public class LoadApplicationTask extends AsyncTask<Application, View, HashMap<String, AppInfo>> {

    protected Profile currentUser;
    protected Profile guardian;
    protected Context context;
    protected LinearLayout targetLayout;
    protected int iconSize;
    protected View.OnClickListener onClickListener;
    protected List<LinearLayout> appRowsToAdd;
    protected Set<String> selectedApps;
    protected ProgressBar progressbar;

    public LoadApplicationTask(Context context, Profile currentUser, Profile guardian, LinearLayout targetLayout, int iconSize, View.OnClickListener onClickListener){
        this.context = context;
        this.currentUser = currentUser;
        this.guardian = guardian;
        this.targetLayout = targetLayout;
        this.iconSize = LauncherUtility.intToDP(context, iconSize);
        this.onClickListener = onClickListener;
        appRowsToAdd = new ArrayList<LinearLayout>();
    }

    @Override
    protected void onPreExecute() {
        Log.d(Constants.ERROR_TAG, "Thread says hello");
        hideNoAppsMessage();
        progressbar = new ProgressBar(context);
        progressbar.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100,100);
        params.gravity = Gravity.CENTER;
        progressbar.setLayoutParams(params);
        progressbar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progressbar));
        ViewGroup parent = getProgressBarParent();

        parent.addView(progressbar);

        targetLayout.removeAllViews();
    }

    @Override
    protected HashMap<String, AppInfo> doInBackground(Application... applications) {
        Log.d(Constants.ERROR_TAG, "Thread says working");
        SharedPreferences preferences = LauncherUtility.getSharedPreferencesForCurrentUser(context, currentUser);
        selectedApps = preferences.getStringSet(context.getResources().getString(R.string.selected_android_apps_key), new HashSet<String>());

        HashMap<String, AppInfo> appInfoHash = new HashMap<String, AppInfo>();
        if (applications != null && applications.length != 0) {
            //Fill AppInfo hash map with AppInfo objects for each app
            if (currentUser == null)
                currentUser = LauncherUtility.getCurrentUser(context);

            appInfoHash = AppViewCreationUtility.updateAppInfoHashMap(context, applications);
            List<AppInfo> appInfoList = new ArrayList<AppInfo>(appInfoHash.values());
            Collections.sort(appInfoList, new AppComparator(context));

            int containerWidth = ((ScrollView) targetLayout.getParent()).getWidth();
            int containerHeight = ((ScrollView) targetLayout.getParent()).getHeight();
            //If we are in portrait swap width and height
            if (containerHeight > containerWidth){
                int temp = containerWidth;
                containerWidth = containerHeight;
                containerHeight = temp;
                Log.d(Constants.ERROR_TAG, "Portrait mode detected. Width and height swapped.");
            }

            //Calculate how many apps the screen can fit on each row, and how much space is available for horizontal padding
            int appsPrRow = getAmountOfAppsWithinBounds(containerWidth, iconSize);

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int appsPrColumn = getAmountOfAppsWithinBounds(containerHeight, iconSize);
            int paddingHeight = getLayoutPadding(containerHeight, appsPrColumn, iconSize);

            //Add the first row to the container
            LinearLayout currentAppRow = createNewRow(appsPrRow, paddingHeight, true);
            appRowsToAdd.add(currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (AppInfo appInfo : appInfoList) {
                if (currentAppRow.getChildCount() == appsPrRow) {
                    currentAppRow = createNewRow(appsPrRow,paddingHeight,false);
                    appRowsToAdd.add(currentAppRow);
                }

                AppImageView newAppView = AppViewCreationUtility.createAppImageView(context, currentUser, guardian, appInfo, targetLayout, onClickListener);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.setMargins(2,2,2,2);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                currentAppRow.addView(newAppView);
                if (isCancelled()){
                    break;
                }
            }

            //If last row is not full, fill it with empty elements, to get the icon alignment right
            int appsInLastRow = (appInfoList.size() % appsPrRow);
            if (appsInLastRow > 0) {
                while (appsInLastRow < appsPrRow){
                    AppImageView newAppView = new AppImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                    params.setMargins(2,2,2,2);
                    params.weight = 1f;
                    newAppView.setLayoutParams(params);
                    newAppView.setTag(Constants.NO_APP_TAG);
                    currentAppRow.addView(newAppView);
                    appsInLastRow++;
                }
            }


        } else {
            // show no apps available message
            Log.e(Constants.ERROR_TAG, "App list is null");
        }
        markApplications(appInfoHash);

        return appInfoHash;
    }

    @Override
    protected void onPostExecute(HashMap<String, AppInfo> appInfos) {
        Log.d(Constants.ERROR_TAG, "Thread says bye");

        progressbar.setVisibility(View.GONE);

        try {
            if(appRowsToAdd.size() > 0)
            {
                hideNoAppsMessage();

                for(LinearLayout row : appRowsToAdd){
                    targetLayout.addView(row);
                }
            }
            else
            {
                showNoAppsMessage();
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        removeStrayProgressbars();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if(progressbar != null)
            progressbar.setVisibility(View.GONE);
    }

    private ViewGroup getProgressBarParent()
    {
        ViewGroup parent = (ViewGroup)targetLayout.getParent();
        while (parent instanceof ScrollView)
            parent = (ViewGroup) parent.getParent();

        return parent;
    }
    /**
     * Check for stray progressbars still running and remove them if needed.
     * This must be done this way, since
     */
    private void removeStrayProgressbars()
    {
        ViewGroup parent = getProgressBarParent();

        ArrayList<Integer> delete = new ArrayList<Integer>();
        for(int i = 0; i < parent.getChildCount();i++)
        {
            if(parent.getChildAt(i) instanceof ProgressBar)
            {
                delete.add(i);
            }
        }

        if(delete.size() > 0){
            for(int i = delete.size()-1; i >= 0 ;i--)
            {
                parent.removeViewAt(delete.get(i));
            }
        }
    }

    private void hideNoAppsMessage() {
        if(context instanceof SettingsActivity)
            ((Activity) context).findViewById(R.id.no_apps_textview).setVisibility(View.GONE);
        else
            ((Activity) context).findViewById(R.id.noAppsMessage).setVisibility(View.GONE);
    }

    private void showNoAppsMessage() {
        if(context instanceof SettingsActivity)
            ((Activity) context).findViewById(R.id.no_apps_textview).setVisibility(View.VISIBLE);
        else
            ((Activity) context).findViewById(R.id.noAppsMessage).setVisibility(View.VISIBLE);
    }

    private boolean UserHasGirafApplicationInView(ProfileApplicationController pac, Application app, Profile user){
        ProfileApplication thisPA = pac.getProfileApplicationByProfileIdAndApplicationId(app,user);

        return thisPA != null;
    }

    private void markApplications(final HashMap<String, AppInfo> appInfos){
        if (context instanceof SettingsActivity) {
            final ProfileApplicationController pac = new ProfileApplicationController(context);
            for(final LinearLayout row : appRowsToAdd){
                for (int j = 0; j < row.getChildCount(); j++) {
                    AppImageView appImageView = (AppImageView) row.getChildAt(j);
                    if (appImageView.getTag() != Constants.NO_APP_TAG) {
                        AppInfo app = null;
                        try {
                            app = appInfos.get(appImageView.getTag().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (app != null && (UserHasGirafApplicationInView(pac, app.getApp(), currentUser) || selectedApps.contains(app.getActivity()))) {
                            appImageView.setChecked(true);
                        }
                    }
                }
            }
        }
    }

    private LinearLayout createNewRow(int appsPrRow, int paddingHeight, boolean isFirstRow){
        LinearLayout newAppRow = new LinearLayout(context);
        newAppRow = new LinearLayout(context);
        newAppRow.setWeightSum(appsPrRow);
        newAppRow.setOrientation(LinearLayout.HORIZONTAL);
        if(!isFirstRow)
            newAppRow.setPadding(0, 0, 0, paddingHeight);
        else
            newAppRow.setPadding(0, paddingHeight, 0, paddingHeight);

        newAppRow.setLayoutParams(targetLayout.getLayoutParams());

        return newAppRow;
    }

    protected static int getAmountOfAppsWithinBounds(int containerSize, int iconSize) {
        return containerSize / iconSize;
    }

    protected static int getLayoutPadding(int containerSize, int appsPrRow, int iconSize) {
        return (containerSize % iconSize) / (appsPrRow + 1);
    }
}
