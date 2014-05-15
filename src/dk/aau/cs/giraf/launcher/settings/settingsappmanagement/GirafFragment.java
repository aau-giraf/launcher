package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileApplicationController;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileApplication;

/**
 * Created by Vagner on 01-05-14.
 */
public class GirafFragment extends AppContainerFragment {

    private AppsFragmentInterface mCallback;
    private Profile currentUser;
    private HashMap<String,AppInfo> appInfos;
    private loadGirafApplicationTask loadApplicationTask;
    private View.OnClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        context = getActivity();
        appView = (LinearLayout) view.findViewById(R.id.appContainer);
        currentUser = mCallback.getSelectedProfile();

        setListeners();

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    // Ensure you call it only once :
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
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
            mCallback = (AppsFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AppsFragmentInterface");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        loadApplicationTask.cancel(true);
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
            //Remember that the apps have been added, so they are not added again by the listener

            loadApplicationTask = new loadGirafApplicationTask(context, currentUser, null, appView, 110, listener);
            loadApplicationTask.execute();
        }
    }

    private void setListeners() {
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppImageView appImageView = (AppImageView) v;
                appImageView.toggle();
                ProfileApplicationController pac = new ProfileApplicationController(context);
                AppInfo app = appInfos.get(v.getTag().toString());

                if(userHasApplicationInView(pac, app.getApp(), currentUser))
                {
                    pac.removeProfileApplicationByProfileAndApplication(app.getApp(), currentUser);
                }
                else
                {
                    ProfileApplication pa = new ProfileApplication(currentUser.getId(), app.getApp().getId());
                    pac.insertProfileApplication(pa);
                }
            }
        };
    }

    private boolean userHasApplicationInView(ProfileApplicationController pac, Application app, Profile user)
    {
        List<ProfileApplication> profileApplications = pac.getListOfProfileApplicationsByProfileId(user);
        ProfileApplication thisPA = pac.getProfileApplicationByProfileIdAndApplicationId(app,user);

        if(profileApplications.contains(thisPA))
            return true;
        else
            return false;
    }

    class loadGirafApplicationTask extends LoadApplicationTask {

        public loadGirafApplicationTask(Context context, Profile currentUser, Profile guardian, LinearLayout targetLayout, int iconSize, View.OnClickListener onClickListener) {
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
            apps = ApplicationControlUtility.getAvailableGirafAppsButLauncher(context);
            applications = apps.toArray(applications);
            appInfos = super.doInBackground(applications);

            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, AppInfo> appInfos) {
            super.onPostExecute(appInfos);
            if (appInfos == null){
                haveAppsBeenAdded = false;
            }
            else
            {
                haveAppsBeenAdded = true;
                loadedApps = apps;
            }
            Log.d(Constants.ERROR_TAG, "Thread says bye");
        }
    }
}
