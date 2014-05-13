package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
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
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.helper.LoadAndroidApplicationTask;
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
    private AndroidAppsFragmentInterface mCallback;
    private Profile currentUser;
    private HashMap<String,AppInfo> appInfos;
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppImageView appImageView = (AppImageView) v;
            appImageView.toggle();
            ProfileApplicationController pac = new ProfileApplicationController(context);
            AppInfo app = appInfos.get(v.getTag().toString());

            if(UserHasApplicationInView(pac, app.getApp(), currentUser))
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        context = getActivity();
        apps = LauncherUtility.getAvailableGirafAppsButLauncher(context);
        appView = (LinearLayout) view.findViewById(R.id.appContainer);
        currentUser = mCallback.getSelectedProfile();

        super.showProgressBar();

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
    protected void reloadApplications() {
        super.reloadApplications();
        loadApplications();
    }

    @Override
    public void loadApplications()
    {

        if (loadedApps == null || loadedApps.size() != apps.size()){
            //Remember that the apps have been added, so they are not added again by the listener
            LoadAndroidApplicationTask loadAndroidApplicationTask = new LoadAndroidApplicationTask(context, currentUser, null, appView, 110, listener);
            Application[] applications = new Application[apps.size()];
            apps.toArray(applications);
            LoadAndroidApplicationTask load =  (LoadAndroidApplicationTask) loadAndroidApplicationTask.execute(applications);
            //LauncherUtility.loadGirafApplicationsIntoView(context, currentUser, (List<Application>) apps, appView, 110, listener);
            appInfos = LauncherUtility.loadAppInfos(context, (List<Application>) apps, currentUser);
            if (appInfos == null){
                haveAppsBeenAdded = false;
            }
            else{
                haveAppsBeenAdded = true;
                /*
                for (int i = 0; i < appView.getChildCount();i++)
                {
                    LinearLayout thisLayout = (LinearLayout)appView.getChildAt(i);
                    for(int j = 0; j < thisLayout.getChildCount(); j++)
                    {
                        AppImageView appImageView = (AppImageView) thisLayout.getChildAt(j);
                        ProfileApplicationController pac = new ProfileApplicationController(context);

                        AppInfo app = null;
                        try{
                            app = appInfos.get(appImageView.getTag().toString());
                        }
                        catch (Exception e)  {
                            Log.e(Constants.ERROR_TAG, e.getMessage());
                        }
                        if(app != null && UserHasApplicationInView(pac, app.getApp(), currentUser))
                        {
                            appImageView.setChecked(true);
                        }
                    }
                }*/
            }
        }
        loadedApps = apps;

        super.hideProgressBar();
    } 
    private boolean UserHasApplicationInView(ProfileApplicationController pac, Application app, Profile user)
    {
        List<ProfileApplication> profileApplications = pac.getListOfProfileApplicationsByProfileId(user);
        ProfileApplication thisPA = pac.getProfileApplicationByProfileIdAndApplicationId(app,user);

        if(profileApplications.contains(thisPA))
            return true;
        else
            return false;
    }

}
