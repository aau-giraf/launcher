package dk.aau.cs.giraf.launcher.helper;

import android.app.ActionBar;
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
import android.widget.TextView;

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
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileApplicationController;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileApplication;

public class LoadApplicationTask extends AsyncTask<Application, View, HashMap<String, AppInfo>> {

    private Profile currentUser;
    private Profile guardian;
    private Context context;
    private LinearLayout targetLayout;
    private int iconSize;
    private View.OnClickListener onClickListener;
    private List<LinearLayout> appRowsToAdd;
    private Set<String> selectedApps;
    private ProgressBar progressbar;

    public LoadApplicationTask(Context context, Profile currentUser, Profile guardian, LinearLayout targetLayout, int iconSize, View.OnClickListener onClickListener){
        this.context = context;
        this.currentUser = currentUser;
        this.guardian = guardian;
        this.targetLayout = targetLayout;
        this.iconSize = iconSize;
        this.onClickListener = onClickListener;
        appRowsToAdd = new ArrayList<LinearLayout>();
    }

    @Override
    protected void onPreExecute() {

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressbar = new ProgressBar(context);
                progressbar.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100,100);
                params.gravity = Gravity.CENTER;
                progressbar.setLayoutParams(params);
                progressbar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progressbar));
                ViewGroup parent = (ViewGroup)targetLayout.getParent();
                while (parent instanceof ScrollView)
                    parent = (ViewGroup) parent.getParent();

                parent.addView(progressbar);
            }
        });


        targetLayout.removeAllViews();
    }

    @Override
    protected HashMap<String, AppInfo> doInBackground(Application... applications) {
        SharedPreferences preferences = LauncherUtility.getSharedPreferencesForCurrentUser(context, currentUser);
        selectedApps = preferences.getStringSet(context.getResources().getString(R.string.selected_android_apps_key), new HashSet<String>());

        HashMap<String, AppInfo> appInfoHash = new HashMap<String, AppInfo>();
        if (applications != null && applications.length != 0) {
            //Fill AppInfo hash map with AppInfo objects for each app
            if (currentUser == null)
                currentUser = LauncherUtility.getCurrentUser(context);

            appInfoHash = LauncherUtility.loadAppInfos(context, applications, currentUser);
            List<AppInfo> appInfos = new ArrayList<AppInfo>(appInfoHash.values());
            Collections.sort(appInfos, new AppComparator(context));

            int containerWidth = ((ScrollView) targetLayout.getParent()).getWidth();
            int containerHeight = ((ScrollView) targetLayout.getParent()).getHeight();
            // if we are in portrait swap width and height
            if (containerHeight > containerWidth){
                int temp = containerWidth;
                containerWidth = containerHeight;
                containerHeight = temp;
                Log.d(Constants.ERROR_TAG, "Portrait mode detected. Width and height swapped.");
            }

            //Calculate how many apps the screen can fit on each row, and how much space is available for horizontal padding
            int appsPrRow = LauncherUtility.getAmountOfAppsWithinBounds(containerWidth, iconSize);

            if(appInfos.size() % appsPrRow == 0)
            {
                appsPrRow--;
            }

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int appsPrColumn = LauncherUtility.getAmountOfAppsWithinBounds(containerHeight, iconSize);
            int paddingHeight = LauncherUtility.getLayoutPadding(containerHeight, appsPrColumn, iconSize);

            //Add the first row to the container
            LinearLayout currentAppRow = new LinearLayout(context);
            currentAppRow.setWeightSum(appsPrRow);
            currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
            currentAppRow.setPadding(0, paddingHeight, 0, paddingHeight);
            currentAppRow.setLayoutParams(targetLayout.getLayoutParams());
            appRowsToAdd.add(currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (AppInfo appInfo : appInfos) {
                if (currentAppRow.getChildCount() == appsPrRow) {
                    currentAppRow = new LinearLayout(context);
                    currentAppRow.setWeightSum(appsPrRow);
                    currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
                    currentAppRow.setPadding(0, 0, 0, paddingHeight);
                    currentAppRow.setLayoutParams(targetLayout.getLayoutParams());
                    appRowsToAdd.add(currentAppRow);
                }

                AppImageView newAppView = LauncherUtility.createGirafLauncherApp(context, currentUser, guardian, appInfo, targetLayout, onClickListener);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.setMargins(2,2,2,2);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                currentAppRow.addView(newAppView);
                if (isCancelled()){
                    break;
                }
            }

            int appsInLastRow = (applications.length % appsPrRow);

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

        } else {
            // show no apps available message
            Log.e(Constants.ERROR_TAG, "App list is null");
        }
        markApplications(appInfoHash);

        return appInfoHash;
    }

    @Override
    protected void onPostExecute(HashMap<String, AppInfo> appInfos) {
        //appRowsToAdd = new ArrayList<LinearLayout>();

        try {
            if(appRowsToAdd.size() > 0)
            {
                if(context instanceof SettingsActivity)
                    ((Activity) context).findViewById(R.id.no_apps_textview).setVisibility(View.GONE);
                else
                    ((Activity) context).findViewById(R.id.noAppsMessage).setVisibility(View.GONE);

                for(LinearLayout row : appRowsToAdd){
                    targetLayout.addView(row);
                }
            }
            else
            {
                if(context instanceof SettingsActivity)
                    ((Activity) context).findViewById(R.id.no_apps_textview).setVisibility(View.VISIBLE);
                else
                    ((Activity) context).findViewById(R.id.noAppsMessage).setVisibility(View.GONE);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        progressbar.setVisibility(View.GONE);
    }

    private boolean UserHasGirafApplicationInView(ProfileApplicationController pac, Application app, Profile user)
    {
        List<ProfileApplication> profileApplications = pac.getListOfProfileApplicationsByProfileId(user);
        ProfileApplication thisPA = pac.getProfileApplicationByProfileIdAndApplicationId(app,user);

        if(profileApplications.contains(thisPA))
            return true;
        else
            return false;
    }

    private void markApplications(final HashMap<String, AppInfo> appInfos)
    {
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
}
