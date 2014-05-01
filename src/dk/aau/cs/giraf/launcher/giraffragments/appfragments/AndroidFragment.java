package dk.aau.cs.giraf.launcher.giraffragments.appfragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Vagner on 01-05-14.
 */
public class AndroidFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment_android,
                container, false);

        return view;
    }
}
