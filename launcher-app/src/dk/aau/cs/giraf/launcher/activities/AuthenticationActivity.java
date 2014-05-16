package dk.aau.cs.giraf.launcher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;

import java.util.Date;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.SimulateAnimationDrawable;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Handles authentication of the user's QR code through a camera feed. If the the user's QR code
 * is valid, session information is saved in preferences, and {@code HomeActivity} is started.
 */
public class AuthenticationActivity extends CaptureActivity {
	
	private Intent mHomeIntent;
	private GButton mGLoginButton;
	private TextView mLoginNameView;
	private TextView mInfoView;
	private Context mContext;
	private Vibrator mVibrator;
	private Profile mPreviousProfile;
    private View mCameraFeed;

    private boolean isFramingRectangleRedrawn = false;

    /**
     * Sets up the activity. Specifically view variables are instantiated, the login button listener
     * is set, and the instruction animation is set up.
     *
     * @param savedInstanceState Information from the last launch of the activity.
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authentication);

		mContext = this;
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);		
		mGLoginButton = (GButton)this.findViewById(R.id.loginGButton);
		mLoginNameView = (TextView)this.findViewById(R.id.loginname);
		mInfoView = (TextView)this.findViewById(R.id.authentication_step1);

        // Show warning if in debugging mode
        if (LauncherUtility.isDebugging()) {
            LauncherUtility.showDebugInformation(this);
        }

		mGLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// If the authentication activity was not launched by the launcher...
				if (!getIntent().hasCategory("dk.aau.cs.giraf.launcher.GIRAF")) {
                    LauncherUtility.saveLogInData(mContext, mPreviousProfile.getId(), new Date().getTime());
					startActivity(mHomeIntent);
				}
			}
		});

        // Simulate the AnimationDrawable class
        final ImageView instructImageView = (ImageView) findViewById(R.id.animation);
        new SimulateAnimationDrawable(instructImageView, Constants.INSTRUCTION_ANIMATION, Constants.INSTRUCTION_FRAME_DURATION);

        // Start logging this activity
        EasyTracker.getInstance(this).activityStart(this);
	}

    /**
     * Draws the feed's framing rectangle if necessary.
     *
     * @param hasFocus {@code true} if the activity has focus.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!isFramingRectangleRedrawn){
            mCameraFeed = this.findViewById(R.id.camerafeed);
            super.setFramingRect(mCameraFeed.getWidth(), mCameraFeed.getHeight());
            isFramingRectangleRedrawn = true;
        }
    }

    /**
     * Stops Google Analytics logging.
     */
    @Override
    public void finish() {
        super.finish();

        // Start logging this activity
        EasyTracker.getInstance(this).activityStop(this);
    }

    /**
	 * Changes the color of the border around the camera feed of the QR code scanner.
     *
	 * @param color The color which the border should have
	 */
	private void changeCameraFeedBorderColor(int color) {
		ViewGroup cameraFeedView = (ViewGroup)this.findViewById(R.id.camerafeed);
		
		RectF rectf = new RectF(10,10,10,10);
		RoundRectShape rect = new RoundRectShape( new float[] {15,15, 15,15, 15,15, 15,15}, rectf, null);
		ShapeDrawable shapeDrawable = new ShapeDrawable(rect);
		
		shapeDrawable.getPaint().setColor(color);
		cameraFeedView.setBackgroundDrawable(shapeDrawable);
	}

	/**
	 * Checks whether the string from a scanned QR code is a valid GIRAF certificate.
     * If the certificate is valid, the certificate-holder's name is shown, and the login button
     * is displayed.
     *
	 * @param rawResult Result which the scanned string is saved in.
	 * @param barcode A greyscale bitmap of the camera data which was decoded.
	 */
	@Override
	public void handleDecode(Result rawResult, Bitmap barcode){
        try {
            Helper helper = new Helper(this);
            Profile profile = helper.profilesHelper.authenticateProfile(rawResult.getText());

            // If the certificate was not valid, profile is set to null.
            if (profile != null) {
                if (mPreviousProfile == null || !profile.toString().equals(mPreviousProfile.toString())) {
                    mVibrator.vibrate(400);
                }
                mPreviousProfile = profile;

                this.changeCameraFeedBorderColor(0xFF3AAA35);
                mLoginNameView.setText(profile.getName());
                mLoginNameView.setVisibility(View.VISIBLE);
                mGLoginButton.setVisibility(View.VISIBLE);
                mInfoView.setText(R.string.saadan);

                mHomeIntent = new Intent(AuthenticationActivity.this, HomeActivity.class);
                mHomeIntent.putExtra(Constants.GUARDIAN_ID, profile.getId());
            } else {
                this.changeCameraFeedBorderColor(0xFFFF0000);
                mGLoginButton.setVisibility(View.INVISIBLE);
                mLoginNameView.setVisibility(View.INVISIBLE);
                mInfoView.setText(R.string.authentication_step1);
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.could_not_verify_msg), Toast.LENGTH_LONG).show();
        }

		/*
		 * Needed by ZXing in order to continuously scan QR codes, and not halt after first scan.
		 */
		this.getHandler().sendEmptyMessageDelayed(R.id.restart_preview, 500);
	}

    /**
     * Does nothing, to prevent the user from returning to the splash screen or native OS.
     */
    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }
}
