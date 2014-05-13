package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public class LoadAndroidApplicationTask extends AsyncTask<Application, View, HashMap<String, AppInfo>> {

    private Profile currentUser;
    private Profile guardian;
    private Context context;
    private LinearLayout targetLayout;
    private int iconSize;
    private View.OnClickListener onClickListener;
    private List<LinearLayout> appRowsToAdd;

    public LoadAndroidApplicationTask(Context context, Profile currentUser, Profile guardian, LinearLayout targetLayout, int iconSize, View.OnClickListener onClickListener){
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
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                targetLayout.removeAllViews();
            }
        });
    }

    @Override
    protected HashMap<String, AppInfo> doInBackground(Application... applications) {
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

            //Calculate how many apps the screen can fit vertically on a single screen, and how much space is available for vertical padding
            int appsPrColumn = LauncherUtility.getAmountOfAppsWithinBounds(containerHeight, iconSize);
            int paddingHeight = LauncherUtility.getLayoutPadding(containerHeight, appsPrColumn, iconSize);

            //Add the first row to the container
            LinearLayout currentAppRow = new LinearLayout(context);
            currentAppRow.setWeightSum(appsPrRow);
            currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
            currentAppRow.setPadding(0, paddingHeight, 0, paddingHeight);
            //targetLayout.addView(currentAppRow);
            appRowsToAdd.add(currentAppRow);

            //Insert apps into the container, and add new rows as needed
            for (AppInfo appInfo : appInfos) {
                if (currentAppRow.getChildCount() == appsPrRow) {
                    currentAppRow = new LinearLayout(context);
                    currentAppRow.setWeightSum(appsPrRow);
                    currentAppRow.setOrientation(LinearLayout.HORIZONTAL);
                    currentAppRow.setPadding(0, 0, 0, paddingHeight);
                    //targetLayout.addView(currentAppRow);
                    appRowsToAdd.add(currentAppRow);
                }

                AppImageView newAppView = LauncherUtility.createGirafLauncherApp(context, currentUser, guardian, appInfo, targetLayout, onClickListener);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                newAppView.setScaleX(0.9f);
                newAppView.setScaleY(0.9f);
                currentAppRow.addView(newAppView);
            }

            int appsInLastRow = (applications.length % appsPrRow);
            while (appsInLastRow < appsPrRow){
                AppImageView newAppView = new AppImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.weight = 1f;
                newAppView.setLayoutParams(params);
                newAppView.setScaleX(0.9f);
                newAppView.setScaleY(0.9f);
                currentAppRow.addView(newAppView);
                appsInLastRow++;
            }

        } else {
            // show no apps available message
            Log.e(Constants.ERROR_TAG, "App list is null");
        }

        return appInfoHash;
    }

    @Override
    protected void onPostExecute(HashMap<String, AppInfo> appInfos) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(LinearLayout row : appRowsToAdd){
                    targetLayout.addView(row);
                }
            }
        });
    }
}
