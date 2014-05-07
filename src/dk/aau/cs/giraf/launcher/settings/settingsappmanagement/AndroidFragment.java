package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.AppComparator;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;

/**
 * Created by Vagner on 01-05-14.
 */
public class AndroidFragment extends AppContainerFragment {
    private SharedPreferences preferences;
    private Set<String> selectedApps;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppImageView appImageView = (AppImageView) v;
            appImageView.toggle();

            if (selectedApps == null)
                selectedApps = new HashSet<String>();

            ResolveInfo app = (ResolveInfo) v.getTag();
            String packageName = app.activityInfo.packageName;

            if (selectedApps.contains(packageName)){
                selectedApps.remove(packageName);
                Log.d(Constants.ERROR_TAG, "Removed '" + app.activityInfo.name + "' to list: " + selectedApps.size());
            }
            else{
                selectedApps.add(packageName);
                Log.d(Constants.ERROR_TAG, "Added '" + app.activityInfo.name + "' to list: " + selectedApps.size());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        context = getActivity();
        apps = LauncherUtility.getApplicationsFromDevice(context, "dk.aau.cs.giraf", false);
        appView = (LinearLayout) view.findViewById(R.id.appContainer);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        selectedApps = preferences.getStringSet(Constants.SELECTED_ANDROID_APPS, new HashSet<String>());

        loadApplications();

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                appView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                reloadApplications();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadApplications();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (selectedApps == null)
            selectedApps = new HashSet<String>();

        preferences.edit().putStringSet(Constants.SELECTED_ANDROID_APPS, selectedApps).commit();
    }

    @Override
    protected void reloadApplications() {
        super.reloadApplications();
        loadApplications();
    }

    @Override
    public void loadApplications()
    {
        Handler loadApplicationsHandler = new Handler();
        loadApplicationsHandler.post(new Runnable() {
            @Override
            public void run() {
                if (loadedApps == null || loadedApps.size() != apps.size()){
                    //Remember that the apps have been added, so they are not added again by the listener
                    List<ResolveInfo> sortedApps = (List<ResolveInfo>) apps;
                    Collections.sort(sortedApps, new AppComparator(context));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            haveAppsBeenAdded = LauncherUtility.loadOtherApplicationsIntoView(context, (List<ResolveInfo>) apps, appView, 110, onClickListener);                     }
                    });
                    loadedApps = apps;
                }
            }
        });
    }
}
