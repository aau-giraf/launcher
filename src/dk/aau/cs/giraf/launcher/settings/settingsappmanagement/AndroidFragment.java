package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.AppComparator;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Created by Vagner on 01-05-14.
 */
public class AndroidFragment extends AppContainerFragment {
    private SharedPreferences preferences;
    private Set<String> selectedApps;
    private Profile currentUser;
    private HashMap<String, AppInfo> appInfos;
    private LoadAndroidApplicationTask loadApplicationsTask;
    AndroidAppsFragmentInterface mCallback; // Callback to containing Activity implementing the SettingsListFragmentListener interface

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppImageView appImageView = (AppImageView) v;
            appImageView.toggle();

            if (selectedApps == null)
                selectedApps = new HashSet<String>();

            AppInfo app = appInfos.get((String) v.getTag());
            String activityName = app.getActivity();

            if (selectedApps.contains(activityName)){
                selectedApps.remove(activityName);
                Log.d(Constants.ERROR_TAG, "Removed '" + activityName + "' to list: " + selectedApps.size());
            }
            else{
                selectedApps.add(activityName);
                Log.d(Constants.ERROR_TAG, "Added '" + activityName + "' to list: " + selectedApps.size());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        context = getActivity();
        appView = (LinearLayout) view.findViewById(R.id.appContainer);
        currentUser = mCallback.getSelectedProfile();
        preferences = LauncherUtility.getSharedPreferencesForCurrentUser(getActivity(), currentUser);
        selectedApps = preferences.getStringSet(getString(R.string.selected_android_apps_key), new HashSet<String>());

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (AndroidAppsFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AndroidAppsFragmentInterface");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (selectedApps == null)
            selectedApps = new HashSet<String>();

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.selected_android_apps_key)).commit(); // Remove to ensure that the new set is written to file.
        editor.putStringSet(getString(R.string.selected_android_apps_key), selectedApps);
        editor.apply();

        loadApplicationsTask.cancel(true);
    }

    @Override
    protected void reloadApplications() {
        super.reloadApplications();
        loadApplications();
    }

    @Override
    public void loadApplications()
    {
        if (loadedApps == null || loadedApps.size() != apps.size()){
           loadApplicationsTask = new LoadAndroidApplicationTask(context, currentUser, null, appView, 110, listener);
           loadApplicationsTask.execute();
        }

    }

    class LoadAndroidApplicationTask extends LoadApplicationTask {

        public LoadAndroidApplicationTask(Context context, Profile currentUser, Profile guardian, LinearLayout targetLayout, int iconSize, View.OnClickListener onClickListener) {
            super(context, currentUser, guardian, targetLayout, iconSize, onClickListener);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(Constants.ERROR_TAG, "Thread says hello");
        }

        @Override
        protected HashMap<String, AppInfo> doInBackground(Application... applications) {
            Log.d(Constants.ERROR_TAG, "Thread says working");
            applications = LauncherUtility.getAndroidApplicationList(context, "dk.aau.cs.giraf").toArray(applications);
            super.doInBackground(applications);
            appInfos = LauncherUtility.updateAppInfoHashMap(context, (List<Application>) apps);
            //Remember that the apps have been added, so they are not added again by the listener
            //List<ResolveInfo> sortedApps = (List<ResolveInfo>) apps;
            //Collections.sort(sortedApps, new AppComparator(context));

            //LoadApplicationTask loadAndroidApplicationTask = new LoadApplicationTask(context, currentUser, null, targetLayout, 110, listener);

            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, AppInfo> appInfos) {
            super.onPostExecute(appInfos);
            Log.d(Constants.ERROR_TAG, "Thread says bye");
        }
    }
}
