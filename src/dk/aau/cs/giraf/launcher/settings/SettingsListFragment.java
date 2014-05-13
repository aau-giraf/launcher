package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.gui.GWidgetProfileSelection;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public class SettingsListFragment extends Fragment {

    private ListView mSettingsListView;
    private TextView mProfileName;
    private SettingsListAdapter mAdapter;
    private GWidgetProfileSelection mProfileButton;

    private Profile mLoggedInGuardian;
    public static Profile mCurrentUser;
    private GProfileSelector mProfileSelector;

    public SettingsListFragmentListener mCallback; // Callback to containing Activity implementing the SettingsListFragmentListener interface

    // Container Activity must implement this interface
    public interface SettingsListFragmentListener {
        public void setActiveFragment(Fragment fragment);
        public void reloadActivity();
        public ArrayList<SettingsListItem> getInstalledSettingsApps();
        public void setCurrentUser(Profile profile);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment_list, container, false);

        mSettingsListView =  (ListView) view.findViewById(R.id.settingsListView);

        mProfileButton = (GWidgetProfileSelection) view.findViewById(R.id.profile_widget_settings);
        mProfileName = (TextView) view.findViewById(R.id.profile_selected_name);

        ProfileController pc = new ProfileController(getActivity());
        mLoggedInGuardian = pc.getProfileById(getActivity().getIntent().getIntExtra(Constants.GUARDIAN_ID, -1));

        int childID = getActivity().getIntent().getIntExtra(Constants.CHILD_ID, -1);
        if(childID == -1)
        {
            mCurrentUser = pc.getProfileById(getActivity().getIntent().getIntExtra(Constants.GUARDIAN_ID, -1));
            mProfileSelector = new GProfileSelector(getActivity(), mLoggedInGuardian, null);
        }
        else
        {
            mCurrentUser = pc.getProfileById(childID);
            mProfileSelector = new GProfileSelector(getActivity(), mLoggedInGuardian, mCurrentUser);
        }
        mCallback.setCurrentUser(mCurrentUser);
        mProfileName.setText(mCurrentUser.getName());

        mAdapter = new SettingsListAdapter(getActivity(), mSettingsListView, mCallback.getInstalledSettingsApps());
        mSettingsListView.setAdapter(mAdapter);

        setListeners();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter.setSelected(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.setSelected(0);
    }

    private void setListeners() {
        mSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsListItem item = (SettingsListItem) parent.getAdapter().getItem(position);
                if (item.mAppFragment != null) {
                    mCallback.setActiveFragment(item.mAppFragment);
                    mAdapter.setSelected(position);
                }
                else if (item.mIntent != null) {
                    startActivity(item.mIntent);
                    mAdapter.setSelected(0);
                }
            }
        });

        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSelector.show();
            }
        });

        setProfileSelectorClickListener();
    }

    /**
     * This is used to set the onClickListener for a new ProfileSelector
     * It must be used everytime a new selector is set.
     * */
    private void setProfileSelectorClickListener()
    {
        mProfileSelector.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ProfileController pc = new ProfileController(getActivity());
                mCurrentUser = pc.getProfileById((int) id);
                mProfileSelector.dismiss();

                if (mCurrentUser.getRole() != Profile.Roles.GUARDIAN)
                    mProfileSelector = new GProfileSelector(getActivity(), mLoggedInGuardian, mCurrentUser);
                else
                    mProfileSelector = new GProfileSelector(getActivity(), mLoggedInGuardian, null);

                mCallback.setCurrentUser(mCurrentUser);
                mCallback.reloadActivity();

                setProfileSelectorClickListener();
            }
        });
    }
}