package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Vagner on 01-05-14.
 */
public class AppManagementSettings extends Fragment {

    private static final String MARKET_SEARCH_APP_URI = "market://search?q=pub:";
    private static final String MARKET_SEARCH_WEB_URI = "http://play.google.com/store/search?q=pub:";
    //TODO: Remember to change our publisher name when Giraf has been published on Google Play
    private static final String PUBLISHER_NAME = "AAU Giraf";

    private Activity mActivity;
    private FragmentManager mFragManager;
    private Fragment mGirafFragment;
    private Fragment mAndroidFragment;
    private Fragment mActiveFragment;
    private Fragment mFragmentContainer;

    private Button mGirafButton;
    private Button mAndroidAppsButton;
    private Button mGooglePlayButton;
    private Button mAndroidSettingsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment_main,
                container, false);

        this.setRetainInstance(true);

        mGirafFragment = new GirafFragment();
        mAndroidFragment = new AndroidFragment();

        mActivity = this.getActivity();

        mGirafButton = (Button)view.findViewById(R.id.settings_giraf_button);
        mAndroidAppsButton = (Button)view.findViewById(R.id.settings_android_button);
        mGooglePlayButton = (Button)view.findViewById(R.id.settings_googleplay_button);
        mAndroidSettingsButton = (Button)view.findViewById(R.id.settings_androidsettings_button);
        this.setButtonListeners();

        mFragManager = this.getFragmentManager();
        mFragmentContainer = mFragManager.findFragmentById(R.id.app_settings_fragmentlayout);

        if (mFragmentContainer == null) {
            {
                mFragmentContainer = mGirafFragment;
                focusButton(mGirafButton);
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

        mAndroidAppsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(mAndroidFragment);
                focusButton(mAndroidAppsButton);
            }
        });

        mAndroidSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mGooglePlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_SEARCH_APP_URI + PUBLISHER_NAME)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_SEARCH_WEB_URI + PUBLISHER_NAME)));
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction ft = mFragManager.beginTransaction();
        ft.replace(R.id.app_settings_fragmentlayout, fragment);
        ft.commit();
    }

    private void focusButton(Button clickedButton)
    {
        mGirafButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        mAndroidAppsButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        mGooglePlayButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);

        clickedButton.setBackgroundResource(android.R.color.holo_orange_light);
    }
}
