package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.settings.settingsappmanagement.AppManagementFragment;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CarsSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CatSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CrocSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.LauncherSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.ParrotSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.WombatSettings;

public class SettingsActivity extends Activity
        implements SettingsListFragment.SettingsListFragmentListener {

    private FragmentManager mFragManager;
    private SettingsListAdapter mAdapter;
    private ListView mSettingsListView;
    private Fragment mActiveFragment;

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
        ArrayList<SettingsListItem> appList = new ArrayList<SettingsListItem>();

        SettingsListItem item1 = new SettingsListItem("Giraf", getResources().getDrawable(R.drawable.giraf_icon),
                new LauncherSettings(), new ColorDrawable(0xffffdd55));
        SettingsListItem item2 = new SettingsListItem("Cat", getResources().getDrawable(R.drawable.giraf_icon),
                new CatSettings(), new ColorDrawable(0xffac9393));
        SettingsListItem item3 = new SettingsListItem("Wombat", getResources().getDrawable(R.drawable.giraf_icon),
                new WombatSettings(), new ColorDrawable(0xffffe680));
        SettingsListItem item4 = new SettingsListItem("Parrot", getResources().getDrawable(R.drawable.giraf_icon),
                new ParrotSettings(), new ColorDrawable(0xff808000));
        SettingsListItem item5 = new SettingsListItem("Croc", getResources().getDrawable(R.drawable.giraf_icon),
                new CrocSettings(), new ColorDrawable(0xff5fd35f));
        SettingsListItem item6 = new SettingsListItem("Cars", getResources().getDrawable(R.drawable.giraf_icon),
                new CarsSettings(), new ColorDrawable(0xff9de7e6));
        SettingsListItem item7 = new SettingsListItem("Android", getResources().getDrawable(R.drawable.android_icon),
                new AppManagementFragment(), new ColorDrawable(0xffe6e6e6));

        appList.add(item1);
        appList.add(item2);
        appList.add(item3);
        appList.add(item4);
        appList.add(item5);
        appList.add(item6);
        appList.add(item7);

        // Getting mAdapter by passing list data
        mAdapter = new SettingsListAdapter(SettingsActivity.this, appList);
        mSettingsListView.setAdapter(mAdapter);
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