package dk.aau.cs.giraf.launcher.giraffragments.appfragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Vagner on 01-05-14.
 */
public class AppSettingsPagerAdapter extends FragmentStatePagerAdapter {
    public AppSettingsPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public android.support.v4.app.Fragment getItem(int i) {
        android.support.v4.app.Fragment fragment = new GirafFragment();

        if(i == 1)
            fragment = new AndroidFragment();
        else if(i == 2)
            fragment = new PlayStoreFragment();

        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(GirafFragment.ARG_OBJECT, i + 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}

