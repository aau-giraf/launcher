package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.LauncherSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.WombatSettings;

public class SettingsActivity extends Activity
        implements SettingsListFragment.SettingsListFragmentListener {

    private FragmentManager mFragManager;
    private SettingsListAdapter mAdapter;
    private ListView mSettingsListView;
    private Fragment mActiveFragment;
    private ArrayList<SettingsListItem> mSettingsAppList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Giraf settings debugging", "SettingsActivity onCreate");
        setContentView(R.layout.settings);

        mSettingsListView = (ListView)findViewById(R.id.settingsListView);

        // Adds data to SettingsListFragment
        populateListFragment();

        mFragManager = this.getFragmentManager();
        Fragment settingsFragment = mFragManager.findFragmentById(R.id.settingsContainer);

        if (settingsFragment == null) {
            SettingsListItem item = (SettingsListItem) mAdapter.getItem(0);
            mActiveFragment = item.mAppFragment;
        }

        mFragManager.beginTransaction().add(R.id.settingsContainer, mActiveFragment)
                .commit();
    }

    private void populateListFragment(){
        mSettingsAppList = new ArrayList<SettingsListItem>();

        addApplicationByPackageName("dk.aau.cs.giraf.launcher", new LauncherSettings());
        addApplicationByPackageName("dk.aau.cs.giraf.wombat", new WombatSettings());
        addApplicationByPackageName("com.android.test", new LauncherSettings());
        addApplicationByName("Android", new AppManagementSettings(), getResources().getDrawable(R.drawable.android_icon));

        // Getting mAdapter by passing list data
        mAdapter = new SettingsListAdapter(SettingsActivity.this, getInstalledGirafApps(mSettingsAppList));
        mSettingsListView.setAdapter(mAdapter);
    }

    private ArrayList<SettingsListItem> getInstalledGirafApps(ArrayList<SettingsListItem> list) {
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
            mSettingsAppList.add(item);
        }
    }

    private void addApplicationByName(String appName, Fragment settingsFragment, Drawable icon) {
        SettingsListItem item = new SettingsListItem(
                null,
                setCorrectCase(appName),
                icon,
                settingsFragment
        );
        mSettingsAppList.add(item);
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
    protected void onResume() {
        super.onResume();
        Log.d("Giraf settings debugging", "SettingsActivity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Giraf settings debugging", "SettingsActivity onPause");
    }

    @Override
    public void onUserChanged(AdapterView<?> parent, View view, int position, long id) {
        return;
    }
}