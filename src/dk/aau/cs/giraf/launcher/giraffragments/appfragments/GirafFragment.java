package dk.aau.cs.giraf.launcher.giraffragments.appfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.oasis.lib.models.Application;

/**
 * Created by Vagner on 01-05-14.
 */
public class GirafFragment extends AppFragment{

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
            HashMap<String,AppInfo> appInfos = LauncherUtility.loadGirafApplicationsIntoView(context, (List<Application>) apps, appView, 110);
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
