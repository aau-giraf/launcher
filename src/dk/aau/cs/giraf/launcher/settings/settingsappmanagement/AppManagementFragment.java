package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Vagner on 01-05-14.
 */
public class AppManagementFragment extends Fragment implements AndroidFragment.InterfaceParseAndroidApps{
    public AndroidFragment.InterfaceParseAndroidApps interfaceParseAndroidApps;

    private Activity activity;
    private FragmentManager mFragManager;
    private Fragment girafFragment;
    private Fragment androidFragment;
    private Fragment googlePlayFragment;
    private TextView girafButton;
    private TextView androidButton;
    private TextView googlePlayButton;
    private Fragment fragmentContainer;
    private List<ResolveInfo> selectedAndroidApps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_appfragment_main,
                container, false);

        girafFragment = new GirafFragment();
        androidFragment = new AndroidFragment();
        googlePlayFragment = new GooglePlayFragment();

        activity = this.getActivity();

        girafButton = (TextView)view.findViewById(R.id.settings_giraf_button);
        androidButton = (TextView)view.findViewById(R.id.settings_android_button);
        googlePlayButton = (TextView)view.findViewById(R.id.settings_googleplay_button);

        mFragManager = this.getFragmentManager();
        fragmentContainer = mFragManager.findFragmentById(R.id.app_settings_fragmentlayout);

        girafButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(girafFragment);
                focusButton(girafButton);
            }
        });

        androidButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(androidFragment);
                focusButton(androidButton);

            }
        });

        googlePlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceFragment(googlePlayFragment);
                focusButton(googlePlayButton);

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


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            interfaceParseAndroidApps = (AndroidFragment.InterfaceParseAndroidApps) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement GetSelectedAndroidApps");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        focusButton(girafButton);
    }

    @Override
    public void onResume() {
        super.onResume();
        replaceFragment(girafFragment);
        focusButton(girafButton);
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction ft = mFragManager.beginTransaction();
        ft.replace(R.id.app_settings_fragmentlayout, fragment);
        ft.commit();
    }

    private void focusButton(TextView clickedView)
    {
        girafButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        androidButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);
        googlePlayButton.setBackgroundResource(R.drawable.settings_tab_button_drawable);

        girafButton.setTypeface(Typeface.DEFAULT);
        androidButton.setTypeface(Typeface.DEFAULT);
        googlePlayButton.setTypeface(Typeface.DEFAULT);

        clickedView.setTypeface(Typeface.DEFAULT_BOLD);
        clickedView.setBackgroundResource(android.R.color.holo_orange_dark);
    }

    @Override
    public void setSelectedAndroidApps(List<ResolveInfo> selectedAndroidApps) {
        interfaceParseAndroidApps.setSelectedAndroidApps(selectedAndroidApps);
    }
}
