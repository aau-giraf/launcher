package dk.aau.cs.giraf.launcher.giraffragments.appfragments;

import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;

/**
 * Created by Vagner on 01-05-14.
 */
public class AndroidFragment extends AppFragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        context = getActivity();
        apps = LauncherUtility.getApplicationsFromDevice(context, "dk.aau.cs.giraf", false);
        appView = (LinearLayout) view.findViewById(R.id.appContainer);

        loadApplications();

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
            haveAppsBeenAdded = LauncherUtility.loadOtherApplicationsIntoView(context, (List<ResolveInfo>)apps, appView, 110);
        }
        loadedApps = apps;
    }
}
