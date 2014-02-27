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

public class LogoActivity extends Activity{

	private Thread mLogoThread;
	private Context mContext;

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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Animation rotate1 = AnimationUtils.loadAnimation(mContext, R.animator.rotatelogo);
                rotate1.setDuration(Constants.SPEED_OF_LOGO_ANIMATION);
                findViewById(R.id.giraf_logo).startAnimation(rotate1);
            }
        });

	    // Thread used to display the logo for a set amount of time.
	    mLogoThread = new Thread() {
	        @Override
	        public void run() {
	            try {
	            	synchronized(this) {
	            		wait(Constants.TIME_TO_DISPLAY_LOGO);
	            	}
	            } catch(InterruptedException e) {}
	            finally {
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
	        }
	    };
	    
	    mLogoThread.start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
	    	synchronized(mLogoThread){
	    		mLogoThread.notifyAll();
	    	}
	    }
	    return true;
	}

    @Override
    public void onBackPressed() {
        //Do nothing, as the user should be able to back out of this activity
    }
}