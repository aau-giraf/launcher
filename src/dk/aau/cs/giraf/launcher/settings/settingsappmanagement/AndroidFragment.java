package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * This is the Fragment used to show the available Android apps in the device.
 * The user can select or deselect each app by pressing it, handled in the OnClickListener listener
 */
public class AndroidFragment extends AppContainerFragment {
    private SharedPreferences preferences;
    private Set<String> selectedApps;
    private Profile currentUser;
    private HashMap<String, AppInfo> appInfos;
    private LoadAndroidApplicationTask loadApplicationsTask;
    AppsFragmentInterface mCallback; // Callback to containing Activity implementing the SettingsListFragmentListener interface
    private View.OnClickListener listener;

    /**
     * Because we are dealing with a Fragment, OnCreateView is where most of the variables are set.
     * The context is set by the superclass.
     * @param inflater The inflater (Android takes care of this)
     * @param container The container, the ViewGroup, that the fragment should be inflate in.
     * @param savedInstanceState The previously saved instancestate
     * @return the inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        context = getActivity();
        appView = (LinearLayout) view.findViewById(R.id.appContainer);
        currentUser = mCallback.getSelectedProfile();
        preferences = LauncherUtility.getSharedPreferencesForCurrentUser(getActivity(), currentUser);
        selectedApps = preferences.getStringSet(getString(R.string.selected_android_apps_key), new HashSet<String>());
        setListeners();

        return view;
    }

    /**
     * Once the view has been created, we start loading applications into the view with a call to reloadApplications.
     * This call is done inside the ViewTreeObserver, since the Observer ensures that the view has been fully inflated.
     * If we attempt to call reloadApplications without the Observer, the view is not inflated yet.
     * This means that the width of the view, which we use to see how many apps we can fill into a row, is 0.
     * @param view The view that has been created
     * @param savedInstanceState The previously saved instancestate.
     */
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

    /**
     * This makes sure that the container activity has implemented the callback interface. If not, it throws an exception.
     * The callback interface is needed to reload applications when a new user is selected.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (AppsFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AppsFragmentInterface");
        }
    }

    /**
     * Handles what happens when the fragment is paused
     * It removes the file containing the previous settings for which Android apps are connected to the user
     * Subsequently, it inputs a new file, containing the new settings for the apps saved to a user.
     * Finally, if the fragment is still loadapplications in the ASyncTask, it cancels the task
     */
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

    /**
     * This function reloads all the applications into the view.
     * @see dk.aau.cs.giraf.launcher.helper.LoadApplicationTask to see what the superclass does.
     */
    @Override
    protected void reloadApplications() {
        super.reloadApplications();
        loadApplications();
    }

    /**
     * Loads applications into the appview container if:
     *  - the currently loadedapps list is null OR
     *  - the size of the current loadedapps list is not equal to the list of all apps that should be loaded.
     */
    @Override
    public void loadApplications(){
        if (loadedApps == null || loadedApps.size() != apps.size()){
           loadApplicationsTask = new LoadAndroidApplicationTask(context, currentUser, null, appView, 110, listener);
           loadApplicationsTask.execute();
        }

    }

    /**
     * This function sets the global variable listener.
     * The listener is the OnClickListener that all the AppImageViews created need to implement to make them
     * selectable and deselectable by the user.
     */
    private void setListeners(){
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppImageView appImageView = (AppImageView) v;
                appImageView.toggle();

                if (selectedApps == null)
                    selectedApps = new HashSet<String>();

                AppInfo app = appInfos.get((String) v.getTag());
                String activityName = app.getActivity();

                /** If the user had previously selected the app, removed it from the list of selected apps
                 * otherwise add it to the list of selected apps.*/
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
    }

    /**
     * This class carries out all the work of populating the appView with clickable applications.
     * It inherits from LoadApplicationTask, which does most of the work.
     * However, since there are some special things that need to be handled in the case of Android applications,
     * we must inherit the class, override it's methods and do what we need to do in addition to the superclass
     */
    class LoadAndroidApplicationTask extends LoadApplicationTask {

        /**
         * the contructor of the class
         * @param context The context of the current activity
         * @param currentUser The current user (if the current user is a guardian, this is set to null)
         * @param guardian The guardian of the current user (or just the current user, if the user is a guardian)
         * @param targetLayout The layout to be populated with AppImageViews
         * @param iconSize The size the icons should have
         * @param onClickListener the onClickListener that each created app should have. In this case we feed it the global variable listener
         */
        public LoadAndroidApplicationTask(Context context, Profile currentUser, Profile guardian, LinearLayout targetLayout, int iconSize, View.OnClickListener onClickListener) {
            super(context, currentUser, guardian, targetLayout, iconSize, onClickListener);
        }

        /**
         * This method needs to be overridden since we need to inform the superclass of exactly which apps should be generated.
         * @param applications the applications that the task should generate AppImageViews for
         * @return The Hashmap of AppInfos that describe the added applications.
         */
        @Override
        protected HashMap<String, AppInfo> doInBackground(Application... applications) {
            applications = ApplicationControlUtility.getAndroidAppsAsApplicationList(context, "dk.aau.cs.giraf").toArray(applications);
            appInfos = super.doInBackground(applications);

            return null;
        }
    }
}
