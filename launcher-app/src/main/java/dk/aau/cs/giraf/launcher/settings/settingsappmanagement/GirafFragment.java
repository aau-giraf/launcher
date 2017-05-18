package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.helper.LoadApplicationTask;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.launcher.settings.components.ApplicationGridResizer;
import dk.aau.cs.giraf.launcher.widgets.AppImageView;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.PutRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.Application;
import dk.aau.cs.giraf.models.core.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is the Fragment used to show the available Giraf apps installed on the device.
 * The user can select or deselect each app by pressing it, handled in the OnClickListener listener
 */
public class GirafFragment extends AppContainerFragment {

    private ArrayList<AppInfo> appInfos;
    private LoadGirafApplicationTask loadApplicationTask;
    private RequestQueue queue;
    //private View.OnClickListener listener;

    /**
     * Because we are dealing with a Fragment, OnCreateView is where most of the variables are set.
     * The context is set by the superclass.
     *
     * @param inflater           The inflater (Android takes care of this)
     * @param container          The container, the ViewGroup, that the fragment should be inflate in.
     * @param savedInstanceState The previously saved instancestate
     * @return the inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        queue = RequestQueueHandler.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        GetRequest<User> userGetRequest = new GetRequest<User>( User.class, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                onCreateViewResponse(view, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    LoginRequest loginRequest = new LoginRequest(currentUser, new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            GetRequest<User> userGetRequest = new GetRequest<User>( User.class, new Response.Listener<User>() {
                                @Override
                                public void onResponse(User response) {
                                    onCreateViewResponse(view, response);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse.statusCode == 401) {
                                        LauncherUtility.showErrorDialog(view.getContext(),R.string.home_activity_you_do_not_have_access_to_this);
                                    } else {
                                        LauncherUtility.showErrorDialog(view.getContext(), R.string.dialog_offline_server);}
                                }
                            });
                            queue.add(userGetRequest);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            LauncherUtility.showErrorDialog(view.getContext(),R.string.dialog_offline_server);
                        }
                    });
                    queue.add(loginRequest);
                } else {
                    LauncherUtility.showErrorDialog(view.getContext(),R.string.dialog_offline_server);
                }

            }
        });
        queue.add(userGetRequest);

        return view;
    }

    private void onCreateViewResponse(View view, User user) {
        appView = (ViewPager) view.findViewById(R.id.appsViewPager);
        final int rowsSize = ApplicationGridResizer.getGridRowSize(currentUser);
        final int columnsSize = ApplicationGridResizer.getGridColumnSize(currentUser);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

            appView.setAdapter(new AppsFragmentAdapter(user, getChildFragmentManager(), appInfos, rowsSize, columnsSize));
        } else {

            appView.setAdapter(new AppsFragmentAdapter(user, getFragmentManager(), appInfos, rowsSize, columnsSize));
        }

        CirclePageIndicator titleIndicator = (CirclePageIndicator) view.findViewById(R.id.pageIndicator);
        titleIndicator.setViewPager(appView);
    }

    /**
     * We override onResume to make the observer start observing apps.
     */
    @Override
    public void onResume() {
        super.onResume();
        reloadApplications();
    }

    /**
     * Handles what happens when the fragment is paused
     * If the fragment is still loadapplications in the ASyncTask, it cancels the task
     * It stops observing apps, so it doesnt try an update while paused.
     */
    @Override
    public void onPause() {
        super.onPause();

        if (loadApplicationTask != null) {
            loadApplicationTask.cancel(true);
        }
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
     * Keep in mind that this version of loadapplication uses LoadGirafApplicationTask,
     * while AndroidFragment uses LoadAndroidApplicationTask, which is why it must be overridden
     */
    @Override
    public void loadApplications() {
        if (loadedApps == null || AppInfo.isAppListsDifferent(loadedApps, apps)) {
            //Remember that the apps have been added, so they are not added again by the listener

            loadApplicationTask = new LoadGirafApplicationTask(getActivity(), currentUser, null, appView, listener);
            loadApplicationTask.execute();
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
            public void onClick(final View view) {
                GetRequest<User> getRequest = new GetRequest<User>( User.class, new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        User localUser = response;
                        AppImageView appImageView = (AppImageView) view;
                        appImageView.toggle();
                        if (userCanAccesApp(appImageView.appInfo.getApp(), localUser)) {
                            List<Application> applicationCollection = localUser.getSettings().getAppsUserCanAccess();
                            applicationCollection.remove(appImageView.appInfo.getApp());
                            localUser.getSettings().setAppsUserCanAccess(applicationCollection);
                        } else {
                            List<Application> applicationCollection = localUser.getSettings().getAppsUserCanAccess();
                            applicationCollection.add(appImageView.appInfo.getApp());
                            localUser.getSettings().setAppsUserCanAccess(applicationCollection);
                        }
                        PutRequest<User> putRequest = new PutRequest<User>(localUser, new Response.Listener<User>() {
                            @Override
                            public void onResponse(User response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        queue.add(putRequest);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Launcer", "Could not get user for GirafFragment");
                    }
                });
                queue.add(getRequest);
            }
        };
    }

    private boolean userCanAccesApp(Application app, User user) {
        return user.getSettings().getAppsUserCanAccess().contains(app);
    }

    /**
     * This class carries out all the work of populating the appView with clickable applications.
     * It inherits from LoadApplicationTask, which does most of the work.
     * However, since there are some special things that need to be handled in the case of Giraf applications,
     * we must inherit the class, override it's methods and do what we need to do in addition to the superclass
     */
    class LoadGirafApplicationTask extends LoadApplicationTask {

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
        public LoadGirafApplicationTask(Context context, User currentUser,
                                        User guardian, ViewPager appsViewPager, View.OnClickListener onClickListener)
        {
            super(context, currentUser, appsViewPager, onClickListener);
        }


        /**
         * This method needs to be overridden since we need to inform the superclass of
         * exactly which apps should be generated.
         * In this case, it is Giraf applications only
         *
         * @param applications the applications that the task should generate AppImageViews for
         * @return The Hashmap of AppInfos that describe the added applications.
         */
        @Override
        protected ArrayList<AppInfo> doInBackground(Application... applications) {
            apps = ApplicationControlUtility.getGirafAppsButLauncherOnDevice(context);
            applications = apps.toArray(applications);
            appInfos = super.doInBackground(applications);

            return appInfos;
        }

        @Override
        public android.support.v4.app.FragmentManager getFragmentMangerForAppsFragmentAdapter() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return GirafFragment.this.getChildFragmentManager();
            } else {
                return GirafFragment.this.getFragmentManager();
            }
        }

    }

}
