package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.viewpagerindicator.CirclePageIndicator;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.ApplicationControlUtility;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.launcher.settings.components.ApplicationGridResizer;
import dk.aau.cs.giraf.launcher.widgets.AppImageView;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.Application;
import dk.aau.cs.giraf.models.core.Settings;
import dk.aau.cs.giraf.models.core.User;

import java.util.*;



/**
 * This is the Fragment used to show the available Android apps installed on the device.
 * The user can select or deselect each app by pressing it, handled in the OnClickListener listener
 */
public class AndroidFragment extends AppContainerFragment {
    private Timer appsUpdater;
    private SharedPreferences preferences;
    private Set<String> selectedApps;
    private ArrayList<AppInfo> appInfos;
    private LoadAndroidApplicationTask loadApplicationsTask;
    private RequestQueue queue;
    private Settings settings;
    private RequestQueueHandler handler;

    /**
     * Because we are dealing with a Fragment, OnCreateView is where most of the variables are set.
     * The context and the currentUser is set by the superclass.
     *
     * @param inflater           The inflater (Android takes care of this)
     * @param container          The container, the ViewGroup, that the fragment should be inflate in.
     * @param savedInstanceState The previously saved instancestate
     * @return the inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        handler = RequestQueueHandler.getInstance(getActivity().getApplicationContext());
        queue = handler.getRequestQueue();
        onCreateViewResponce(view,currentUser);
        return view;
    }

    private void onCreateViewResponce(View view, User user) {
        appView = (ViewPager) view.findViewById(R.id.appsViewPager);
        final int rowsSize = ApplicationGridResizer.getGridRowSize(currentUser);
        final int columnsSize = ApplicationGridResizer.getGridColumnSize(currentUser);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

            appView.setAdapter(new AppsFragmentAdapter(user, getChildFragmentManager(), appInfos, rowsSize, columnsSize));
        } else {

            appView.setAdapter(new AppsFragmentAdapter(user ,getFragmentManager(), appInfos, rowsSize, columnsSize));
        }

        CirclePageIndicator titleIndicator = (CirclePageIndicator) view.findViewById(R.id.pageIndicator);
        titleIndicator.setViewPager(appView);
    }

    /**
     * We override onResume to make the observer start observing apps.
     */
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (haveAppsBeenAdded) {
            startObservingApps();
        }

        reloadApplications();
    }

    /**
     * Handles what happens when the fragment is paused
     * It stops observing apps, so it doesnt try an update while paused.
     * It removes the file containing the previous settings for which Android apps are connected to the user
     * Subsequently, it inputs a new file, containing the new settings for the apps saved to a user.
     * Finally, if the fragment is still loadapplications in the ASyncTask, it cancels the task
     */
    @Override
    public synchronized void onPause() {
        super.onPause();
        if (selectedApps == null)
            selectedApps = new HashSet<String>();

        if (appsUpdater != null) {
            appsUpdater.cancel();
            Log.d(Constants.ERROR_TAG, "Applications are no longer observed.");
        }


        if (loadApplicationsTask != null) {
            loadApplicationsTask.cancel(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //ToDo make this work, because we get a stack overflow from rest, the code should work, but rest thinks not
        /*GetRequest<User> getRequest = new GetRequest<User>( User.class, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                User localUser = response;
                final Settings settings = response.getSettings();
                settings.setAppsUserCanAccess(AndroidFragment.this.settings.getAppsUserCanAccess());
                handler.resourceRequest(settings, new Response.Listener<Settings>() {
                    @Override
                    public void onResponse(Settings response) {
                        Log.i("Launcher", "Put user settings request success for GirafFragment");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handler.login(currentUser, new Response.Listener<Integer>() {
                            @Override
                            public void onResponse(Integer response) {
                                handler.resourceRequest(settings, new Response.Listener<Settings>() {
                                    @Override
                                    public void onResponse(Settings response) {
                                        Log.i("Launcher", "Put user settings request success for GirafFragment");
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("Launcher", "Put user request failed for GirafFragment");
                                    }
                                });
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Launcher", "Put user request failed for GirafFragment");
                            }
                        });
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Launcer", "Could not get user for GirafFragment");
            }
        });
        queue.add(getRequest);*/
    }

    /**
     * This function reloads all the applications into the view.
     *
     * @see dk.aau.cs.giraf.launcher.helper.LoadApplicationTask to see what the superclass does.
     */
    @Override
    protected void reloadApplications() {
        super.reloadApplications();
        loadApplications();
    }

    /**
     * Loads applications into the appview container if:
     * - the currently loadedapps list is null OR
     * - the size of the current loadedapps list is not equal to the list of all apps that should be loaded.
     * the superclass merely exists for derived classes to overwrite it and is empty
     * Keep in mind that this version of loadapplication uses LoadAndroidApplicationTask,
     * while GirafFragment uses LoadGirafApplicationTask, which is why it must be overridden
     */
    @Override
    public synchronized void loadApplications() {

        if (loadedApps == null || AppInfo.isAppListsDifferent(loadedApps, apps)) {
            loadApplicationsTask = new LoadAndroidApplicationTask(getActivity(), currentUser, null, appView, listener);
            loadApplicationsTask.execute();
        }
    }

    /**
     * This function sets the global variable listener.
     * The listener is the OnClickListener that all the AppImageViews created need to implement to make them
     * selectable and deselectable by the user.
     */
    @Override
    void setListeners() {
        super.listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                synchronized (AndroidFragment.this) {
                    AppImageView appImageView = (AppImageView) view;
                    appImageView.toggle();
                    if (userCanAccesApp(appImageView.appInfo.getApp(), currentUser)) {
                        if (settings.getAppsUserCanAccess().contains(appImageView.appInfo.getApp())) {
                            List<Application> applicationCollection = settings.getAppsUserCanAccess();
                            applicationCollection.remove(appImageView.appInfo.getApp());
                            settings.setAppsUserCanAccess(applicationCollection);
                        }
                    } else {
                        List<Application> applicationCollection = settings.getAppsUserCanAccess();
                        applicationCollection.add(appImageView.appInfo.getApp());
                        settings.setAppsUserCanAccess(applicationCollection);
                    }
                }
            }
        };
    }

    private boolean userCanAccesApp(Application app, User user) {
        return user.getSettings().getAppsUserCanAccess().contains(app);
    }


    /**
     * Starts a timer that looks for updates in the set of available applications every 5 seconds.
     */

    private void startObservingApps() {
        appsUpdater = new Timer();
        AppsObserver timerTask = new AppsObserver();

        try {
            appsUpdater.scheduleAtFixedRate(timerTask, 5000, 5000);
        } catch (IllegalStateException e) {
            Log.e(Constants.ERROR_TAG, "Timer was already canceled:" + e.getMessage());
        }

        Log.d(Constants.ERROR_TAG, "Applications are being observed.");
    }


    /**
     * This class carries out all the work of populating the appView with clickable applications.
     * It inherits from LoadApplicationTask, which does most of the work.
     * However, since there are some special things that need to be handled in the case of Android applications,
     * we must inherit the class, override it's methods and do what we need to do in addition to the superclass
     */
    private class LoadAndroidApplicationTask extends LoadApplicationTask {

        /**
         * The contructor of the class
         *
         * @param context         The context of the current activity
         * @param currentUser     The current user (if the current user is a guardian, this is set to null)
         * @param guardian        The guardian of the current user (or just the current user, if the user is a guardian)
         * @param appsViewPager   The layout to be populated with AppImageViews
         * @param onClickListener the onClickListener that each created app should have.
         *                        In this case we feed it the global variable listener
         */
        public LoadAndroidApplicationTask(Context context, User currentUser, User guardian,
                                          ViewPager appsViewPager, View.OnClickListener onClickListener)
        {
            super(context, currentUser, appsViewPager, onClickListener);
        }

        /**
         * We override onPreExecute to cancel the AppObserver if it is running.
         */
        @Override
        protected void onPreExecute() {
            if (appsUpdater != null)
                appsUpdater.cancel();

            super.onPreExecute();
        }

        @Override
        public android.support.v4.app.FragmentManager getFragmentMangerForAppsFragmentAdapter() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return AndroidFragment.this.getChildFragmentManager();
            } else {
                return AndroidFragment.this.getFragmentManager();
            }
        }

        /**
         * This method needs to be overridden since we need to inform the
         * superclass of exactly which apps should be generated.
         * In this case it is Android applications only.
         *
         * @param applications the applications that the task should generate AppImageViews for
         * @return The Hashmap of AppInfos that describe the added applications.
         */
        @Override
        protected ArrayList<AppInfo> doInBackground(Application... applications) {
            applications = ApplicationControlUtility.getAndroidAppsOnDeviceAsApplicationList(context)
                .toArray(applications);
            appInfos = super.doInBackground(applications);

            return appInfos;
        }

        /**
         * Once we have loaded applications, we start observing for new apps.
         */
        @Override
        protected void onPostExecute(ArrayList<AppInfo> appInfoList) {
            super.onPostExecute(appInfoList);
            loadedApps = appInfoList;
            startObservingApps();
            haveAppsBeenAdded = true;
        }
    }

    /**
     * Task for observing if the set of available apps has changed.
     * Is only instantiated after apps have been loaded the first time.
     *
     * @see dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AndroidFragment#loadApplications()
     */
    private class AppsObserver extends TimerTask {

        @Override
        public void run() {
            apps = ApplicationControlUtility.getAndroidAppsOnDeviceAsApplicationList(getActivity());
            if (AppInfo.isAppListsDifferent(loadedApps, apps)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadApplications();
                    }
                });
            }
            Log.d(Constants.ERROR_TAG, "Applications checked");
        }

    }
}
