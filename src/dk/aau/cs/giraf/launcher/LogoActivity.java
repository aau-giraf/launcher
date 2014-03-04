package dk.aau.cs.giraf.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public class LogoActivity extends Activity implements Animation.AnimationListener{

    /** ****************************************** **/
    // TODO: ONLY USED FOR DEBUGGING PURPOSES!!!
    private final boolean DEBUG_MODE = true;
    private final boolean showAuthentication = false;
    private final boolean showLogoAnimation = false;
    /** ****************************************** **/

	private Context mContext;

    /* This function is run after the logo animation is finished.
       It checks if a session is in progress and launches the correct activity based on the answer
       It goes to AuthenticationActivity to login if a session is not in progress
       It goes to HomeActivity, the home screen, if a session is in progress*/
    public void CheckSessionAndGoToActivity(){
        Intent intent;

        if (DEBUG_MODE && !showAuthentication){
            intent = skipAuthentication();
        } else if (LauncherUtility.sessionExpired(mContext)) {
            intent = new Intent(mContext, AuthenticationActivity.class);
        } else {
            intent = new Intent(mContext, HomeActivity.class);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.TIMER_KEY, 0);
            long guardianID = sharedPreferences.getLong(Constants.GUARDIAN_ID, -1);

            intent.putExtra(Constants.GUARDIAN_ID, guardianID);
        }

        if(DEBUG_MODE)
            LauncherUtility.enableDebugging(DEBUG_MODE);

        startActivity(intent);
        finish();
    }

    private Intent skipAuthentication(){
        Helper helper = new Helper(this);
        Profile profile = helper.profilesHelper.authenticateProfile("jkkxlagqyrztlrexhzofekyzrnppajeobqxcmunkqhsbrgpxdtqgygnmbhrgnpphaxsjshlpupgakmirhpyfaivvtpynqarxsghhilhkqvpelpreevykxurtppcggkzfaepihlodgznrmbrzgqucstflhmndibuymmvwauvdlyqnnlxkurinuypmqypspmkqavuhfwsh");

        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.putExtra(Constants.GUARDIAN_ID, profile.getId());
        LauncherUtility.saveLogInData(mContext, profile.getId());
        return intent;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.logo);

        mContext = this.getApplicationContext();

        // Show warning if DEBUG_MODE is true
        if (DEBUG_MODE) {
            LinearLayout debug = (LinearLayout) findViewById(R.id.debug_mode);
            debug.setVisibility(View.VISIBLE);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Animation logoAnimation = AnimationUtils.loadAnimation(mContext, R.animator.rotatelogo);

        // Opt in/out whether to show animation or not
        if (DEBUG_MODE && !showLogoAnimation)
            logoAnimation.setDuration(0);
        else
            logoAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);

        findViewById(R.id.giraficon).startAnimation(logoAnimation);
        logoAnimation.setAnimationListener(this);

        Helper helper = new Helper(mContext);
        int size = helper.profilesHelper.getProfiles().size();
        if (size <= 0) {
            helper.CreateDummyData();
        }
	}

    // Necessary for the AnimationListener interface, We use this to check for when the animation ends.
    @Override
    public void onAnimationEnd(Animation animation) {
        // After completing the animation, check for session and go to the correct activity.
        CheckSessionAndGoToActivity();
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