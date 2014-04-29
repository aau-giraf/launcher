package dk.aau.cs.giraf.launcher.giraffragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import dk.aau.cs.giraf.launcher.R;

public class CarsSettingsFragment extends Fragment {

    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_giraf_fragments_testfragment,
                container, false);

        activity = this.getActivity();
        Button btn = (Button)view.findViewById(R.id.testFragmentButton);
        btn.setText("Cars");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "Her står der noget dumt tekst...", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
} 