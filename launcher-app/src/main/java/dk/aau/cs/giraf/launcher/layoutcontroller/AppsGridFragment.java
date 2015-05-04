package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.AppViewCreationUtility;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsActivity;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppContainerFragment;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.launcher.widgets.AppImageView;
import dk.aau.cs.giraf.dblib.controllers.ProfileApplicationController;
import dk.aau.cs.giraf.dblib.models.Application;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.dblib.models.ProfileApplication;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        pac = new ProfileApplicationController(activity);
        final Profile currentUser = ((AppsFragmentInterface) activity).getCurrentUser();
        SharedPreferences preferences = LauncherUtility.getSharedPreferencesForCurrentUser(activity, currentUser);
        selectedApps = preferences.getStringSet(activity.getResources().getString(R.string.selected_android_apps_key), new HashSet<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final GridLayout appsGridLayout = (GridLayout) inflater.inflate(R.layout.apps_grid_fragment, null);

        final Bundle arguments = getArguments();

        if (arguments != null) {
            final int rowSize = arguments.getInt(ROW_SIZE_INT_TAG);
            final int columnSize = arguments.getInt(COLUMN_SIZE_INT_TAG);
            final ArrayList<AppInfo> appInfos = arguments.getParcelableArrayList(APPINFOS_PARCELABLE_TAG);

            appsGridLayout.setRowCount(rowSize);
            appsGridLayout.setColumnCount(columnSize);

            for (int appCounter = 0; appCounter < appInfos.size(); appCounter++) {
                final AppInfo currentAppInfo = appInfos.get(appCounter);

                final AppsFragmentInterface activity = (AppsFragmentInterface) getActivity();
                final Profile currentUser = activity.getCurrentUser();

                //Create a new AppImageView and set its properties
                AppImageView newAppView = AppViewCreationUtility.createAppImageView(getActivity(), currentUser, activity.getLoggedInGuardian(), currentAppInfo, appsGridLayout, getOnClickListener());
                //newAppView.setScaleType(ImageView.ScaleType.FIT_XY);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(container.getMeasuredWidth() / columnSize, container.getMeasuredHeight() / rowSize));
                //params.setMargins(2, 2, 2, 2);
                newAppView.setLayoutParams(params);

                // Application icons should only have their check state set when in the SettingsActivity
                if (activity instanceof SettingsActivity && currentAppInfo != null && (doesProfileApplicationExist(pac, currentAppInfo.getApp(), currentUser) || selectedApps.contains(currentAppInfo.getActivity()))) {
                    newAppView.setChecked(true);
                }
                appsGridLayout.addView(newAppView, appCounter);

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

    protected View.OnClickListener getOnClickListener() {

        Fragment parentFragment;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            parentFragment = getParentFragment();
        } else {
            FragmentManager manager = getFragmentManager();

            parentFragment = manager.findFragmentById(R.id.app_settings_fragmentlayout);
        }

        if (parentFragment instanceof AppContainerFragment) {
            return ((AppContainerFragment) parentFragment).getListener();
        }

        return null;
    }
}
