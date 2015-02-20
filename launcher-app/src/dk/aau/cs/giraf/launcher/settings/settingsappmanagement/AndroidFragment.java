package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.MainActivity;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * This is the Fragment used to show the available Android apps installed on the device.
 * The user can select or deselect each app by pressing it, handled in the OnClickListener listener
 */
public class AndroidFragment extends AppContainerFragment {
    private Timer appsUpdater;
    private SharedPreferences preferences;
    private Set<String> selectedApps;
    private ArrayList<AppInfo> appInfos;
    private LoadAndroidApplicationTask loadApplicationsTask;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Because we are dealing with a Fragment, OnCreateView is where most of the variables are set.
     * The context and the currentUser is set by the superclass.
     *
     * @param inflater           The inflater (Android takes care of this)
     * @param container          The container, the ViewGroup, that the fragment should be inflate in.
     * @param savedInstanceState The previously saved instancestate
     * @return the inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);

        appView = (ViewPager) view.findViewById(R.id.appsViewPager);
        preferences = LauncherUtility.getSharedPreferencesForCurrentUser(getActivity(), currentUser);
        selectedApps = preferences.getStringSet(getString(R.string.selected_android_apps_key), new HashSet<String>());


        return view;
    }

    /**
     * We override onResume to make the observer start observing apps.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (haveAppsBeenAdded) {
            startObservingApps();
        }



        reloadApplications();
    }

    /**
     * Handles what happens when the fragment is paused
     * It stops observing apps, so it doesnt try an update while paused.
     * It removes the file containing the previous settings for which Android apps are connected to the user
     * Subsequently, it inputs a new file, containing the new settings for the apps saved to a user.
     * Finally, if the fragment is still loadapplications in the ASyncTask, it cancels the task
     */
    @Override
    public void onPause() {
        super.onPause();
        if (selectedApps == null)
            selectedApps = new HashSet<String>();

        if (appsUpdater != null) {
            appsUpdater.cancel();
            Log.d(Constants.ERROR_TAG, "Applications are no longer observed.");
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.selected_android_apps_key)).commit(); // Remove to ensure that the new set is written to file.
        editor.putStringSet(getString(R.string.selected_android_apps_key), selectedApps);
        editor.apply();

        loadApplicationsTask.cancel(true);

        appView.setAdapter(null);
    }

    /**
     * This function reloads all the applications into the view.
     *
     * @see dk.aau.cs.giraf.launcher.helper.LoadApplicationTask to see what the superclass does.
     */
    @Override
    protected void reloadApplications() {
        super.reloadApplications();
        loadApplications();
    }

    /**
     * Loads applications into the appview container if:
     * - the currently loadedapps list is null OR
     * - the size of the current loadedapps list is not equal to the list of all apps that should be loaded.
     * the superclass merely exists for derived classes to overwrite it and is empty
     * Keep in mind that this version of loadapplication uses LoadAndroidApplicationTask,
     * while GirafFragment uses LoadGirafApplicationTask, which is why it must be overridden
     */
    @Override
    public void loadApplications() {
        loadApplicationsTask = new LoadAndroidApplicationTask(getActivity(), currentUser, null, appView, 110, listener);
        loadApplicationsTask.execute();

    }

    /**
     * This function sets the global variable listener.
     * The listener is the OnClickListener that all the AppImageViews created need to implement to make them
     * selectable and deselectable by the user.
     */
     @Override
     void setListeners() {
        super.listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppImageView appImageView = (AppImageView) v;
                appImageView.toggle();

                if (selectedApps == null)
                    selectedApps = new HashSet<String>();

                AppInfo app = appImageView.appInfo;
                String activityName = app.getActivity();

                /** If the user had previously selected the app, removed it from the list of selected apps
                 * otherwise add it to the list of selected apps.*/
                if (selectedApps.contains(activityName)) {
                    selectedApps.remove(activityName);
                    Log.d(Constants.ERROR_TAG, "Removed '" + activityName + "' to list: " + selectedApps.size());
                } else {
                    selectedApps.add(activityName);
                    Log.d(Constants.ERROR_TAG, "Added '" + activityName + "' to list: " + selectedApps.size());
                }
            }
        };
    }

    /**
     * Starts a timer that looks for updates in the set of available applications every 5 seconds.
     */
    private void startObservingApps() {
        appsUpdater = new Timer();
        AppsObserver timerTask = new AppsObserver();

        try {
            appsUpdater.scheduleAtFixedRate(timerTask, 5000, 5000);
        } catch (IllegalStateException e) {
            Log.e(Constants.ERROR_TAG, "Timer was already canceled:" + e.getMessage());
        }

        Log.d(Constants.ERROR_TAG, "Applications are being observed.");
    }


    /**
     * This class carries out all the work of populating the appView with clickable applications.
     * It inherits from LoadApplicationTask, which does most of the work.
     * However, since there are some special things that need to be handled in the case of Android applications,
     * we must inherit the class, override it's methods and do what we need to do in addition to the superclass
     */
    private class LoadAndroidApplicationTask extends LoadApplicationTask {

        /**
         * The contructor of the class
         *
         * @param context         The context of the current activity
         * @param currentUser     The current user (if the current user is a guardian, this is set to null)
         * @param guardian        The guardian of the current user (or just the current user, if the user is a guardian)
         * @param appsViewPager   The layout to be populated with AppImageViews
         * @param iconSize        The size the icons should have
         * @param onClickListener the onClickListener that each created app should have. In this case we feed it the global variable listener
         */
        public LoadAndroidApplicationTask(Context context, Profile currentUser, Profile guardian, ViewPager appsViewPager, int iconSize, View.OnClickListener onClickListener) {
            super(context, currentUser, guardian, appsViewPager, iconSize, onClickListener);
        }

        /**
         * We override onPreExecute to cancel the AppObserver if it is running
         */
        @Override
        protected void onPreExecute() {
            if (appsUpdater != null)
                appsUpdater.cancel();

            super.onPreExecute();
        }

        @Override
        public android.support.v4.app.FragmentManager getFragmentMangerForAppsFragmentAdapter()
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return AndroidFragment.this.getChildFragmentManager();
            } else {
                return AndroidFragment.this.getFragmentManager();
            }
        }

        /**
         * This method needs to be overridden since we need to inform the superclass of exactly which apps should be generated.
         * In this case it is Android applications only.
         *
         * @param applications the applications that the task should generate AppImageViews for
         * @return The Hashmap of AppInfos that describe the added applications.
         */
        @Override
        protected ArrayList<AppInfo> doInBackground(Application... applications) {
            applications = ApplicationControlUtility.getAndroidAppsOnDeviceAsApplicationList(context).toArray(applications);
            appInfos = super.doInBackground(applications);

            return appInfos;
        }

        /**
         * Once we have loaded applications, we start observing for new apps
         */
        @Override
        protected void onPostExecute(ArrayList<AppInfo> appInfos) {
            super.onPostExecute(appInfos);
            loadedApps = appInfos;
            startObservingApps();
            haveAppsBeenAdded = true;
        }
    }

    /**
     * Task for observing if the set of available apps has changed.
     * Is only instantiated after apps have been loaded the first time.
     *
     * @see dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AndroidFragment#loadApplications()
     */
    private class AppsObserver extends TimerTask {

        @Override
        public void run() {
            apps = ApplicationControlUtility.getAndroidAppsOnDeviceAsApplicationList(getActivity());
            if (loadedApps == null || loadedApps.size() != apps.size()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadApplications();
                    }
                });
            }
            Log.d(Constants.ERROR_TAG, "Applications checked");
        }

    }
}
