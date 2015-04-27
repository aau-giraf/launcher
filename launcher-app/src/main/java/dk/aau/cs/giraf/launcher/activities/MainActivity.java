package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.os.Handler;

import java.util.Date;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.localdb.main;

/**
 * Displays the splash logo of Launcher, and then starts {@link dk.aau.cs.giraf.launcher.activities.AuthenticationActivity}.
 * Provides variables for enabling debug mode before compilation.
 */
public class MainActivity extends Activity implements Animation.AnimationListener {
    //private Context mContext;
    private int oldSessionGuardianID = -1;
    Animation startingAnimation;
    Animation loadAnimation;
    Handler messageHandler;

    /* ************* DEBUGGING MODE ************* */
    // TODO: ONLY USED FOR DEBUGGING PURPOSES!!!
    /**
     * If {@code true}, the Launcher is stated in debugging mode, where the splash screen and
     * authentication_activity is skipped.
     */
    private final boolean DEBUG_MODE = false;

    /**
     * If {@code true}, the authentication_activity screen is shown, despite debugging mode. Has no
     * effect if {@code DEBUG_MODE} is {@code false}.
     */
    private final boolean SKIP_AUTHENTICATION = false;

    /**
     * If {@code true}, the splash screen is shown, despite debugging mode. Has no
     * effect if {@code DEBUG_MODE} is {@code false}.
     */
    private final boolean SKIP_SPLASH_SCREEN = false;

    /**
     * If {@code true}, Launcher automatically logs in with a child profile. If {@code false},
     * Launcher logs in with a guardian profile. Has no effect if {@code DEBUG_MODE} is {@code false},
     * or if {@code SKIP_AUTHENTICATION} is {@code true}.
     */
    private final boolean DEBUG_AS_CHILD = false;
    /* ****************************************** */

    /**
     * Sets up the activity. Adds dummy data to the database if it's empty. Starts the splash animation,
     * if this is not disabled through debugging mode.
     *
     * @param savedInstanceState Information from the last launch of the activity.
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_MODE
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#SKIP_SPLASH_SCREEN
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        messageHandler = new MessageHandler();

        //Load the preference determining whether the animation should be shown
        findOldSession();

        boolean showAnimation = true;

        if (oldSessionGuardianID != -1) {
            Profile oldSessionProfile = new ProfileController(this).getProfileById(oldSessionGuardianID);
            SharedPreferences prefs = SettingsUtility.getLauncherSettings(this, LauncherUtility.getSharedPreferenceUser(oldSessionProfile));
            showAnimation = prefs.getBoolean(getString(R.string.show_animation_preference_key), true);
        }

        // Skip loading screen if monkey test
        if (ActivityManager.isUserAMonkey()) {
            startNextActivity();
        }

        //Decide whether to skip animation, according to debug mode
        if ((DEBUG_MODE && SKIP_SPLASH_SCREEN) || !showAnimation) {
            startNextActivity();
        }
        //Load the splash animation
        startingAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_once);
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_infinite);

        startingAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);
        loadAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);


        findViewById(R.id.giraficon).startAnimation(startingAnimation);
        startingAnimation.setAnimationListener(this);

        // Start the remote syncing service
        new main(this).startSynch(messageHandler);
    }

    /**
     * Overrides the backbutton to do nothing, as the user should not be able to back out of this activity
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
        TextView welcomeText = (TextView) findViewById(R.id.welcome_text);
        welcomeText.setText("Henter data...");
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
     * Launches the next relevant activity, according to the current debugging mode, and to
     * whether any valid login data is detected in {@code SharedPerferences}.
     *
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_MODE
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#SKIP_AUTHENTICATION
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_AS_CHILD
     * @see dk.aau.cs.giraf.launcher.helper.LauncherUtility#sessionExpired(android.content.Context)
     */
    public void startNextActivity() {
        Intent intent;
        TextView welcomeText = (TextView) findViewById(R.id.welcome_text);
        welcomeText.setText("Klar!");

        if (DEBUG_MODE && SKIP_AUTHENTICATION) {
            intent = skipAuthentication(DEBUG_AS_CHILD);
        }
        //If no valid session is found, start authentication_activity
        else if (LauncherUtility.sessionExpired(this)) {
            intent = new Intent(this, AuthenticationActivity.class);
        }
        //If a valid session is found, pass the profile ID along with the intent.
        else {
            intent = new Intent(this, HomeActivity.class);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            final int guardianID = sharedPreferences.getInt(getString(R.string.current_guardian_id), -1);
            final int childID = sharedPreferences.getInt(getString(R.string.current_child_id), -1);

            intent.putExtra(getString(R.string.current_guardian_id), guardianID);
            intent.putExtra(getString(R.string.current_child_id), childID);
        }

        //If in debugging mode, set global variables.
        if (DEBUG_MODE) {
            LauncherUtility.setDebugging(DEBUG_MODE, DEBUG_AS_CHILD, this);
        }

        startActivity(intent);
        finish();
    }

    /**
     * Used for debugging mode.
     * Overrides the authentication by authenticating a test profile, and creating an intent
     * for starting {@code HomeActivity}. The guardian profile used is 'Tony Stark', and the child profile used
     * is 'Johnathan Doerwald'.
     *
     * @param asChild If {@code true}, a child profile is used for authentication. If {@code false}, a guardian
     *                profile is used for authentication.
     * @return An intent for starting {@code MainActivity} with the authenticated profile ID as an extra.
     */
    private Intent skipAuthentication(boolean asChild) {
        Helper helper = LauncherUtility.getOasisHelper(this);
        Profile profile;

        //Get the relevant profile info.
        if (asChild) {
            profile = helper.profilesHelper.authenticateProfile("childqkxlnftvxquwrwcdloaumdhzkgyglezzsebpvnethrlstvmlorrolymdynjcyonkrtvcuagwigdqqkftsxxhklcnbhznthcqjxnjzzdoqvmfdlxrudcyakvrnfcbohdumawlwmfndjascmvrsoxfjgwzhdvcvqcroxoyjeazmxtrjtlkldoevgdrqvgfbklhtgm");
        } else {
            //profile = helper.profilesHelper.authenticateProfile("jkkxlagqyrztlrexhzofekyzrnppajeobqxcmunkqhsbrgpxdtqgygnmbhrgnpphaxsjshlpupgakmirhpyfaivvtpynqarxsghhilhkqvpelpreevykxurtppcggkzfaepihlodgznrmbrzgqucstflhmndibuymmvwauvdlyqnnlxkurinuypmqypspmkqavuhfwsh");
            profile = helper.profilesHelper.authenticateProfile("d74ecba82569eafc763256e45a126b4ce882f8a81327f28a380faa13eb2ec8f3");
        }

        Intent intent = new Intent(this, HomeActivity.class);

        //Add the profile ID to the intent, and save information on the session.
        intent.putExtra(getString(R.string.current_guardian_id), profile.getId());

        LauncherUtility.saveLogInData(this, profile.getId(), new Date().getTime());
        return intent;
    }

    /**
     * Looks for a previously loaded session.
     * Loads the sharedpreferences if one exists
     */
    private void findOldSession() {
        if (LauncherUtility.sessionExpired(this)) {
            oldSessionGuardianID = -1;
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            oldSessionGuardianID = sharedPreferences.getInt(getString(R.string.current_guardian_id), -1);
        }
    }

    /**
     * Used to communicate with DownloadService
     * To determine if a download is complete
     */
    public class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            int progress = message.arg1;
            if (progress == 100) {
                startNextActivity();
            } else {
                Log.d("Progress", Integer.toString(progress));
            }
        }
    }
}