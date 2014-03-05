package dk.aau.cs.giraf.launcher;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * This activity controls the camera based authentication,
 * used when logging in to launcher. Specifically is sets up
 * QR code scanning, and authenticates valid QR codes.
 */
public class AuthenticationActivity extends CaptureActivity {
	
	private Intent mHomeIntent;
	private GButton mGLoginButton;
	private TextView mLoginNameView;
	private TextView mInfoView;
	private Context mContext;
	private Vibrator mVibrator;
	private Profile mPreviousProfile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authentication1);

		mContext = this;
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);		
		mGLoginButton = (GButton)this.findViewById(R.id.loginGButton);
		mLoginNameView = (TextView)this.findViewById(R.id.loginname);
		mInfoView = (TextView)this.findViewById(R.id.authentication_step1);

        // Show warning if DEBUG_MODE is true
        LauncherUtility.ShowDebugInformation(this);

		mGLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// If the authentication activity was not launched by the launcher...
				if (!getIntent().hasCategory("dk.aau.cs.giraf.launcher.GIRAF")) {
                    //TODO:The method call below caused an exception. Figure out why the call is needed.
					//LauncherUtility.attachLauncher(mContext); // should not be called
					LauncherUtility.saveLogInData(mContext, mPreviousProfile.getId());
					startActivity(mHomeIntent);
				} else {
					finish();
				}
			}
		});

        // Simulate the AnimationDrawable class
        final ImageView instructImageView = (ImageView) findViewById(R.id.animation);
        new SimulateAnimationDrawable(instructImageView, Constants.INSTRUCTION_ANIMATION, Constants.INSTRUCTION_FRAME_DURATION);
	}

	/**
	 * Changes the color of the border around the camera feed of the QR code scanner.
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
	 * @param rawResult Result which the scanned string is saved in.
	 * @param barcode A greyscale bitmap of the camera data which was decoded.
	 */
	@Override
	public void handleDecode(Result rawResult, Bitmap barcode)
	{
		Helper helper = new Helper(this);
		Profile profile = helper.profilesHelper.authenticateProfile(rawResult.getText());

		// If the certificate was not valid, profile is set to null.
		if (profile != null) {	
			if (mPreviousProfile == null || !profile.toString().equals(mPreviousProfile.toString())) {
				mVibrator.vibrate(400);
			}
			mPreviousProfile = profile;
			
			this.changeCameraFeedBorderColor(0xFF3AAA35);
			mLoginNameView.setText(profile.getFirstname() + " " + profile.getSurname());
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
		
		/*
		 * Needed by ZXing in order to continuously scan QR codes, and not halt after first scan.
		 */
		this.getHandler().sendEmptyMessageDelayed(R.id.restart_preview, 500);
	}

    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }
}
