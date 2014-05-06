package dk.aau.cs.giraf.launcher.giraffragments.appfragments;

import android.app.Activity;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppImageView;

/**
 * Created by Vagner on 01-05-14.
 */
public class AndroidFragment extends AppFragment {
    public InterfaceParseAndroidApps interfaceParseAndroidApps;
    public interface InterfaceParseAndroidApps {
        public void setSelectedAndroidApps(List<ResolveInfo> selectedAndroidApps);
    }

    private List<ResolveInfo> selectedApps;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppImageView appImageView = (AppImageView) v;
            appImageView.toggle();

            if (selectedApps == null)
                selectedApps = new ArrayList<ResolveInfo>();

            ResolveInfo app = (ResolveInfo) v.getTag();

            if (selectedApps.contains(app)){
                selectedApps.remove(app);
                Log.d(Constants.ERROR_TAG, "Removed '" + app.activityInfo.name + "' to list: " + selectedApps.size());
            }
            else{
                selectedApps.add(app);
                Log.d(Constants.ERROR_TAG, "Added '" + app.activityInfo.name + "' to list: " + selectedApps.size());
            }
        }
    };

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            interfaceParseAndroidApps = (InterfaceParseAndroidApps) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement GetSelectedAndroidApps");
        }
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
    public void onPause() {
        super.onPause();
        if (selectedApps == null)
            selectedApps = new ArrayList<ResolveInfo>();
        interfaceParseAndroidApps.setSelectedAndroidApps(selectedApps);
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
            haveAppsBeenAdded = LauncherUtility.loadOtherApplicationsIntoView(context, (List<ResolveInfo>)apps, appView, 110, onClickListener);
        }
        loadedApps = apps;
    }
}
