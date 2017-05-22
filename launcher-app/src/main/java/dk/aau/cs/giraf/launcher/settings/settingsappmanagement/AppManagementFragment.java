package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.User;

/**
 * This is fragment that is loaded into the container of the SettingsActivity.
 * This fragment contains a container itself, which it loads GirafFragment or AndroidFragment into
 * Also implements the buttons needed to switch between the two fragments
 * Finally, it also implements the button used to open the Play Store.
 */
public class AppManagementFragment extends android.support.v4.app.Fragment {

    /**
     * These strings are used for opening the Play Store, searching for the correct items.
     */
    private static final String MARKET_SEARCH_APP_URI = "market://search?q=pub:";
    private static final String MARKET_SEARCH_WEB_URI = "http://play.google.com/store/search?q=pub:";
    //TODO: Remember to change our publisher name when Giraf has been published on Google Play
    private static final String PUBLISHER_NAME = "Giraf Autism Apps";
    private RequestQueueHandler handler;

    /** The FragmentManager is used to manage whcih fragments are current in the FragmentContainer.*/
    /** The girafFragment and the androidFragment are the fragments to be inflated into the container.*/
    /**
     * The fragmentContainer that contains the inflated girafFragment or androidFragment.
     */
    private android.support.v4.app.FragmentManager fragmentManager;
    //private GirafFragment girafFragment;
    //private AndroidFragment androidFragment;
    //private android.support.v4.app.Fragment fragmentContainer;

    /**
     * All the buttons that make the fragmentContainer inflate the correct fragment or open Play Store.
     */
    private Button girafAppsButton;
    private Button androidAppsButton;
    private Button googlePlayButton;

    /**
     * Because we are dealing with a Fragment, OnCreateView is where most of the variables are set.
     *
     * @param inflater           The inflater (Android takes care of this)
     * @param container          The container, the ViewGroup, that the fragment should be inflate in.
     * @param savedInstanceState The previously saved instancestate
     * @return the inflated view.
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings_appmanagement,
            container, false);

        this.setRetainInstance(true);

        /** Instantiated all the variables needed.*/
        //girafFragment = new GirafFragment();
        //androidFragment = new AndroidFragment();
        girafAppsButton = (Button) view.findViewById(R.id.settings_giraf_button);
        androidAppsButton = (Button) view.findViewById(R.id.settings_android_button);
        googlePlayButton = (Button) view.findViewById(R.id.settings_googleplay_button);
        this.setButtonListeners();

        /*
        * getChildFragmentManager() only works with Build.VERSION_CODES.JELLY_BEAN_MR1 or higher
        * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            fragmentManager = this.getChildFragmentManager();
        } else {
            fragmentManager = this.getFragmentManager();
        }

        /** Choose the GIRAF pane and set the GIRAF button when the fragment has loaded.*/
        //fragmentContainer = girafFragment;
        focusButton(girafAppsButton);
        handler = RequestQueueHandler.getInstance(getActivity().getApplicationContext());
        handler.get(User.class, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                GirafFragment fragment = new GirafFragment();
                fragment.setCurrentUser(response);
                replaceFragment(fragment);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        return view;
    }

    /**
     * This function sets the correct onClickListeners for the three buttons.
     */
    private void setButtonListeners() {
        /** inflate the girafFragment when the girafAppsButton is pressed*/
        girafAppsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handler = RequestQueueHandler.getInstance(getActivity().getApplicationContext());
                handler.get(User.class, new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        GirafFragment fragment = new GirafFragment();
                        fragment.setCurrentUser(response);
                        replaceFragment(fragment);
                        focusButton(girafAppsButton);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
            }
        });

        /**
         *  inflate the androidFragment when the androidAppsButton is pressed.
         */
        androidAppsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handler = RequestQueueHandler.getInstance(getActivity().getApplicationContext());
                handler.get(User.class, new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        AndroidFragment fragment = new AndroidFragment();
                        fragment.setCurrentUser(response);
                        replaceFragment(fragment);
                        focusButton(androidAppsButton);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
            }
        });

        /**
         * Attempt to open the Play Store. If it is not installed on the device, open Play Store in the browser instead.
         */
        googlePlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Try to start the Google Play app
                Intent intent = new Intent();
                intent.setData(Uri.parse(MARKET_SEARCH_APP_URI + PUBLISHER_NAME));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_SEARCH_WEB_URI + PUBLISHER_NAME)));
                }
            }
        });
    }

    /**
     * Replace active fragment by running the transaction in a new thread.
     * Adds responsiveness when loading list of installed apps_container.
     *
     * @param fragment the fragment.
     */
    private void replaceFragment(final android.support.v4.app.Fragment fragment) {
        new Runnable() {
            @Override
            public void run() {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.app_settings_fragmentlayout, fragment);
                ft.commit();
            }
        }.run();
    }

    /**
     * Makes sure the button clicked appears visually as the button clicked.
     */
    private void focusButton(Button clickedButton) {
        girafAppsButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        androidAppsButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        googlePlayButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);

        clickedButton.setBackgroundResource(android.R.color.holo_orange_light);
    }
}
