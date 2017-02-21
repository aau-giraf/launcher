package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.launcher.R;

/**
 * This fragment contains the settings for Launcher itself, meaning the iconsize and
 * whether to show the starting animation or not.
 */
public class AboutFragment extends Fragment {

    //ToDo Write JavaDoc
    public static AboutFragment newInstance() {
        final AboutFragment newFragment = new AboutFragment();

        Bundle args = new Bundle();
        newFragment.setArguments(args);

        return newFragment;
    }

    /**
     * OnCreate is per usual overridden to instantiate the mayority of the variables of the class
     * It loads the SharedPreferences of the user and sets the layout accordingly.
     * @param savedInstanceState The previously SavedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {

        final View view = inflater.inflate(R.layout.settings_about, null);

        return view;
    }
}
