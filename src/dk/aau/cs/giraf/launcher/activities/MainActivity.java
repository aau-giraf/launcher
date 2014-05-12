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
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Displays the splash logo of Launcher, and then starts {@link dk.aau.cs.giraf.launcher.activities.AuthenticationActivity}.
 * Provides variables for enabling debug mode before compilation.
 */
public class MainActivity extends Activity implements Animation.AnimationListener{

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

	private Context mContext;

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

    private Intent skipAuthentication(boolean asChild) {
        Helper helper = LauncherUtility.getOasisHelper(mContext);
        Profile profile = helper.profilesHelper.authenticateProfile("jkkxlagqyrztlrexhzofekyzrnppajeobqxcmunkqhsbrgpxdtqgygnmbhrgnpphaxsjshlpupgakmirhpyfaivvtpynqarxsghhilhkqvpelpreevykxurtppcggkzfaepihlodgznrmbrzgqucstflhmndibuymmvwauvdlyqnnlxkurinuypmqypspmkqavuhfwsh");

        if(asChild)
            profile = helper.profilesHelper.authenticateProfile("childqkxlnftvxquwrwcdloaumdhzkgyglezzsebpvnethrlstvmlorrolymdynjcyonkrtvcuagwigdqqkftsxxhklcnbhznthcqjxnjzzdoqvmfdlxrudcyakvrnfcbohdumawlwmfndjascmvrsoxfjgwzhdvcvqcroxoyjeazmxtrjtlkldoevgdrqvgfbklhtgm");

        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.putExtra(Constants.GUARDIAN_ID, profile.getId());
        LauncherUtility.saveLogInData(mContext, profile.getId(), new Date().getTime());
        return intent;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.logo);

        mContext = this.getApplicationContext();

        Helper helper = LauncherUtility.getOasisHelper(mContext);
        int size = helper.profilesHelper.getProfiles().size();
        if (size <= 0) {
            helper.CreateDummyData();
        }

        Animation logoAnimation = AnimationUtils.loadAnimation(mContext, R.animator.rotatelogo);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean skipAuthenticationPref = prefs.getBoolean("show_animation_preference", true);

        // Opt in/out whether to show animation or not
        if ((DEBUG_MODE && SKIP_SPLASH_SCREEN) || !skipAuthenticationPref)
            startNextActivity();
        else
            logoAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);

        findViewById(R.id.giraficon).startAnimation(logoAnimation);
        logoAnimation.setAnimationListener(this);
	}

    // Necessary for the AnimationListener interface, We use this to check for when the animation ends.
    @Override
    public void onAnimationEnd(Animation animation) {
        // After completing the animation, check for session and go to the correct activity.
        startNextActivity();
    }

    // Necessary for the AnimationListener interface
    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub

    }

    // Necessary for the AnimationListener interface
    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }
}