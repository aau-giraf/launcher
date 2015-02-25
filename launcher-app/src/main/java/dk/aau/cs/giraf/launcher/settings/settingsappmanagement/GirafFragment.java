package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.MainActivity;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.launcher.settings.components.ApplicationGridResizer;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileApplicationController;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileApplication;

/**
 * This is the Fragment used to show the available Giraf apps installed on the device.
 * The user can select or deselect each app by pressing it, handled in the OnClickListener listener
 */
public class GirafFragment extends AppContainerFragment {

    private Timer appsUpdater;
    private ArrayList<AppInfo> appInfos;
    private loadGirafApplicationTask loadApplicationTask;
    private View.OnClickListener listener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Because we are dealing with a Fragment, OnCreateView is where most of the variables are set.
     * The context is set by the superclass.
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

        final int rowsSize = ApplicationGridResizer.getGridRowSize(getActivity(), currentUser);
        final int columnsSize = ApplicationGridResizer.getGridColumnSize(getActivity(), currentUser);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

            appView.setAdapter(new AppsFragmentAdapter(getChildFragmentManager(), appInfos, rowsSize, columnsSize));
        } else {

            appView.setAdapter(new AppsFragmentAdapter(getFragmentManager(), appInfos, rowsSize, columnsSize));
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
     * If the fragment is still loadapplications in the ASyncTask, it cancels the task
     * It stops observing apps, so it doesnt try an update while paused.
     */
    @Override
    public void onPause() {
        super.onPause();

        if (loadApplicationTask != null) {
            loadApplicationTask.cancel(true);
        }
        if (appsUpdater != null) {
            appsUpdater.cancel();

            Log.d(Constants.ERROR_TAG, "Applications are no longer observed.");
        }
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
     * Keep in mind that this version of loadapplication uses LoadGirafApplicationTask,
     * while AndroidFragment uses LoadAndroidApplicationTask, which is why it must be overridden
     */
    @Override
    public void loadApplications() {
        if (loadedApps == null || loadedApps.size() != apps.size()) {
            //Remember that the apps have been added, so they are not added again by the listener

            loadApplicationTask = new loadGirafApplicationTask(getActivity(), currentUser, null, appView, listener);
            loadApplicationTask.execute();
        }
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
                ProfileApplicationController pac = new ProfileApplicationController(getActivity());
                AppInfo app = appImageView.appInfo;


                if (userHasApplicationInView(pac, app.getApp(), currentUser)) {
                    pac.removeProfileApplicationByProfileAndApplication(app.getApp(), currentUser);
                } else {
                    ProfileApplication pa = new ProfileApplication(currentUser.getId(), app.getApp().getId());
                    pac.insertProfileApplication(pa);
                }
            }
        };
    }

    /**
     * This function attempts to retrive a ProfileApplication based on a user and an application
     * to see if the user has access to the application
     *
     * @param pac  The ProfileApplicationController retrived from the current context
     * @param app  The application we wish to check if the user has access to
     * @param user The user we wish to check for an application
     * @return true if the user has access to the application, false if the user does not.
     */
    private boolean userHasApplicationInView(ProfileApplicationController pac, Application app, Profile user) {
        ProfileApplication thisPA = pac.getProfileApplicationByProfileIdAndApplicationId(app, user);

        return thisPA != null;
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
     * However, since there are some special things that need to be handled in the case of Giraf applications,
     * we must inherit the class, override it's methods and do what we need to do in addition to the superclass
     */
    class loadGirafApplicationTask extends LoadApplicationTask {

        /**
         * The contructor of the class
         *
         * @param context         The context of the current activity
         * @param currentUser     The current user (if the current user is a guardian, this is set to null)
         * @param guardian        The guardian of the current user (or just the current user, if the user is a guardian)
         * @param appsViewPager   The layout to be populated with AppImageViews
         * @param onClickListener the onClickListener that each created app should have. In this case we feed it the global variable listener
         */
        public loadGirafApplicationTask(Context context, Profile currentUser, Profile guardian, ViewPager appsViewPager, View.OnClickListener onClickListener) {
            super(context, currentUser, guardian, appsViewPager, onClickListener);
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

        /**
         * This method needs to be overridden since we need to inform the superclass of exactly which apps should be generated.
         * In this case, it is Giraf applications only
         *
         * @param applications the applications that the task should generate AppImageViews for
         * @return The Hashmap of AppInfos that describe the added applications.
         */
        @Override
        protected ArrayList<AppInfo> doInBackground(Application... applications) {
            apps = ApplicationControlUtility.getGirafAppsOnDeviceButLauncherAsApplicationList(context);
            applications = apps.toArray(applications);
            appInfos = super.doInBackground(applications);

            return appInfos;
        }

        @Override
        public android.support.v4.app.FragmentManager getFragmentMangerForAppsFragmentAdapter() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return GirafFragment.this.getChildFragmentManager();
            } else {
                return GirafFragment.this.getFragmentManager();
            }
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
            apps = ApplicationControlUtility.getGirafAppsOnDeviceButLauncherAsApplicationList(getActivity());
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
