package dk.aau.cs.giraf.launcher.giraffragments.appfragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Created by Vagner on 01-05-14.
 */
public class GirafFragment extends Fragment{

    List<Application> loadedApps;
    List<Application> apps;
    LinearLayout appView;
    boolean haveAppsBeenAdded;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment_giraf,
                container, false);
        context = getActivity();
        apps = LauncherUtility.getAvailableGirafAppsButLauncher(context);
        appView = (LinearLayout) view.findViewById(R.id.giraf_appContainer);

        loadApplications();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadApplications();
    }

    private void reloadApplications(){
        if (!haveAppsBeenAdded) return;
        loadedApps = null; // Force loadApplications to redraw
        loadApplications();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        reloadApplications();
    }

    private void loadApplications()
    {
        if (loadedApps == null || loadedApps.size() != apps.size()){
            //Remember that the apps have been added, so they are not added again by the listener
            HashMap<String,AppInfo> appInfos = LauncherUtility.loadApplicationsIntoView(context,apps,appView, Constants.APP_ICON_DIMENSION_DEF);
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
