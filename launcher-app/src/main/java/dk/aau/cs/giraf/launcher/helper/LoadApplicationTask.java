package dk.aau.cs.giraf.launcher.helper;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Collections;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppsFragmentAdapter;
import dk.aau.cs.giraf.launcher.settings.components.ApplicationGridResizer;
import dk.aau.cs.giraf.dblib.models.Application;
import dk.aau.cs.giraf.dblib.models.Profile;

/**
 * This is the main class that loads applications as AppImageViews into a given targetlayout
 * When using this class, you must derive from it, and specify in the derived class exactly
 * what applications you want to generate AppImageViews for.
 * For examples:
 *
 * @see dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AndroidFragment.LoadAndroidApplicationTask
 * @see dk.aau.cs.giraf.launcher.settings.settingsappmanagement.GirafFragment.loadGirafApplicationTask
 * @see dk.aau.cs.giraf.launcher.activities.HomeActivity.LoadHomeActivityApplicationTask
 */
public abstract class LoadApplicationTask extends AsyncTask<Application, View, ArrayList <AppInfo>> {

    protected Profile currentUser;
    protected Profile guardian;
    protected Context context;
    protected ViewPager appsViewPager;
    protected View.OnClickListener onClickListener;

    protected ProgressBar progressbar;

    /**
     * the contructor for the class
     *
     * @param context         The context for the current activity
     * @param currentUser     The user of the current activity. If set to null, the user will be found based on the context
     * @param guardian        The guardian of the current user.
     * @param appsViewPager    The layout that the AppImageViews should be put into
     * @param onClickListener The onClickListener attached to each AppImageView. These vary depending on the purpose of the layout they are loaded into.
     */
    public LoadApplicationTask(Context context, Profile currentUser, Profile guardian, ViewPager appsViewPager, View.OnClickListener onClickListener) {
        this.context = context;
        this.currentUser = currentUser;
        this.guardian = guardian;
        this.appsViewPager = appsViewPager;
        this.onClickListener = onClickListener;
    }

    /**
     * The tasks that should be carried out before the main task in doInBackground.
     * This function adds a progress bar to the parent of the targetlayout and
     * removes all other views from the targetlayout itself.
     */
    @Override
    protected void onPreExecute() {

        changeVisibilityOfNoAppsMessage(View.GONE);

        ViewGroup parent = getProgressBarParent();
        progressbar = (ProgressBar) parent.findViewById(R.id.ProgressBar);
        progressbar.setVisibility(View.VISIBLE);
    }

    /**
     * This is the main task to be carried out - populating the targetlayout with AppImageViews.
     * AppImageViews are generated from the given ArrayList of Applications
     * Futhermore comments can be found inside the function
     *
     * @param applications The applications that should populate the targetlayout
     * @return a HashMap containing all the apps that there was generated AppImageViews for.
     */
    @Override
    protected ArrayList<AppInfo> doInBackground(Application... applications)
    {
        // Only creates AppImageViews if there actually are applications to generate
        if (applications != null && applications.length != 0)
        {
            // If the current user is null, find the user based on the context
            if (currentUser == null) {
                currentUser = LauncherUtility.getCurrentUser(context);
            }

            // update the HashMap with information of the apps being generated and sort it
            ArrayList<AppInfo> appInfoList = AppViewCreationUtility.updateAppInfoList(context, applications);
            Collections.sort(appInfoList, new AppComparator(context));

            return appInfoList;
        } else {
            // show no apps available message
            Log.e(Constants.ERROR_TAG, "App list is null");
        }

        return null;
    }

    /**
     * Carries out after the main task has completed in doInBackground.
     * Hides the progressbar, adds the rows in the list of rows to be added to the targetlayout.
     * Also clears the parent of the targetlayout of progressbars
     *
     * @param appInfos
     */
    @Override
    protected void onPostExecute(ArrayList<AppInfo> appInfos) {


        progressbar.setVisibility(View.INVISIBLE);

        final int rowsSize = ApplicationGridResizer.getGridRowSize(this.context, currentUser);
        final int columnsSize = ApplicationGridResizer.getGridColumnSize(this.context, currentUser);

        if (appInfos!= null && appInfos.size() > 0)
        {
            changeVisibilityOfNoAppsMessage(View.GONE);
        }
        else
        {
            changeVisibilityOfNoAppsMessage(View.VISIBLE);
        }

        ((AppsFragmentAdapter)this.appsViewPager.getAdapter()).swapApps(appInfos , rowsSize, columnsSize);

    }

    public android.support.v4.app.FragmentManager getFragmentMangerForAppsFragmentAdapter()
    {
        return ((FragmentActivity)context).getSupportFragmentManager();
    }

    /**
     * If the task was cancelled, and the progressbar is not null, hide the progressbar
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (progressbar != null)
            progressbar.setVisibility(View.GONE);
    }

    /**
     * Gets the correct parent of the targetlayout to add the Progressbar to
     *
     * @return
     */
    private ViewGroup getProgressBarParent() {

        ViewGroup parent = (ViewGroup) appsViewPager.getParent();

        while (parent instanceof ScrollView) {
            parent = (ViewGroup) parent.getParent();
        }

        return parent;
    }

    /**
     * Check for stray progressbars still running and remove them if needed.
     */
    private void removeStrayProgressbars() {
        ViewGroup parent = getProgressBarParent();

        ArrayList<Integer> delete = new ArrayList<Integer>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i) instanceof ProgressBar) {
                delete.add(i);
            }
        }

        if (delete.size() > 0) {
            for (int i = delete.size() - 1; i >= 0; i--) {
                parent.removeViewAt(delete.get(i));
            }
        }
    }

    /**
     * Sets the visibility of the noAppsText message shown if no apps where added to the targetlayout
     * Takes into consideration if we are in SettingsActivity or HomeActivity
     *
     * @param visibility
     */
    private void changeVisibilityOfNoAppsMessage(int visibility) {

        View noAppsTextView = ((Activity) context).findViewById(R.id.noAppsMessage);
        noAppsTextView.setVisibility(visibility);

    }
}
