package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.SwipeAdapter;

/**
 * Created by Caspar on 10-03-2016.
 */
public class ProfileChooserActivity extends Activity {
    ViewPager viewPager;
    SwipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_chooser);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        adapter = new SwipeAdapter(this);
        viewPager.setAdapter(adapter);
    }
}
