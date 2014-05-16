package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import dk.aau.cs.giraf.launcher.R;

/**
 * This is fragment that is loaded into the container of the SettingsActivity.
 * This fragment contains a container itself, which it loads GirafFragment or AndroidFragment into
 * Also implements the buttons needed to switch between the two fragments
 * Finally, it also implements the button used to open the Play Store.
 */
public class AppManagementSettings extends Fragment {

    /** These strings are used for opening the Play Store, searching for the correct items*/
    private static final String MARKET_SEARCH_APP_URI = "market://search?q=pub:";
    private static final String MARKET_SEARCH_WEB_URI = "http://play.google.com/store/search?q=pub:";
    //TODO: Remember to change our publisher name when Giraf has been published on Google Play
    private static final String PUBLISHER_NAME = "Giraf Autism Apps";

    /** The FragmentManager is used to manage whcih fragments are current in the FragmentContainer*/
    /** The girafFragment and the androidFragment are the fragments to be inflated into the container*/
    /** The fragmentContainer that contains the inflated girafFragment or androidFragment*/
    private FragmentManager fragmentManager;
    private Fragment girafFragment;
    private Fragment androidFragment;
    private Fragment fragmentContainer;

    /** All the buttons that make the fragmentContainer inflate the correct fragment or open Play Store*/
    private Button girafAppsButton;
    private Button androidAppsButton;
    private Button googlePlayButton;

    /**
     * Because we are dealing with a Fragment, OnCreateView is where most of the variables are set.
     * @param inflater The inflater (Android takes care of this)
     * @param container The container, the ViewGroup, that the fragment should be inflate in.
     * @param savedInstanceState The previously saved instancestate
     * @return the inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment,
                container, false);

        this.setRetainInstance(true);

        /** Instantiated all the variables needed*/
        girafFragment = new GirafFragment();
        androidFragment = new AndroidFragment();
        girafAppsButton = (Button)view.findViewById(R.id.settings_giraf_button);
        androidAppsButton = (Button)view.findViewById(R.id.settings_android_button);
        googlePlayButton = (Button)view.findViewById(R.id.settings_googleplay_button);
        this.setButtonListeners();
        fragmentManager = this.getFragmentManager();
        fragmentContainer = fragmentManager.findFragmentById(R.id.app_settings_fragmentlayout);

        /** Choose the GIRAF pane and set the GIRAF button when the fragment has loaded.*/
        fragmentContainer = girafFragment;
        focusButton(girafAppsButton);
        fragmentManager.beginTransaction().add(R.id.app_settings_fragmentlayout, fragmentContainer)
                .commit();

        return view;
    }

    /**
     * This function sets the correct onClickListeners for the three buttons
     */
    private void setButtonListeners() {
        /** inflate the girafFragment when the girafAppsButton is pressed*/
        girafAppsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(girafFragment);
                focusButton(girafAppsButton);
            }
        });

        /** inflate the androidFragment when the androidAppsButton is pressed*/
        androidAppsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(androidFragment);
                focusButton(androidAppsButton);
            }
        });

        /** Attempt to open the Play Store. If it is not installed on the device, open Play Store in the browser instead.*/
        googlePlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    launchPlayStore();
                } catch (android.content.ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Replace active fragment by running the transaction in a new thread.
     * Adds responsiveness when loading list of installed apps.
     * @param fragment
     */
    private void replaceFragment(final Fragment fragment){
        new Runnable() {
            @Override
            public void run() {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.app_settings_fragmentlayout, fragment);
                ft.commit();
            }
        }.run();
    }

    /** Makes sure the button clicked appears visually as the button clicked.*/
    private void focusButton(Button clickedButton){
        girafAppsButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        androidAppsButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        googlePlayButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);

        clickedButton.setBackgroundResource(android.R.color.holo_orange_light);
    }

    /**
     * This function opens the Play Store for the user
     */
    public void launchPlayStore()
    {
        // look for intent able to process 'market://' uris
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));

        PackageManager packageManager = getActivity().getPackageManager();

        ComponentName playStoreComponentName=null;

        for(ResolveInfo resolveInfo : packageManager.queryIntentActivities(market, 0))
        {
            ActivityInfo activityInfo = resolveInfo.activityInfo;

            String packageName = activityInfo.applicationInfo.packageName;

            // looking for "com.android.vending", "com.google.android.finsky.activities.MainActivity"
            if (!packageName.contains("android"))// || !activityInfo.name.contains("android"))
                continue;

            // appname should be 'Play Store'
            // String appName = resolveInfo.loadLabel(packageManager).toString();
            playStoreComponentName =  new ComponentName(packageName, activityInfo.name);
            break;
        }

        if(playStoreComponentName!=null)
        {
            Intent intent = new Intent();
            intent.setComponent(playStoreComponentName);
            intent.setData(Uri.parse("market://"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);

            // launch Google Play Store app :-)
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);

            // fallback -> web browser
            startActivity(intent);
        }
    }
}
