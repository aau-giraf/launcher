package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Vagner on 01-05-14.
 */
public class AppManagementFragment extends Fragment {

    private Activity mActivity;
    private FragmentManager mFragManager;
    private Fragment mGirafFragment;
    private Fragment mAndroidFragment;
    private Fragment mActiveFragment;
    private Fragment mFragmentContainer;

    private TextView mGirafButton;
    private TextView mAndroidButton;
    private TextView mGooglePlayButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment_main,
                container, false);

        this.setRetainInstance(true);

        mGirafFragment = new GirafFragment();
        mAndroidFragment = new AndroidFragment();

        mActivity = this.getActivity();

        mGirafButton = (TextView)view.findViewById(R.id.settings_giraf_button);
        mAndroidButton = (TextView)view.findViewById(R.id.settings_android_button);
        mGooglePlayButton = (TextView)view.findViewById(R.id.settings_googleplay_button);
        this.setButtonListeners();

        mFragManager = this.getFragmentManager();
        mFragmentContainer = mFragManager.findFragmentById(R.id.app_settings_fragmentlayout);

        if (mFragmentContainer == null) {
            {
                mFragmentContainer = mGirafFragment;
                mActiveFragment = mFragmentContainer;
            }
            mFragManager.beginTransaction().add(R.id.app_settings_fragmentlayout, mFragmentContainer)
                    .commit();
        }

        return view;
    }

    private void setButtonListeners() {
        mGirafButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(mGirafFragment);
                focusButton(mGirafButton);
            }
        });

        mAndroidButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(mAndroidFragment);
                focusButton(mAndroidButton);
            }
        });

        mGooglePlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String appPackageName = mActivity.getPackageName(); // getPackageName() from Context or Activity object
                try {
                    // This TaskStackBuilder makes sure that the Play Store returns to this mActivity after having been closed.
                    TaskStackBuilder.create(mActivity)
                            .addParentStack(mActivity)
                            .addNextIntentWithParentStack(new Intent(mActivity.getApplicationContext(), mActivity.getClass()))
                            .addNextIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + appPackageName)))
                            .startActivities();
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        focusButton(mGirafButton);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        focusButton(mGirafButton);
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction ft = mFragManager.beginTransaction();
        ft.replace(R.id.app_settings_fragmentlayout, fragment);
        ft.commit();
    }

    private void focusButton(TextView clickedView)
    {
        mGirafButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        mAndroidButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        mGooglePlayButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);

        mGirafButton.setTypeface(Typeface.DEFAULT);
        mAndroidButton.setTypeface(Typeface.DEFAULT);
        mGooglePlayButton.setTypeface(Typeface.DEFAULT);

        clickedView.setTypeface(Typeface.DEFAULT_BOLD);
        clickedView.setBackgroundResource(android.R.color.holo_orange_dark);
    }
}
