package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import dk.aau.cs.giraf.launcher.R;

public class SettingsListFragment extends Fragment {

    private ListView mSettingsListView;
    private SettingsListAdapter mAdapter;
    private Spinner mUserSpinner;
    SettingsListFragmentListener mCallback; // Callback to containing Activity implementing the SettingsListFragmentListener interface

    // Container Activity must implement this interface
    public interface SettingsListFragmentListener {
        public void setActiveFragment(Fragment fragment);
        public void onUserChanged(AdapterView<?> parent, View view, int position, long id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Giraf Settings debugging", "onCreateView");
        View view = inflater.inflate(R.layout.settings_fragment_list, container, false);

        Log.d(getTag(), "Finding settingsListView");
        mSettingsListView =  (ListView) view.findViewById(R.id.settingsListView);

        Log.d(getTag(), "Finding spinnerUser");
        mUserSpinner = (Spinner) view.findViewById(R.id.spinnerUser);

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
        mUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Giraf Settings debugging", "userSpinner onItemSelected");
                mCallback.onUserChanged(parent, view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
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
}