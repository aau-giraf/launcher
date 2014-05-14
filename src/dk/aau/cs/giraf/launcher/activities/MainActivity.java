package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.Date;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.ProfileController;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Displays the splash logo of Launcher, and then starts {@link dk.aau.cs.giraf.launcher.activities.AuthenticationActivity}.
 * Provides variables for enabling debug mode before compilation.
 */
public class MainActivity extends Activity implements Animation.AnimationListener{
    private Context mContext;
    private int oldSessionGuardianID = -1;

    /* ************* DEBUGGING MODE ************* */
    // TODO: ONLY USED FOR DEBUGGING PURPOSES!!!
    /**
     * If {@code true}, the Launcher is stated in debugging mode, where the splash screen and
     * authentication is skipped.
     */
    private final boolean DEBUG_MODE = false;

    /**
     * If {@code true}, the authentication screen is shown, despite debugging mode. Has no
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
     * @param savedInstanceState Information from the last launch of the activity.
     *
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_MODE
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#SKIP_SPLASH_SCREEN
     */
    @Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        boolean skipAnimation = false;

	    setContentView(R.layout.logo);

        mContext = this.getApplicationContext();

        //Load the splash animation
        Animation logoAnimation = AnimationUtils.loadAnimation(mContext, R.animator.rotatelogo);

        //Load the preference determining whether the animation should be shown
        findOldSession();
        if (oldSessionGuardianID != -1) {
            Profile oldSessionProfile = new ProfileController(mContext).getProfileById(oldSessionGuardianID);
            SharedPreferences prefs = SettingsUtility.getLauncherSettings(mContext, LauncherUtility.getSharedPreferenceUser(oldSessionProfile));
            skipAnimation = prefs.getBoolean(getString(R.string.show_animation_preference_key), false);
        }


        //Decide whether to skip animation, according to debug mode
        if ((DEBUG_MODE && SKIP_SPLASH_SCREEN) || skipAnimation)
        {
            startNextActivity();
        }
        else {
            logoAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);
        }

        Helper helper = LauncherUtility.getOasisHelper(mContext);
        int size = helper.profilesHelper.getProfiles().size();
        if (size <= 0) {
            helper.CreateDummyData();
        }

        findViewById(R.id.giraficon).startAnimation(logoAnimation);
        logoAnimation.setAnimationListener(this);
	}

    /**
     * Called by the system. Starts the next activity (through {@link MainActivity#startNextActivity()}
     * when the animation has ended.
     *
     * @param animation Instance of the ended animation. Not used.
     */
    @Override
    public void onAnimationEnd(Animation animation) {
        // After completing the animation, check for session and go to the correct activity.
        startNextActivity();
    }

    // Necessary for the AnimationListener interface
    @Override
    public void onAnimationRepeat(Animation animation) {}

    // Necessary for the AnimationListener interface
    @Override
    public void onAnimationStart(Animation animation) {}

    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }

    /**
     * Launches the next relevant activity, according to the current debugging mode, and to
     * whether any valid login data is detected in {@code SharedPerferences}.
     *
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_MODE
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#SKIP_AUTHENTICATION
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_AS_CHILD
     * @see dk.aau.cs.giraf.launcher.helper.LauncherUtility#sessionExpired(android.content.Context) */
    public void startNextActivity(){
        Intent intent;

        if (DEBUG_MODE && SKIP_AUTHENTICATION){
            intent = skipAuthentication(DEBUG_AS_CHILD);
        }
        //If no valid session is found, start authentication
        else if (LauncherUtility.sessionExpired(mContext)) {
            intent = new Intent(mContext, AuthenticationActivity.class);
        }
        //If a valid session is found, pass the profile ID along with the intent.
        else {
            intent = new Intent(mContext, HomeActivity.class);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            int guardianID = sharedPreferences.getInt(Constants.GUARDIAN_ID, -1);

            intent.putExtra(Constants.GUARDIAN_ID, guardianID);
        }

        //If in debugging mode, set global variables.
        if(DEBUG_MODE)
            LauncherUtility.setDebugging(DEBUG_MODE, DEBUG_AS_CHILD, this);

        startActivity(intent);
        finish();
    }

    /**
     * Used for debugging mode.
     * Overrides the authentication activity by authenticating a test profile, and creating an intent
     * for starting {@code HomeActivity}. The guardian profile used is 'Tony Stark', and the child profile used
     * is 'Johnathan Doerwald'.
     * @param asChild If {@code true}, a child profile is used for authentication. If {@code false}, a guardian
     *                profile is used for authentication.
     * @return An intent for starting {@code MainActivity} with the authenticated profile ID as an extra.
     */
    private Intent skipAuthentication(boolean asChild) {
        Helper helper = LauncherUtility.getOasisHelper(mContext);
        Profile profile;

        //Get the relevant profile info.
        if(asChild) {
            profile = helper.profilesHelper.authenticateProfile("childqkxlnftvxquwrwcdloaumdhzkgyglezzsebpvnethrlstvmlorrolymdynjcyonkrtvcuagwigdqqkftsxxhklcnbhznthcqjxnjzzdoqvmfdlxrudcyakvrnfcbohdumawlwmfndjascmvrsoxfjgwzhdvcvqcroxoyjeazmxtrjtlkldoevgdrqvgfbklhtgm");
        }
        else {
            profile = helper.profilesHelper.authenticateProfile("jkkxlagqyrztlrexhzofekyzrnppajeobqxcmunkqhsbrgpxdtqgygnmbhrgnpphaxsjshlpupgakmirhpyfaivvtpynqarxsghhilhkqvpelpreevykxurtppcggkzfaepihlodgznrmbrzgqucstflhmndibuymmvwauvdlyqnnlxkurinuypmqypspmkqavuhfwsh");
        }

        Intent intent = new Intent(mContext, HomeActivity.class);

        //Add the profile ID to the intent, and save information on the session.
        intent.putExtra(Constants.GUARDIAN_ID, profile.getId());
        LauncherUtility.saveLogInData(mContext, profile.getId(), new Date().getTime());
        return intent;
    }

    private void findOldSession() {
        if (LauncherUtility.sessionExpired(mContext)) {
            oldSessionGuardianID = -1;
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            oldSessionGuardianID = sharedPreferences.getInt(Constants.GUARDIAN_ID, -1);
        }
    }
}