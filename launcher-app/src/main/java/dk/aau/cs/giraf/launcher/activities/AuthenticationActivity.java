package dk.aau.cs.giraf.launcher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;

import java.util.ArrayList;
import java.util.Date;

import dk.aau.cs.giraf.launcher.BuildConfig;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.SimulateAnimationDrawable;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.AdminOfController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.controllers.UserController;
import dk.aau.cs.giraf.oasis.lib.models.AdminOf;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.User;

/**
 * Handles authentication of the user's QR code through a camera feed. If the the user's QR code
 * is valid, session information is saved in preferences, and {@code HomeActivity} is started.
 */
public class AuthenticationActivity extends CaptureActivity {

	private Intent mHomeIntent;
	private TextView mLoginNameView;
	private TextView mInfoView;
	private Context mContext;
	private Vibrator mVibrator;
	private Profile mPreviousProfile;
    private View mCameraFeed;
    private TextView mScanStatus;
    private ListView listUsers;

    private boolean isFramingRectangleRedrawn = false;
    private boolean scanFailed = false;

    /**
     * Sets up the activity. Specifically view variables are instantiated, the login button listener
     * is set, and the instruction animation is set up.
     *
     * @param savedInstanceState Information from the last launch of the activity.
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authentication_activity);

		mContext = this;
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mLoginNameView = (TextView)this.findViewById(R.id.loginname);
		mInfoView = (TextView)this.findViewById(R.id.authentication_step1);
        mScanStatus = (TextView)this.findViewById(R.id.scanStatusTextView);
        listUsers = (ListView)this.findViewById(R.id.users_list);

        ArrayList<String> arr = new ArrayList<String>();
        for (User u : new UserController(this).getUsers())
            arr.add(u.getUsername()+ " - " + u.getId());

        final ArrayAdapter<String> usersAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arr);
        listUsers.setAdapter(usersAdapter);


        if(BuildConfig.DEBUG) {
            Button guardianButton = (Button)this.findViewById(R.id.loginAsGuardianButton);
            guardianButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Helper h = new Helper(mContext);
                    if (h.profilesHelper.getProfiles().size() == 0)
                    {
                        Toast.makeText(mContext, "Databasen indeholder ingen profiler", Toast.LENGTH_LONG).show();
                        return;
                    }
                    /*Profile profile = h.profilesHelper.getProfileById(37); // Hardcoded value. It's the ID for group sw615f14.
                    login(profile);*/
                    PictogramController ctrl = new PictogramController(mContext);
                    Pictogram p = ctrl.getPictogramById(13969);
                    ctrl.removePictogram(p);
                }
            });
            guardianButton.setVisibility(View.VISIBLE);
        }

        // Show warning if in debugging mode
        if (LauncherUtility.isDebugging()) {
            LauncherUtility.showDebugInformation(this);
        }

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
	public void changeCameraFeedBorderColor(int color) {
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
	public void handleDecode(Result rawResult, final Bitmap barcode){
        try {
            Helper helper = new Helper(this);
            Profile profile = helper.profilesHelper.authenticateProfile(rawResult.getText());

            // If the certificate was not valid, profile is set to null.
            if (profile != null) {
                if (mPreviousProfile == null || !profile.toString().equals(mPreviousProfile.toString())) {
                    mVibrator.vibrate(400);
                }

                login(profile);

            } else {
                mLoginNameView.setVisibility(View.INVISIBLE);
                mInfoView.setText(R.string.authentication_step1);

                if (!scanFailed) {
                    changeCameraFeedBorderColor(0xFFFF0000); // Error color (red)
                    mScanStatus.setText(getString(R.string.wrong_qr_code_msg));
                    scanFailed = true;

                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeCameraFeedBorderColor(0xFFDD9639); // Default color (orange)
                            mScanStatus.setText(getString(R.string.scan_qr_code_msg));
                            scanFailed = false;
                        }
                    }, 2000);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.could_not_verify_msg), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

		/*
		 * Needed by ZXing in order to continuously scan QR codes, and not halt after first scan.
		 */
		this.getHandler().sendEmptyMessageDelayed(R.id.restart_preview, 500);
	}

    private void login(Profile profile) {
        mPreviousProfile = profile;

        this.changeCameraFeedBorderColor(0xFF3AAA35); // Success color (green)
        mLoginNameView.setText(profile.getName());
        mLoginNameView.setVisibility(View.VISIBLE);

        mHomeIntent = new Intent(AuthenticationActivity.this, HomeActivity.class);
        mHomeIntent.putExtra(Constants.GUARDIAN_ID, profile.getId());

        // If authentication_activity was not launched by the launcher...
        if (!getIntent().hasCategory("dk.aau.cs.giraf.launcher.GIRAF")) {
            Handler h = new Handler();

            mScanStatus.setText(getString(R.string.logging_in_msg));

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LauncherUtility.saveLogInData(mContext, mPreviousProfile.getId(), new Date().getTime());
                    startActivity(mHomeIntent);
                }
            }, 800);
        }
    }

    /**
     * Does nothing, to prevent the user from returning to the splash screen or native OS.
     */
    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }
}
