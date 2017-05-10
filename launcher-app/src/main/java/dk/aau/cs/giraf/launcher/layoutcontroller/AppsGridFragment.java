package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.SettingsActivity;
import dk.aau.cs.giraf.launcher.helper.AppViewCreationUtility;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppContainerFragment;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppsFragmentInterface;
import dk.aau.cs.giraf.launcher.widgets.AppImageView;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.User;
import dk.aau.cs.giraf.models.core.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class handles how the apps are displayed in the launcher.'
 */
//TODO fix possible nullpointer exception

public class AppsGridFragment extends Fragment {

    private static final String ROW_SIZE_INT_TAG = "ROW_SIZE_INT_TAG";
    private static final String COLUMN_SIZE_INT_TAG = "COLUMN_SIZE_INT_TAG";
    private static final String APPINFOS_PARCELABLE_TAG = "APPINFOS_PARCELABLE_TAG";

    private Collection<Application> selectedApps;

    private RequestQueue queue;

    //ToDo Write JavaDoc

    /**
     * Constructs an AppsGridFragment using a bundle.
     *
     * @param appInfos A list of AppInfo
     * @param rowSize The amount of rows available
     * @param columnSize The amount of columns available
     * @return A newly created AppsGridFragment
     */
    public static AppsGridFragment newInstance(final ArrayList<AppInfo> appInfos,
                                               final int rowSize, final int columnSize)
    {
        Bundle args = new Bundle();
        args.putInt(ROW_SIZE_INT_TAG, rowSize);
        args.putInt(COLUMN_SIZE_INT_TAG, columnSize);
        args.putParcelableArrayList(APPINFOS_PARCELABLE_TAG, appInfos);
        AppsGridFragment newFragment = new AppsGridFragment();
        newFragment.setArguments(args);


        return newFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*final User currentUser = ((AppsFragmentInterface) activity).getUser();
        SharedPreferences preferences = LauncherUtility.getSharedPreferencesForCurrentUser(activity, currentUser);
        selectedApps = preferences.getStringSet(activity.getResources()
            .getString(R.string.selected_android_apps_key), new HashSet<String>());*/
        final User currentUser = ((AppsFragmentInterface) activity).getUser();

        GetRequest<User> userGetRequest =
            new GetRequest<User>(currentUser.getId(), User.class, new Response.Listener<User>() {
                @Override
                public void onResponse(User response) {
                    selectedApps = response.getSettings().getAppsUserCanAccess();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse.statusCode == 401) {
                        LoginRequest loginRequest = new LoginRequest(currentUser, new Response.Listener<Integer>() {
                            @Override
                            public void onResponse(Integer response) {
                                GetRequest<User> userGetRequest =
                                    new GetRequest<User>(currentUser.getId(), User.class, new Response.Listener<User>() {
                                        @Override
                                        public void onResponse(User response) {
                                            selectedApps = response.getSettings().getAppsUserCanAccess();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("Launcher","User has not enough permissions for AppsGridFragment");
                                        }
                                    });
                                queue.add(userGetRequest);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Launcher","Could not get user for AppsGridFragment");
                            }
                        });
                        queue.add(loginRequest);
                    }
                    else{
                        Log.e("Launcher","Could not get user for AppsGridFragment");
                    }
                }
            });
        queue.add(userGetRequest);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        queue = RequestQueueHandler.getInstance(this.getContext().getApplicationContext()).getRequestQueue();

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
                final User currentUser = activity.getUser();
                final int margin = 10;

                //Create a new AppImageView and set its properties
                AppImageView newAppView = AppViewCreationUtility.createAppImageView(getActivity(), currentUser,
                    activity.getUser(), currentAppInfo, appsGridLayout, getOnClickListener());
                //newAppView.setScaleType(ImageView.ScaleType.FIT_XY);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(
                    container.getMeasuredWidth() / columnSize - margin * 2, container.getMeasuredHeight() /
                    rowSize - margin * 2));
                params.setMargins(margin, margin, margin, margin);
                newAppView.setLayoutParams(params);

                // Application icons should only have their check state set when in the SettingsActivity
                if (activity instanceof SettingsActivity && currentAppInfo != null && (doesProfileApplicationExist(
                    currentAppInfo.getApp(), currentUser) || selectedApps.contains(currentAppInfo.getActivity())))
                {
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
     * Note (09-05-2017): As a part of switching to rest-models, I have chosen to assume that this method checks if a
     * given user has access to an application, and has replaced the old code to reflect this.
     *
     * //@param pac  The ProfileApplicationController used to retrieve the ProfileApplication
     * @param app  The application to check for
     * @param user The profile to check for
     * @return true if the ProfileApplication exists, otherwise return false
     */
    private boolean doesProfileApplicationExist(Application app, User user) {
        return user.getSettings().getAppsUserCanAccess().contains(app);
        //ProfileApplication thisProfileApplication = pac.getProfileApplicationByProfileIdAndApplicationId(app, user);
        //return thisProfileApplication != null;
    }

    private View.OnClickListener getOnClickListener() {

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
