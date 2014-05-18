package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.support.v4.app.NotificationCompat;
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

/**
 * This fragment contains all of the elements on the left side of SettingsActivity.
 * This includes both the ListView with the tabs for all the different settings and
 * the Profile selector in the top left corner.
 */
public class SettingsListFragment extends Fragment {

    /**
     * The variables of the class.
     * mSettingsListView is the ListView containing all the SettingsListItems. @see dk.aau.cs.giraf.launcher.settings.SettingsListItem
     * mProfileName is the textView below the profile selector, showing the name of the current user
     * mAdapter is the SettingsListAdapter attached to the ListView.
     * mProfileButton is the ProfileSelector widget button, created by the GUI group. @see dk.aau.cs.giraf.gui.GWidgetProfileSelection
     * mLoggedInGuardian is the currently logged in guardian and, if the current user is a child, the guardian of that child.
     * mCurrentUser is the current user.
     * mProfileSelector is the ProfileSelector Widget itself, not the button.@see dk.aau.cs.giraf.gui.GProfileSelector
     */
    private ListView mSettingsListView;
    private TextView mProfileName;
    private SettingsListAdapter mAdapter;
    private GWidgetProfileSelection mProfileButton;

    private Profile mLoggedInGuardian;
    public static Profile mCurrentUser;
    private GProfileSelector mProfileSelector;

    // Callback to containing Activity implementing the SettingsListFragmentListener interface
    public SettingsListFragmentListener mCallback;

    /**
     * Interface to be implemented by the activity supporting callbacks from this fragment.
     * @see SettingsActivity
     */
    public interface SettingsListFragmentListener {
        /**
         * Used when an item in the ListView has been clicked
         * to send the to-be-active fragment back to the Activity.
         * @param fragment
         */
        public void setActiveFragment(Fragment fragment);

        /**
         * Reloads the activity when user settings should be refreshed
         * based on a new profile selection.
         * @see SettingsListFragment
         */
        public void reloadActivity();

        /**
         * Method is called to load applications into ListView.
         * @return A list of apps that should be added to the ListView,
         * containing only valid/available apps.
         */
        public ArrayList<SettingsListItem> getInstalledSettingsApps();

        /**
         * Called whenever a new user has been selected.
         * @param profile The selected profile.
         */
        public void setCurrentUser(Profile profile);
    }

    /**
     * In OnCreateView we initialize most of the varibles needed by the class.
     * We do it in onCreateView instead of in OnCreate, since we are working with a Fragment and not an Activity.
     * More information about what exactly it does can be found in comments inside.
     * @param inflater The inflator used to inflate the layout
     * @param container The container to be inflated
     * @param savedInstanceState The previously SavedInstanceState
     * @return The created View.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment_list, container, false);

        mSettingsListView =  (ListView) view.findViewById(R.id.settingsListView);

        mProfileButton = (GWidgetProfileSelection) view.findViewById(R.id.profile_widget_settings);
        mProfileName = (TextView) view.findViewById(R.id.profile_selected_name);

        ProfileController pc = new ProfileController(getActivity());
        mLoggedInGuardian = pc.getProfileById(getActivity().getIntent().getIntExtra(Constants.GUARDIAN_ID, -1));

        int childID = getActivity().getIntent().getIntExtra(Constants.CHILD_ID, -1);

        // The childID is -1 meaning that no childs are available
        if(childID == -1)
        {
            mCurrentUser = pc.getProfileById(getActivity().getIntent().getIntExtra(Constants.GUARDIAN_ID, -1));
            mProfileSelector = new GProfileSelector(getActivity(), mLoggedInGuardian, null);
        }
        // A child is found - set it as active and add its profile selector
        else
        {
            mCurrentUser = pc.getProfileById(childID);
            mProfileSelector = new GProfileSelector(getActivity(), mLoggedInGuardian, mCurrentUser);
        }
        // Notify about the current user
        mCallback.setCurrentUser(mCurrentUser);
        // Update the name of the user
        mProfileName.setText(mCurrentUser.getName());

        // Instantiates a new adapter to render the items in the ListView with a list of installed (available) apps
        mAdapter = new SettingsListAdapter(getActivity(), mSettingsListView, mCallback.getInstalledSettingsApps());
        // Set the new adapter in the ListView
        mSettingsListView.setAdapter(mAdapter);

        // Set listerners for loaded user interface components
        setListeners();

        //Load the correct profile picture for the choosen profile
        mProfileButton.setImageBitmap(mCurrentUser.getImage());

        return view;
    }

    /**
     * This makes sure that the container activity has implemented
     * the callback interface. If not, it throws an exception
     * @param activity The activity being attached
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (SettingsListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SettingsListFragmentListener");
        }
    }

    /**
     * Set the selection in the adapter when the activity is created/reloaded
     * This is important when reloading the activity to show settings for
     * selected user.
     * @param savedInstanceState The previously SavedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter.setSelected(0);
    }

    /**
     * If the activity should be garbage collected, the adapter should retain its selection
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.setSelected(0);
    }

    /**
     * Sets the listeners to be used by the components in SettingsListFragment.
     * Called in onCreate.
     */
    private void setListeners() {
        // Handles a clicked row in the ListView
        mSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsListItem item = (SettingsListItem) parent.getAdapter().getItem(position);
                // If the item contains a fragment, set it as active
                if (item.mAppFragment != null) {
                    // Notify class implementing the callback interface that a new fragment has been selected
                    mCallback.setActiveFragment(item.mAppFragment);
                    // Update the adapter to reflect the selection
                    mAdapter.setSelected(position);
                }
                // Otherwise it must be an intent
                else if (item.mIntent != null) {
                    try {
                        // Start a new activity with the intent
                        startActivity(item.mIntent);
                    }
                    // Handle exception if the intended activity can not be started.
                    catch (ActivityNotFoundException ex) {
                        Toast.makeText(parent.getContext(), R.string.settings_activity_not_found_msg, Toast.LENGTH_SHORT).show();
                    }
                    finally {
                        // Notify adapter to redraw views since we want to reset the visual style
                        // of the selected since its state should not be preserved (= reset selected item
                        // to the item selected when starting the Intent.
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        // Handles a click on the profile selector
        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the profile select dialog
                mProfileSelector.show();
            }
        });

        // Set listerner for the dialog
        setProfileSelectorClickListener();
    }

    /**
     * This is used to set the onClickListener for a new ProfileSelector
     * It must be used everytime a new selector is set.
     * */
    private void setProfileSelectorClickListener(){
        // Handles a selection returned from the profile select dialog
        mProfileSelector.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ProfileController pc = new ProfileController(getActivity());
                // Get a profile from id returned from dialog
                mCurrentUser = pc.getProfileById((int) id);
                // Close it when a selection has been made
                mProfileSelector.dismiss();

                // Set a new profile select button according to the role of active user
                if (mCurrentUser.getRole() != Profile.Roles.GUARDIAN)
                    mProfileSelector = new GProfileSelector(getActivity(), mLoggedInGuardian, mCurrentUser);
                else
                    mProfileSelector = new GProfileSelector(getActivity(), mLoggedInGuardian, null);

                // Notify that a profile selection has been made
                mCallback.setCurrentUser(mCurrentUser);
                // Reload activity to reflect different user settings
                mCallback.reloadActivity();
                mProfileButton.setImageBitmap(mCurrentUser.getImage());


                // Call this method again to set listerners for the newly selected profile
                setProfileSelectorClickListener();
            }
        });
    }
}