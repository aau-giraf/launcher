package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.List;

import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Vagner on 01-05-14.
 */
public class AppContainerFragment extends Fragment{

    protected List<?> loadedApps;
    // This needs to be initialized in the subclasses
    protected List<?> apps;
    protected LinearLayout appView;
    protected boolean haveAppsBeenAdded;
    protected Context context;
    protected ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment_giraf,
                container, false);
        context = getActivity();
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Resets loadedApps variable to loadApplications once again
     */
    protected void reloadApplications(){
        loadedApps = null; // Force loadApplications to redraw
    }

    protected void loadApplications()
    {
    }

    protected void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar(){
        final android.os.Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (appView.getChildCount() > 0)
                    progressBar.setVisibility(View.GONE);
                else
                    handler.postDelayed(this, 100);
            }
        });
    }
}
