package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;

import java.util.ArrayList;

/**
 * This fragment contains all of the elements on the left side of SettingsActivity.
 * This includes both the ListView with the tabs for all the different settings and
 * the Profile selector in the top left corner.
 */
public class SettingsListFragment extends Fragment {

    private ListView settingsListView;

    private SettingsListAdapter adapter;


    // Callback to containing Activity implementing the SettingsListFragmentListener interface
    private SettingsListFragmentListener callback;

    /**
     * Interface to be implemented by the activity supporting callbacks from this fragment.
     *
     * @see SettingsActivity
     */

    public interface SettingsListFragmentListener {
        /**
         * Used when an item in the ListView has been clicked
         * to send the to-be-active fragment back to the Activity.
         *
         * @param fragment the fragment
         */
        public void setActiveFragment(Fragment fragment);

        /**
         * Used when an item in the ListView has been clicked
         * to send the to-be-active fragment back to the Activity.
         *
         * @param fragment the fragment
         */
        public void setActiveFragment(android.support.v4.app.Fragment fragment);

        /**
         * Reloads the activity when user settings should be refreshed
         * based on a new profile selection.
         *
         * @see SettingsListFragment
         */
        public void reloadActivity();

        /**
         * Method is called to load applications into ListView.
         *
         * @return A list of apps that should be added to the ListView, containing only valid/available apps.
         */
        public ArrayList<SettingsListItem> getInstalledSettingsApps();

        /**
         * Called whenever a new user has been selected.
         *
         * @param profile The selected profile.
         */
        public void setCurrentUser(Profile profile);
    }

    /**
     * In OnCreateView we initialize most of the varibles needed by the class.
     * We do it in onCreateView instead of in OnCreate, since we are working with a Fragment and not an Activity.
     * More information about what exactly it does can be found in comments inside.
     *
     * @param inflater           The inflator used to inflate the layout
     * @param container          The container to be inflated
     * @param savedInstanceState The previously SavedInstanceState
     * @return The created View.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.settings_fragment_list, container, false);

        settingsListView = (ListView) view.findViewById(R.id.settingsListView);

        final GirafPictogramItemView mProfileButton = (GirafPictogramItemView)
            view.findViewById(R.id.profile_widget_settings);

        ProfileController profileController = new ProfileController(getActivity());

        final long childId = getActivity().getIntent().getLongExtra(Constants.CHILD_ID, -1);

        Profile currentUser;

        // The childId is -1 meaning that no childs are available
        if (childId == -1) {
            currentUser = profileController.getById(getActivity().getIntent()
                .getLongExtra(Constants.GUARDIAN_ID, -1));
        } else { // A child is found - set it as active and add its profile selector
            currentUser = profileController.getById(childId);
        }
        // Notify about the current user
        callback.setCurrentUser(currentUser);

        // Instantiates a new adapter to render the items in the ListView with a list of installed (available) apps
        adapter = new SettingsListAdapter(getActivity(), settingsListView, callback.getInstalledSettingsApps());
        // Set the new adapter in the ListView
        settingsListView.setAdapter(adapter);

        // Set listeners for loaded user interface components
        setListeners();

        //Load the correct profile picture for the choosen profile
        mProfileButton.setImageModel(currentUser, this.getResources().getDrawable(R.drawable.no_profile_pic));
        mProfileButton.setTitle(currentUser.getName());

        return view;
    }

    /**
     * This makes sure that the container activity has implemented
     * the callback interface. If not, it throws an exception
     *
     * @param activity The activity being attached
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (SettingsListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                " must implement SettingsListFragmentListener");
        }
    }

    /**
     * Set the selection in the adapter when the activity is created/reloaded
     * This is important when reloading the activity to show settings for
     * selected user.
     *
     * @param savedInstanceState The previously SavedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter.setSelected(0);
    }

    /**
     * If the activity should be garbage collected, the adapter should retain its selection.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.setSelected(0);
    }

    /**
     * Sets the listeners to be used by the components in SettingsListFragment.
     * Called in onCreate.
     */
    private void setListeners() {
        // Handles a clicked row in the ListView
        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsListItem item = (SettingsListItem) parent.getAdapter().getItem(position);

                // If the item contains a fragment, set it as active
                if (item instanceof FragmentSettingsListItem) {

                    FragmentSettingsListItem fragmentItem = ((FragmentSettingsListItem) item);

                    // Notify class implementing the callback interface that a new fragment has been selected

                    if (fragmentItem.fragment == null) {
                        callback.setActiveFragment(fragmentItem.supportFragment);
                    } else {
                        callback.setActiveFragment(fragmentItem.fragment);
                    }

                    // Update the adapter to reflect the selection
                    adapter.setSelected(position);
                } else if (item instanceof IntentSettingsListItem) {
                    // Otherwise it must be an intent
                    try {
                        // Start a new activity with the intent
                        startActivity(((IntentSettingsListItem) item).intent);
                    } catch (ActivityNotFoundException e) {
                        // Handle exception if the intended activity can not be started.
                        Toast.makeText(parent.getContext(),
                            R.string.settings_activity_not_found_msg, Toast.LENGTH_SHORT).show();
                    } finally {
                        // Notify adapter to redraw views since we want to reset the visual style
                        // of the selected since its state should not be preserved (= reset selected item
                        // to the item selected when starting the Intent.
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }
}