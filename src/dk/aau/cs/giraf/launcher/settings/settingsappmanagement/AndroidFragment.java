package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
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
import dk.aau.cs.giraf.launcher.helper.LoadAndroidApplicationTask;
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
    AndroidAppsFragmentInterface mCallback; // Callback to containing Activity implementing the SettingsListFragmentListener interface

    private View.OnClickListener onClickListener = new View.OnClickListener() {
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
        super.showProgressBar();

        context = getActivity();
        appView = (LinearLayout) view.findViewById(R.id.appContainer);
        currentUser = mCallback.getSelectedProfile();
        preferences = LauncherUtility.getSharedPreferencesForCurrentUser(getActivity(), currentUser);
        selectedApps = preferences.getStringSet(Constants.SELECTED_ANDROID_APPS, new HashSet<String>());

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
        editor.remove(Constants.SELECTED_ANDROID_APPS).commit(); // Remove to ensure that the new set is written to file.
        editor.putStringSet(Constants.SELECTED_ANDROID_APPS, selectedApps);
        editor.apply();
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
           LoadApplicationsTask loadApplicationsTask = new LoadApplicationsTask();
           loadApplicationsTask.execute();
        }

    }

    class LoadApplicationsTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            Log.d(Constants.ERROR_TAG, "Thread says hello");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(Constants.ERROR_TAG, "Thread says working");
            apps = LauncherUtility.getAndroidApplicationList(context, "dk.aau.cs.giraf");
            appInfos = LauncherUtility.loadAppInfos(context, (List<Application>)apps, currentUser);
            //Remember that the apps have been added, so they are not added again by the listener
            List<ResolveInfo> sortedApps = (List<ResolveInfo>) apps;
            Collections.sort(sortedApps, new AppComparator(context));
            LoadAndroidApplicationTask applicationLoader = LauncherUtility.loadGirafApplicationsIntoView(context, currentUser, (List<Application>) apps, appView, 110, onClickListener);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                for (int i = 0; i < appView.getChildCount();i++)
                {
                    LinearLayout thisLayout = (LinearLayout)appView.getChildAt(i);
                    for(int j = 0; j < thisLayout.getChildCount(); j++)
                    {
                        AppImageView appImageView = (AppImageView) thisLayout.getChildAt(j);

                        AppInfo app = null;
                        try
                        {
                            app = appInfos.get(appImageView.getTag());
                            if(app != null && selectedApps.contains(app.getActivity()))
                            {
                                appImageView.setChecked(true);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                loadedApps = apps;
            }
            catch (Exception e){
                e.printStackTrace();
            }

            hideProgressBar();
            Log.d(Constants.ERROR_TAG, "Thread says bye");
        }
    }
}
