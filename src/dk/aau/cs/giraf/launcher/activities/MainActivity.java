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

    /** This function is run after the logo animation is finished.
       It checks if a session is in progress and launches the correct activity based on the answer
       It goes to AuthenticationActivity to login if a session is not in progress
       It goes to HomeActivity, the home screen, if a session is in progress*/
    public void checkSessionAndGoToActivity(){
        Intent intent;

        if (DEBUG_MODE && SKIP_AUTHENTICATION){
            intent = skipAuthentication(DEBUG_AS_CHILD);
        } else if (LauncherUtility.sessionExpired(mContext)) {
            intent = new Intent(mContext, AuthenticationActivity.class);
        } else {
            intent = new Intent(mContext, HomeActivity.class);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_TIME_KEY, 0);
            int guardianID = sharedPreferences.getInt(Constants.GUARDIAN_ID_KEY, -1);

            intent.putExtra(Constants.GUARDIAN_ID_KEY, guardianID);
        }

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
        intent.putExtra(Constants.GUARDIAN_ID_KEY, profile.getId());
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
            checkSessionAndGoToActivity();
        else
            logoAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);

        findViewById(R.id.giraficon).startAnimation(logoAnimation);
        logoAnimation.setAnimationListener(this);
	}

    // Necessary for the AnimationListener interface, We use this to check for when the animation ends.
    @Override
    public void onAnimationEnd(Animation animation) {
        // After completing the animation, check for session and go to the correct activity.
        checkSessionAndGoToActivity();
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