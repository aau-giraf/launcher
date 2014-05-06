package dk.aau.cs.giraf.launcher.giraffragments.appfragments;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.ProfileLauncher;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.ApplicationController;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileApplicationController;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileApplication;

/**
 * Created by Vagner on 01-05-14.
 */
public class GirafFragment extends AppFragment{

    private HashMap<String,AppInfo> appInfos;
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ApplicationController ac = new ApplicationController(context);
            ProfileApplicationController pac = new ProfileApplicationController(context);
            Profile user = LauncherUtility.findCurrentUser(context);
            List<Application> userAppList = ac.getApplicationsByProfile(user);
            AppInfo app = appInfos.get(v.getTag().toString());

            if(userAppList.contains(app.getApp()))
            {
                pac.removeProfileApplicationByProfileAndApplication(app.getApp(),user);
            }
            else
            {
                ProfileApplication pa = new ProfileApplication(user.getId(), app.getApp().getId());
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
    public void onStart() {
        super.onStart();
        reloadApplications();
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
            appInfos = LauncherUtility.loadGirafApplicationsIntoView(context, (List<Application>) apps, appView, 110, listener);
            if (appInfos == null){
                haveAppsBeenAdded = false;
            }
            else{
                haveAppsBeenAdded = true;
            }
        }
        loadedApps = apps;
    }

}
