package dk.aau.cs.giraf.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.MotionEvent;
import dk.aau.cs.giraf.oasis.lib.Helper;

public class LogoActivity extends Activity implements Animation.AnimationListener{

	private Context mContext;

    /* This function is run after the logo animation is finished.
       It checks if a session is in progress and launches the correct activity based on the answer
       It goes to AuthenticationActivity to login if a session is not in progress
       It goes to HomeActivity, the home screen, if a session is in progress*/
    public void CheckSessionAndGoToActivity(){
        Intent intent;

        if (LauncherUtility.sessionExpired(mContext)) {
            intent = new Intent(mContext, AuthenticationActivity.class);
        } else {
            intent = new Intent(mContext, HomeActivity.class);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.TIMER_KEY, 0);
            long guardianID = sharedPreferences.getLong(Constants.GUARDIAN_ID, -1);

	            		/* Following did we not have time to test due to errors in the Oasislib */

            //if ((new Helper(mContext)).profilesHelper.getProfileById(guardianID) != null) {
            intent.putExtra(Constants.GUARDIAN_ID, guardianID);
            //} else {
            //intent = new Intent(mContext, AuthenticationActivity.class);
            //}
        }

        startActivity(intent);
//	                stop();
        finish();
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.logo);
	    
	    mContext = this.getApplicationContext();
	    
	    Helper helper = new Helper(mContext);
	    int size = helper.profilesHelper.getProfiles().size();
	    if (size <= 0) {
	    	helper.CreateDummyData();
	    }

        Animation logoAnimation = AnimationUtils.loadAnimation(mContext, R.animator.rotatelogo);
        logoAnimation.setDuration(Constants.SPEED_OF_LOGO_ANIMATION);
        findViewById(R.id.giraf_logo).startAnimation(logoAnimation);
        logoAnimation.setAnimationListener(this);
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