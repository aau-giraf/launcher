package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AndroidAppsFragmentInterface;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementSettings;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.LauncherSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.WombatSettings;

public class SettingsActivity extends Activity
        implements SettingsListFragment.SettingsListFragmentListener,
        AndroidAppsFragmentInterface {

    private FragmentManager mFragManager;
    private Fragment mActiveFragment;
    private ArrayList<SettingsListItem> mAppList;
    private Profile mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Giraf settings debugging", "SettingsActivity onCreate");
        setContentView(R.layout.settings);

        mFragManager = this.getFragmentManager();
        Fragment settingsFragment = mFragManager.findFragmentById(R.id.settingsContainer);

        if (settingsFragment == null) {
            SettingsListItem item = getInstalledSettingsApps().get(0);
            mActiveFragment = item.mAppFragment;
        }

        mFragManager.beginTransaction().add(R.id.settingsContainer, mActiveFragment)
                .commit();
    }

    public ArrayList<SettingsListItem> getInstalledSettingsApps(){
        mAppList = new ArrayList<SettingsListItem>();

        Intent androidSettingsIntent = new Intent(Settings.ACTION_SETTINGS);
        androidSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        addApplicationByPackageName("dk.aau.cs.giraf.launcher",
                new LauncherSettings(LauncherUtility.getSharedPreferenceUser(mCurrentUser)));
        addApplicationByPackageName("dk.aau.cs.giraf.wombat",
                new WombatSettings());

        // These following must be added last!
        // Application management
        addApplicationByName(getString(R.string.apps_list_label),
                new AppManagementSettings(), getResources().getDrawable(R.drawable.ic_apps));
        // Native Android Settings
        addApplicationByName("Android Indstillinger", androidSettingsIntent, getResources().getDrawable(R.drawable.ic_android));

        return removeNonGirafApps(mAppList);
    }

    private ArrayList<SettingsListItem> removeNonGirafApps(ArrayList<SettingsListItem> list) {
        List<ResolveInfo> installedGirafApps = LauncherUtility.getDeviceGirafApps(this);
        ArrayList<SettingsListItem> mAvailableSettingsAppList = (ArrayList<SettingsListItem>) list.clone();

        for (SettingsListItem settingsApp : list) {
            for (ResolveInfo installedApp : installedGirafApps) {
                String installedAppName = installedApp.activityInfo.applicationInfo.packageName.toLowerCase();

                if (!mAvailableSettingsAppList.contains(settingsApp)) {
                    if (settingsApp.mPackageName != null && installedAppName.contains(settingsApp.mPackageName.toLowerCase())) {
                        mAvailableSettingsAppList.add(settingsApp);
                    } else if (settingsApp.mPackageName == null && settingsApp.mAppName != null) {
                        mAvailableSettingsAppList.add(settingsApp);
                    }
                    else if (settingsApp.mIntent != null) {
                        mAvailableSettingsAppList.add(settingsApp);
                    }
                }
            }
        }
        return mAvailableSettingsAppList;
    }

    private void addApplicationByPackageName(String packageName, Fragment fragment) {
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo appInfo = null;

        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (appInfo != null) {
            final String appName = setCorrectCase(pm.getApplicationLabel(appInfo).toString());
            final Drawable appIcon = pm.getApplicationIcon(appInfo);

            SettingsListItem item = new SettingsListItem(
                    packageName,
                    appName,
                    appIcon,
                    fragment
            );
            mAppList.add(item);
        }
    }

    private void addApplicationByName(String appName, Fragment settingsFragment, Drawable icon) {
        SettingsListItem item = new SettingsListItem(
                setCorrectCase(appName),
                icon,
                settingsFragment
        );
        mAppList.add(item);
    }

    private void addApplicationByName(String name, Intent intent, Drawable icon) {
        SettingsListItem item = new SettingsListItem(
                setCorrectCase(name),
                icon,
                intent
        );
        mAppList.add(item);
    }

    private String setCorrectCase(String name) {
        return name.substring(0, 1).toUpperCase()
                + name.substring(1).toLowerCase();
    }

    @Override
    public void setActiveFragment(Fragment fragment) {
        if (mActiveFragment != fragment) {
            FragmentTransaction ft = mFragManager.beginTransaction();
            ft.replace(R.id.settingsContainer, fragment);
            ft.commit();

            mActiveFragment = fragment;
        }
    }

    @Override
    public void reloadActivity()
    {
        Intent intent = getIntent();

        if (mCurrentUser.getRole() == Profile.Roles.CHILD) // A child profile has been selected, pass id
            intent.putExtra(Constants.CHILD_ID, mCurrentUser.getId());
        else // We are a guardian, do not add a child
            intent.putExtra(Constants.CHILD_ID, Constants.NO_CHILD_SELECTED_ID);

        finish();
        startActivity(intent);
    }

    @Override
    public void setCurrentUser(Profile profile) {
            mCurrentUser = profile;
    }

    @Override
    public Profile getSelectedProfile() {
        return mCurrentUser;
    }
}