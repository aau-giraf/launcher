package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import dk.aau.cs.giraf.gui.GWidgetProfileSelection;
import dk.aau.cs.giraf.launcher.R;

public class SettingsListFragment extends Fragment {

    private ListView mSettingsListView;
    private TextView mProfileName;
    private SettingsListAdapter mAdapter;
    private GWidgetProfileSelection mProfileButton;
    SettingsListFragmentListener mCallback; // Callback to containing Activity implementing the SettingsListFragmentListener interface

    // Container Activity must implement this interface
    public interface SettingsListFragmentListener {
        public void setActiveFragment(Fragment fragment);
        public void onUserChanged(View view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Giraf Settings debugging", "onCreateView");
        View view = inflater.inflate(R.layout.settings_fragment_list, container, false);

        Log.d(getTag(), "Finding settingsListView");
        mSettingsListView =  (ListView) view.findViewById(R.id.settingsListView);

        Log.d(getTag(), "Finding spinnerUser");
        mProfileButton = (GWidgetProfileSelection) view.findViewById(R.id.profile_widget_settings);
        mProfileName = (TextView) view.findViewById(R.id.profile_selected_name);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Giraf Settings debugging", "onStart()");

        mAdapter = (SettingsListAdapter)mSettingsListView.getAdapter();

        Log.d(getTag(), "Setting listview OnItemSelectedListener");
        mSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsListItem item = (SettingsListItem)parent.getAdapter().getItem(position);
                mCallback.setActiveFragment(item.mAppFragment);
                mAdapter.setSelected(position);
            }
        });

        Log.d(getTag(), "Setting spinner OnItemSelectedListener");
        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onUserChanged(v);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("Giraf Settings debugging", "onAttach");

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (SettingsListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SettingsListFragmentListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.setSelected(0);
    }

    public void setSelectedUserName(String name) {
        mProfileName.setText(name);
    }
}