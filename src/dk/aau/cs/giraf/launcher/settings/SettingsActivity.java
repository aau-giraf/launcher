package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementSettings;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementFragment;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CarsSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CatSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CrocSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.LauncherSettings;

public class SettingsActivity extends Activity
        implements SettingsListFragment.SettingsListFragmentListener {

    private FragmentManager mFragManager;
    private SettingsListAdapter mAdapter;
    private ListView mSettingsListView;
    private Fragment mActiveFragment;
    private ArrayList<SettingsListItem> mAppList;
    private Profile mLoggedInGuardian;
    private Profile mCurrentUser;
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
        SetProfileSelector();

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
        mAppList = new ArrayList<SettingsListItem>();

        addApplicationByPackageName("dk.aau.cs.giraf.launcher", new LauncherSettings());
        addApplicationByName("Android", new AppManagementSettings(), getResources().getDrawable(R.drawable.android_icon));

        // Getting mAdapter by passing list data
        mAdapter = new SettingsListAdapter(SettingsActivity.this, mAppList);
        mSettingsListView.setAdapter(mAdapter);
    }

    private void addApplicationByPackageName(String packageName, Fragment fragment) {
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo appInfo = null;

        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String appName;
        if (appInfo != null) {
            appName = setCorrectCase(pm.getApplicationLabel(appInfo).toString());
        }
        else
            appName = "(unknown)";

        final Drawable appIcon = (
                appInfo != null ?
                        pm.getApplicationIcon(appInfo) :
                        getResources().getDrawable(R.drawable.giraf_icon)
        );

        SettingsListItem item = new SettingsListItem(
                appName,
                appIcon,
                fragment
        );
        mAppList.add(item);
    }

    private void addApplicationByName(String appName, Fragment settingsFragment, Drawable icon) {
        SettingsListItem item = new SettingsListItem(
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
    public void onUserChanged(View view) {
        profileSelector.show();
        return;
    }

    /**
     * This is used to set the onClickListener for a new ProfileSelector
     * It must be used everytime a new selector is set.
     * */
    private void SetProfileSelector()
    {
        final Context context = this;
        profileSelector.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProfileController pc = new ProfileController(context);
                mCurrentUser = pc.getProfileById((int)l);
                profileSelector.dismiss();

                if(mCurrentUser.getRole() != Profile.Roles.GUARDIAN)
                    profileSelector = new GProfileSelector(context, mLoggedInGuardian, mCurrentUser);
                else
                    profileSelector = new GProfileSelector(context, mLoggedInGuardian, null);

                SetProfileSelector();
            }
        });
    }
}