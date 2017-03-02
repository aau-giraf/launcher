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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.dblib.models.User;
import dk.aau.cs.giraf.launcher.BuildConfig;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.SimulateAnimationDrawable;

import java.util.Date;

/**
 * Handles authentication of the user's QR code through a camera feed. If the the user's QR code
 * is valid, session information is saved in preferences, and {@code HomeActivity} is started.
 */
public class AuthenticationActivity extends CaptureActivity {

    private Intent homeIntent;
    private TextView loginNameView;
    private TextView infoView;
    private Vibrator vibrator;
    private Button guardianButton;

    private TextView scanStatus;
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

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        loginNameView = (TextView) this.findViewById(R.id.loginname);
        infoView = (TextView) this.findViewById(R.id.authentication_step1);
        scanStatus = (TextView) this.findViewById(R.id.scanStatusTextView);

        if (BuildConfig.DEBUG) {
            guardianButton = (Button) this.findViewById(R.id.loginAsGuardianButton);
            guardianButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public synchronized void onClick(final View view) {
                    final Helper h = new Helper(AuthenticationActivity.this);

                    // "mutex" for the guardianbutton, so it returns immediately if one task is already trying to log in.
                    if (!guardianButton.isEnabled()) {
                        return;
                    }

                    // claim the "mutex"
                    guardianButton.setEnabled(false);


                    final Thread backgroundThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            final boolean hasProfiles = !(h.profilesHelper.getListOfObjects() == null ||
                                h.profilesHelper.getListOfObjects().size() == 0);

                            if (hasProfiles) {
                                // If there were profiles, run the profilefetchertask and try to login.
                                /*ProfileFetcherTask profileFetcherTask = new ProfileFetcherTask();
                                profileFetcherTask.execute();*/

                                // get SW615F14 test guardian
                                Profile tempProfile = h.profilesHelper.getById(37L);

                                // If SW615F14 does not exists in current database,
                                // get the first guardian available instead
                                if (tempProfile == null) {
                                    tempProfile = h.profilesHelper.getGuardians().get(0);
                                }

                                final Profile profile = tempProfile;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (profile == null) {
                                            Toast.makeText(AuthenticationActivity.this,
                                                R.string.no_guardian_profiles_available, Toast.LENGTH_SHORT).show();
                                            guardianButton.setEnabled(true);
                                        } else {
                                            guardianButton.setEnabled(true);
                                            login(profile);
                                        }
                                    }
                                });


                            } else {
                                // If the profileshelper was empty, display message and reactivate guardianbutton.

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AuthenticationActivity.this,
                                            getString(R.string.db_no_profiles_msg), Toast.LENGTH_LONG).show();
                                        guardianButton.setEnabled(true);
                                    }
                                });

                            }
                        }
                    });
                    backgroundThread.start();
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
        new SimulateAnimationDrawable(instructImageView, Constants.INSTRUCTION_ANIMATION,
            Constants.INSTRUCTION_FRAME_DURATION);

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
        if (!isFramingRectangleRedrawn) {
            final View cameraFeed = this.findViewById(R.id.camerafeed);
            super.setFramingRect(cameraFeed.getWidth(), cameraFeed.getHeight());
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
        ViewGroup cameraFeedView = (ViewGroup) this.findViewById(R.id.camerafeed);

        RectF rectf = new RectF(10, 10, 10, 10);
        RoundRectShape rect = new RoundRectShape(new float[] {15, 15, 15, 15, 15, 15, 15, 15}, rectf, null);
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
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    @Override
    public void handleDecode(Result rawResult, final Bitmap barcode) {
        try {
            Helper helper = new Helper(this);
            Profile profile;

            // SymDS synchronization uses Users for authentication
            if (BuildConfig.ENABLE_SYMMETRICDS) {
                User user = helper.userHelper.authenticateUser(rawResult.getText());
                if (user != null) {
                    // Profile could be found by user / userID instead
                    profile = helper.profilesHelper.authenticateProfile(rawResult.getText());
                }
            } else {
                profile = helper.profilesHelper.authenticateProfile(rawResult.getText());
            }

            // If the certificate was not valid, profile is set to null.
            if (profile != null) {
                vibrator.vibrate(400);

                login(profile);

            } else {
                loginNameView.setVisibility(View.INVISIBLE);
                infoView.setText(R.string.authentication_step1);

                if (!scanFailed) {
                    changeCameraFeedBorderColor(
                        getResources().getColor(R.color.wrong_qr_camera_border_color)); // Error color (red)
                    scanStatus.setText(getString(R.string.wrong_qr_code_msg));

                    scanFailed = true;

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeCameraFeedBorderColor(getResources().getColor(R.color.default_camera_border_color));
                            scanStatus.setText(getString(R.string.scan_qr_code_msg));
                            scanFailed = false;
                        }
                    }, 2000);
                }
            }
        } catch (Exception e) { //ToDo find which type of exception it is
            Toast.makeText(this, getString(R.string.could_not_verify_msg), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        /*
         * Needed by ZXing in order to continuously scan QR codes, and not halt after first scan.
         */
        this.getHandler().sendEmptyMessageDelayed(R.id.restart_preview, 500);
    }

    private void login(final Profile profile) {

        this.changeCameraFeedBorderColor(getResources()
            .getColor(R.color.success_qr_camera_border_color)); // Success color (green)
        loginNameView.setText(profile.getName());
        loginNameView.setVisibility(View.VISIBLE);

        homeIntent = new Intent(AuthenticationActivity.this, HomeActivity.class);
        homeIntent.putExtra(Constants.GUARDIAN_ID, profile.getId());
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // If authentication_activity was not launched by the launcher...
        if (!getIntent().hasCategory("dk.aau.cs.giraf.launcher.GIRAF")) {
            final Handler h = new Handler();

            scanStatus.setText(getString(R.string.logging_in_msg));

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LauncherUtility.saveLogInData(AuthenticationActivity.this, profile.getId(), new Date().getTime());
                    startActivity(homeIntent);
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

