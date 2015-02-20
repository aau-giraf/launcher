package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.helper.AppViewCreationUtility;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsActivity;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppContainerFragment;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileApplicationController;
import dk.aau.cs.giraf.oasis.lib.models.Application;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.ProfileApplication;

/**
 * Created by Marhlder on 18-02-15.
 */
public class AppsGridFragment extends Fragment {

    private static final String ROW_SIZE_INT_TAG = "ROW_SIZE_INT_TAG";
    private static final String COLUMN_SIZE_INT_TAG = "COLUMN_SIZE_INT_TAG";
    private static final String APPINFOS_PARCELABLE_TAG = "APPINFOS_PARCELABLE_TAG";

    private ProfileApplicationController pac;
    private Set<String> selectedApps;

    public static AppsGridFragment newInstance(final ArrayList<AppInfo> appInfos, final int rowSize, final int columnSize) {
        AppsGridFragment newFragment = new AppsGridFragment();

        Bundle args = new Bundle();
        args.putInt(ROW_SIZE_INT_TAG, rowSize);
        args.putInt(COLUMN_SIZE_INT_TAG, columnSize);
        args.putParcelableArrayList(APPINFOS_PARCELABLE_TAG, appInfos);
        newFragment.setArguments(args);

        return newFragment;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        pac = new ProfileApplicationController(activity);
        final Profile currentUser = ((AppsFragmentInterface)activity).getCurrentUser();
        SharedPreferences preferences = LauncherUtility.getSharedPreferencesForCurrentUser(activity, currentUser);
        selectedApps = preferences.getStringSet(activity.getResources().getString(R.string.selected_android_apps_key), new HashSet<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final GridLayout appsGridLayout = (GridLayout) inflater.inflate(R.layout.apps_grid_fragment, null);

        final Bundle arguments = getArguments();

        if (arguments != null)
        {
            final int rowSize = arguments.getInt(ROW_SIZE_INT_TAG);
            final int columnSize = arguments.getInt(COLUMN_SIZE_INT_TAG);
            final ArrayList<AppInfo> appInfos = arguments.getParcelableArrayList(APPINFOS_PARCELABLE_TAG);

            appsGridLayout.setRowCount(rowSize);
            appsGridLayout.setColumnCount(columnSize);


            for(int appCounter = 0; appCounter < appInfos.size(); appCounter++)
            {
                    final AppInfo currentAppInfo = appInfos.get(appCounter);

                    final AppsFragmentInterface activity = (AppsFragmentInterface)getActivity();
                    final Profile currentUser = activity.getCurrentUser();

                    //Create a new AppImageView and set its properties
                    AppImageView newAppView = AppViewCreationUtility.createAppImageView(getActivity(), currentUser, activity.getLoggedInGuardian(), currentAppInfo, appsGridLayout, getOnClickListener());
                    newAppView.setScaleType(ImageView.ScaleType.FIT_XY);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(container.getMeasuredWidth() / columnSize, container.getMeasuredHeight() / rowSize);
                    //params.setMargins(2, 2, 2, 2);
                    newAppView.setLayoutParams(params);

                    if (currentAppInfo != null && (doesProfileApplicationExist(pac, currentAppInfo.getApp(), currentUser) || selectedApps.contains(currentAppInfo.getActivity()))) {
                        newAppView.setChecked(true);
                    }

                    appsGridLayout.addView(newAppView, appCounter);

                /*
                View view = new View(getActivity());
                view.setLayoutParams(new ActionBar.LayoutParams(50, 50));
                view.setBackgroundColor(Color.RED);

                appsGridLayout.addView(view, appCounter);
                */
            }
        }

        return appsGridLayout;
    }

    /**
     * This function checks if the ProfileApplication consisting of the given application and the given user exists.
     * If it does, it means the application was selected earlier by the user and should be marked as selected
     *
     * @param pac  The ProfileApplicationController used to retrieve the ProfileApplication
     * @param app  The application to check for
     * @param user The profile to check for
     * @return true if the ProfileApplication exists, otherwise return false
     */
    private boolean doesProfileApplicationExist(ProfileApplicationController pac, Application app, Profile user) {
        ProfileApplication thisPA = pac.getProfileApplicationByProfileIdAndApplicationId(app, user);

        return thisPA != null;
    }

    protected View.OnClickListener getOnClickListener()
    {
        Fragment parentFragment = getParentFragment();

        if(parentFragment instanceof AppContainerFragment)
        {
            return ((AppContainerFragment)parentFragment).getListener();
        }

        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
