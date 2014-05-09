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

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementSettings;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.GirafFragment;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.LauncherSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.WombatSettings;

public class SettingsActivity extends Activity
        implements SettingsListFragment.SettingsListFragmentListener {

    private FragmentManager mFragManager;
    private Fragment mActiveFragment;
    private ArrayList<SettingsListItem> mAppList;

    private Profile mLoggedInGuardian;
    public static Profile mCurrentUser;
    private GProfileSelector profileSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Giraf settings debugging", "SettingsActivity onCreate");
        setContentView(R.layout.settings);

        ProfileController pc = new ProfileController(this);
        mLoggedInGuardian = pc.getProfileById(this.getIntent().getIntExtra(Constants.GUARDIAN_ID, -1));

        int childID = this.getIntent().getIntExtra(Constants.CHILD_ID, -1);
        if(childID == -1)
        {
            mCurrentUser = pc.getProfileById(this.getIntent().getIntExtra(Constants.GUARDIAN_ID, -1));
            profileSelector = new GProfileSelector(this, mLoggedInGuardian, null);
        }
        else
        {
            mCurrentUser = pc.getProfileById(childID);
            profileSelector = new GProfileSelector(this, mLoggedInGuardian, mCurrentUser);
        }

        mFragManager = this.getFragmentManager();
        Fragment settingsFragment = mFragManager.findFragmentById(R.id.settingsContainer);

        if (settingsFragment == null) {
            SettingsListItem item = getInstalledSettingsApps().get(0);
            mActiveFragment = item.mAppFragment;
        }

        mFragManager.beginTransaction().add(R.id.settingsContainer, mActiveFragment)
                .commit();

        SetProfileSelector();
    }

    public ArrayList<SettingsListItem> getInstalledSettingsApps(){
        mAppList = new ArrayList<SettingsListItem>();

        addApplicationByPackageName("dk.aau.cs.giraf.launcher", new LauncherSettings(LauncherUtility.getSharedPreferenceUser(getApplicationContext())));
        addApplicationByPackageName("dk.aau.cs.giraf.wombat", new WombatSettings());
        addApplicationByName("Android", new AppManagementSettings(), getResources().getDrawable(R.drawable.android_icon));

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
                null,
                setCorrectCase(appName),
                icon,
                settingsFragment
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

    private void reloadCurrentFragment()
    {
        setActiveFragment(getInstalledSettingsApps().get(0).mAppFragment);
    }

    @Override
    public void onUserChanged(View view) {
        profileSelector.show();
    }

    /**
     * This is used to set the onClickListener for a new ProfileSelector
     * It must be used everytime a new selector is set.
     * */
    private void SetProfileSelector()
    {
        profileSelector.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProfileController pc = new ProfileController(SettingsActivity.this);
                mCurrentUser = pc.getProfileById((int)l);
                profileSelector.dismiss();

                if(mCurrentUser.getRole() != Profile.Roles.GUARDIAN)
                    profileSelector = new GProfileSelector(SettingsActivity.this, mLoggedInGuardian, mCurrentUser);
                else
                    profileSelector = new GProfileSelector(SettingsActivity.this, mLoggedInGuardian, null);

                reloadCurrentFragment();

                SetProfileSelector();
            }
        });

        SettingsListFragment fragment = (SettingsListFragment) mFragManager.findFragmentById(R.id.settingsListFragment);
        fragment.setSelectedUserName(mCurrentUser.getName());
    }
}