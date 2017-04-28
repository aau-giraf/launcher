package dk.aau.cs.giraf.launcher.activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GirafNotifyDialog;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.logiccontroller.MainController;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.models.core.*;

/**
 * Displays the splash logo of Launcher, and then starts
 * {@link dk.aau.cs.giraf.launcher.activities.LoginActivity}.
 * Provides variables for enabling debug mode before compilation.
 */
public class MainActivity extends GirafActivity implements Animation.AnimationListener, GirafNotifyDialog.Notification {

    private MainController controller;
    private long oldSessionGuardianId = -1;
    private Animation startingAnimation;
    private Animation loadAnimation;

    //Dialog for offline mode
    private boolean offlineMode;
    private final int offlineDialogId = 1337;
    private GirafNotifyDialog offlineDialog;
    private final String offlineDialogTag = "DIALOG_TAG";
    private final Handler handler = new Handler();


    /**
     * Sets up the activity. Adds dummy data to the database if it's empty. Starts the splash animation,
     * if this is not disabled through debugging mode.
     *
     * @param savedInstanceState Information from the last launch of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTrackingId("UA-48608499-1");
        setContentView(R.layout.main_activity);
        controller = new MainController(this);

        //Load the preference determining whether the animation should be shown
        oldSessionGuardianId = controller.getOldSessionId();

        boolean showAnimation = true;

        if (oldSessionGuardianId != -1) {
            User oldSessionProfile = new User(new Department("Test"),"Testuser","test123"); //Todo rewrite when rest is finished
            SharedPreferences prefs = SettingsUtility.getLauncherSettings(this,
                LauncherUtility.getSharedPreferenceUser(oldSessionProfile));
            showAnimation = prefs.getBoolean(getString(R.string.show_animation_preference_key), true);
        }

        // Skip loading screen if monkey test
        if (ActivityManager.isUserAMonkey()) {
            controller.startNextActivity();
        }

        //Decide whether to skip animation
        if (!showAnimation) {
            controller.startNextActivity();
        }
        //Load the splash animation
        startingAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_once);
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_infinite);

        startingAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);
        loadAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);

        findViewById(R.id.giraficon).startAnimation(startingAnimation);
        startingAnimation.setAnimationListener(this);

        //Todo plz remember to remove this again
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Overrides the backbutton to do nothing, as the user should not be able to back out of this activity.
     *
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * Must be overridden to implement AnimationListener.
     * We do not need it for anything, though.
     *
     * @param animation The animation the function should act upon.
     */
    @Override
    public void onAnimationStart(Animation animation) {
    }

    /**
     * Changed the welcome text to a "fetching data" text and makes the application start loading data.
     *
     * @param animation The animation the function should act upon.
     */
    @Override
    public void onAnimationEnd(Animation animation) {
        findViewById(R.id.giraficon).startAnimation(loadAnimation);
    }

    /**
     * Must be overridden to implement AnimationListener.
     * We do not need it for anything, though.
     *
     * @param animation The animation the function should act upon.
     */
    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    /**
     * Restart the launcher by killing myself then launching again.
     */
    private void restartLauncher() {
        Intent startActivity = new Intent(getApplicationContext(), MainActivity.class);
        int pendingIntentId = 123456;
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
            pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        System.exit(0);
    }


    /**
     * Method which must be implemented for the CustomButtons dialog to work.
     */
    @Override
    public void noticeDialog(int id) {
        if (id == offlineDialogId) {
            offlineDialog.dismiss();
        }
    }

}
