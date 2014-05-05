package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.giraffragments.appfragments.AppManagementFragment;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CarsSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CatSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.CrocSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.LauncherSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.ParrotSettings;
import dk.aau.cs.giraf.settingslib.settingslib.Fragments.WombatSettings;

public class SettingsActivity extends Activity {

    private FragmentManager mFragManager;
    private ListView mSettingsListView;
    private SettingsListAdapter mAdapter;
    private Spinner mUserSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mSettingsListView = (ListView) findViewById(R.id.settingsListView);
        mUserSpinner = (Spinner) findViewById(R.id.spinnerUser);

        // Adds data to SettingsListFragment
        populateListFragment();

        mFragManager = this.getFragmentManager();
        Fragment settingsContainer = mFragManager.findFragmentById(R.id.settingsContainer);

        if (settingsContainer == null) {
            {
                SettingsListItem item = (SettingsListItem) mAdapter.getItem(0);
                settingsContainer = item.appFragment;
            }
            mFragManager.beginTransaction().add(R.id.settingsContainer, settingsContainer)
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsListItem item = (SettingsListItem) parent.getAdapter().getItem(position);

                if (item != null) {
                    replaceFragment(item.appFragment);
                } else {
                    throw new NullPointerException("Fragment does not exist!");
                }

                view.setSelected(true);
                View shadowView = view.findViewById(R.id.settingsListRowShadow);
                shadowView.setVisibility(View.GONE);

                for (int i = 0; i < mAdapter.getCount(); i++) {

                }
            }
        });

        mUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String user = mUserSpinner.getSelectedItem().toString();
                Toast.makeText(SettingsActivity.this, "Du har klikket på " + user, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.findViewById(R.layout.settings_fragment_list_row);
            }
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction ft = mFragManager.beginTransaction();
        ft.replace(R.id.settingsContainer, fragment);
        ft.commit();
    }

    private void populateListFragment(){
        ArrayList<SettingsListItem> appList = new ArrayList<SettingsListItem>();

        SettingsListItem item1 = new SettingsListItem("Giraf", getResources().getDrawable(R.drawable.app_icon),
                new LauncherSettings(), new ColorDrawable(0xffffdd55));
        SettingsListItem item2 = new SettingsListItem("Cat", getResources().getDrawable(R.drawable.app_icon),
                new CatSettings(), new ColorDrawable(0xffac9393));
        SettingsListItem item3 = new SettingsListItem("Wombat", getResources().getDrawable(R.drawable.app_icon),
                new WombatSettings(), new ColorDrawable(0xffffe680));
        SettingsListItem item4 = new SettingsListItem("Parrot", getResources().getDrawable(R.drawable.app_icon),
                new ParrotSettings(), new ColorDrawable(0xff808000));
        SettingsListItem item5 = new SettingsListItem("Croc", getResources().getDrawable(R.drawable.app_icon),
                new CrocSettings(), new ColorDrawable(0xff5fd35f));
        SettingsListItem item6 = new SettingsListItem("Cars", getResources().getDrawable(R.drawable.app_icon),
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
        mAdapter = new SettingsListAdapter(this, appList);
        mSettingsListView.setAdapter(mAdapter);
    }
} 