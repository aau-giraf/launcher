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

    private Activity mActivity;
    boolean mSavedInstance;
    private ListView mSettingsListView;
    private SettingsListAdapter mAdapter;
    private Spinner mUserSpinner;
    OnItemClickedListener mCallback; // Callback to containing Activity implementing the OnItemClickedListener interface

    // Container Activity must implement this interface
    public interface OnItemClickedListener {
        public void onItemClicked(int position);
        public void onFragmentChanged(Fragment fragment);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If just created, savedInstanceState is null
        if (savedInstanceState == null) {
            Log.d("Giraf Settings debugging", "onCreate savedInstanceState == null");
            mSavedInstance = false;
        }
        else {
            Log.d("Giraf Settings debugging", "onCreate savedInstanceState != null");
            mSavedInstance = true;
        }
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Giraf Settings debugging", "onCreateView");
        View view = inflater.inflate(R.layout.settings_fragment_list, container, false);

        Log.d(getTag(), "Finding settingsListView");
        mSettingsListView =  (ListView) view.findViewById(R.id.settingsListView);

        Log.d(getTag(), "Finding spinnerUser");
        mUserSpinner = (Spinner) view.findViewById(R.id.spinnerUser);

        Log.d(getTag(), "Setting spinner OnItemSelectedListener");
        mUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Giraf Settings debugging", "userSpinner onItemSelected");
                String user = mUserSpinner.getSelectedItem().toString();
                Toast.makeText(mActivity, "Du har klikket på " + user, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Giraf Settings debugging", "onStart()");

        mAdapter = (SettingsListAdapter)mSettingsListView.getAdapter();

        mSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsListItem item = (SettingsListItem)parent.getAdapter().getItem(position);
                mCallback.onItemClicked(position);
                mCallback.onFragmentChanged(item.mAppFragment);
                mAdapter.setSelected(position);
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
            mCallback = (OnItemClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemClickedListener");
        }
    }
}