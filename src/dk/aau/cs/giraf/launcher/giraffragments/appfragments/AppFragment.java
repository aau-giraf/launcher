package dk.aau.cs.giraf.launcher.giraffragments.appfragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Vagner on 01-05-14.
 */
public class AppFragment extends Fragment{

    Activity activity;
    private FragmentManager mFragManager;
    private Fragment girafFragment;
    private Fragment androidFragment;
    private Fragment googlePlayFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment_main,
                container, false);

        girafFragment = new GirafFragment();
        androidFragment = new AndroidFragment();
        googlePlayFragment = new GooglePlayFragment();


        activity = this.getActivity();

        final Button girafButton = (Button)view.findViewById(R.id.settings_giraf_button);
        final Button androidButton = (Button)view.findViewById(R.id.settings_android_button);
        final Button googlePlayButton = (Button)view.findViewById(R.id.settings_googleplay_button);

        mFragManager = this.getFragmentManager();
        Fragment fragmentContainer = mFragManager.findFragmentById(R.id.app_settings_fragmentlayout);

        if (fragmentContainer == null) {
            {
                fragmentContainer = girafFragment;
            }
            mFragManager.beginTransaction().add(R.id.app_settings_fragmentlayout, fragmentContainer).commit();
        }

        girafButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(girafFragment);
                if(0 == 1)
                {
                    girafButton.setBackgroundResource(R.color.gBrowncolor);
                    androidButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
                    googlePlayButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
                }

            }
        });

        androidButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(androidFragment);
                if(0 == 1)
                {
                girafButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
                androidButton.setBackgroundResource(R.color.gBrowncolor);
                googlePlayButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
                }
            }
        });

        googlePlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(googlePlayFragment);
                if(0 == 1)
                {
                girafButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
                androidButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
                googlePlayButton.setBackgroundResource(R.color.gBrowncolor);
                }
                final String appPackageName = activity.getPackageName(); // getPackageName() from Context or Activity object
                try {
                    // This TaskStackBuilder makes sure that the Play Store returns to this activity after having been closed.
                    TaskStackBuilder.create(activity)
                            .addParentStack(activity)
                            .addNextIntentWithParentStack(new Intent(activity.getApplicationContext(), activity.getClass()))
                            .addNextIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + appPackageName)))
                            .startActivities();
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        return view;

    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction ft = mFragManager.beginTransaction();
        ft.replace(R.id.app_settings_fragmentlayout, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}
