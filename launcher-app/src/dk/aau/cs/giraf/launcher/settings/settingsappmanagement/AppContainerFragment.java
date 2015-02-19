package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * This is the superclass that both AndroidFragment and GirafFragment inherits from
 * Since both fragments implement many of the same features with very smaller differences, this was deemed to be the best way.
 * This Fragment should never be implemented directly, but simply inherited from.
 * It is therefore abstract
 */
public abstract class AppContainerFragment extends Fragment {

    protected AppsFragmentInterface mCallback; // Callback to containing Activity implementing the SettingsListFragmentListener interface
    protected Profile currentUser;
    protected ArrayList<AppInfo> loadedApps;
    // This needs to be initialized in the subclasses
    protected List<Application> apps;
    protected ViewPager appView;
    protected AppsFragmentAdapter appsFragmentAdapter;
    protected boolean haveAppsBeenAdded = false;

    /**
     * Because we are dealing with a Fragment, OnCreateView is where most of the variables are set.
     * Only the context is initiated, the rest should be instantiated by the inheriting classes.
     * @param inflater The inflater (Android takes care of this)
     * @param container The container, the ViewGroup, that the fragment should be inflate in.
     * @param savedInstanceState The previously saved instancestate
     * @return the inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (appView == null){
            view = inflater.inflate(R.layout.settings_appmanagement_appcontainer,
                    container, false);

            currentUser = mCallback.getCurrentUser();
        } else {
            view = appView.getRootView();
        }

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
        if (!haveAppsBeenAdded && appView.getViewTreeObserver() != null) {
            appView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    // Ensure you call it only once :
                    appView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    reloadApplications();
                }
            });
        }
    }

    /**
     * This makes sure that the container activity has implemented the callback interface. If not, it throws an exception.
     * The callback interface is needed to reload applications when a new user is selected.
     * @param activity
     */
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (AppsFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AppsFragmentInterface");
        }
    }

    /**
     * Resets loadedApps variable to loadApplications once again
     */
    protected void reloadApplications(){
        loadedApps = null; // Force loadApplications to redraw
    }

    /**
     * This function only exists to be overridden by inheriting classes.
     * This is because the inheriting classes MUST use this method to loadapplications, but they
     * must specify which ASyncTask class they use to load the applications as well.
     */
    protected void loadApplications(){
    }
}
