package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment_giraf,
                container, false);
        context = getActivity();

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

    /**
     * Does nothing when not overridden.
     * @throws Exception
     */
    protected void loadApplications() throws Exception
    {
        throw new Exception("This method needs to be overridden");
    }
}
