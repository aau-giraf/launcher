package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
//import com.google.zxing.Result;
//import com.google.zxing.client.android.CaptureActivity;

import java.util.Date;

import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.dblib.models.User;
import dk.aau.cs.giraf.launcher.BuildConfig;
import dk.aau.cs.giraf.launcher.ProfileAdapter;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.ImageMasker;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.SimulateAnimationDrawable;
import jp.wasabeef.picasso.transformations.CropTransformation;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Handles authentication of the user's QR code through a camera feed. If the the user's QR code
 * is valid, session information is saved in preferences, and {@code HomeActivity} is started.
 */
public class AuthenticationActivity extends Activity {

    private Intent mHomeIntent;
    private TextView mLoginNameView;
    private TextView mInfoView;
    private Vibrator mVibrator;
    private Button guardianButton;

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

        Intent intent = getIntent();
        dk.aau.cs.giraf.librest.User curUser = new Gson().fromJson(intent.getExtras().
                getString("userObject"), dk.aau.cs.giraf.librest.User.class);

        final ImageView mImageView = (ImageView)findViewById(R.id.giraflogo);
        ImageMasker masker = new ImageMasker();
        Bitmap maskedImage = masker.maskProfileImage(curUser.getImage(), this);

        mImageView.setImageBitmap(maskedImage);
        mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //mImageView.setBackgroundResource(R.drawable.giraf_loading);
        mImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                Picasso.with(getApplicationContext()).load(R.drawable.stock_profile)
                        .resize(mImageView.getWidth(), mImageView.getHeight())
                        .centerCrop()
                        .transform(new MaskTransformation(getApplicationContext(), R.drawable.giraf_loading))
                        .into(mImageView);
            return true;
            }
        });




        EditText passbox = (EditText)findViewById(R.id.pass_box);
        passbox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    switch(keyCode){
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            LoginBtnPressed(v);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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

                            final boolean hasProfiles = !(h.profilesHelper.getListOfObjects() == null || h.profilesHelper.getListOfObjects().size() == 0);

                            if (hasProfiles) {
                                // If there were profiles, run the profilefetchertask and try to login.
                                /*ProfileFetcherTask profileFetcherTask = new ProfileFetcherTask();
                                profileFetcherTask.execute();*/

                                // get SW615F14 test guardian
                                Profile tempProfile = h.profilesHelper.getById(37L);

                                // If SW615F14 does not exists in current database, get the first guardian available instead
                                if (tempProfile == null) {
                                    tempProfile = h.profilesHelper.getGuardians().get(0);
                                }

                                final Profile profile = tempProfile;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (profile == null) {
                                            Toast.makeText(AuthenticationActivity.this, R.string.no_guardian_profiles_available, Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(AuthenticationActivity.this, getString(R.string.db_no_profiles_msg), Toast.LENGTH_LONG).show();
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

        RectF rectf = new RectF(10, 10, 10, 10);
        RoundRectShape rect = new RoundRectShape(new float[]{15, 15, 15, 15, 15, 15, 15, 15}, rectf, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(rect);

        shapeDrawable.getPaint().setColor(color);
    }

    /**
     * Checks whether the string from a scanned QR code is a valid GIRAF certificate.
     * If the certificate is valid, the certificate-holder's name is shown, and the login button
     * is displayed.
     *
     * @param rawResult Result which the scanned string is saved in.
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    /*public void handleDecode(Result rawResult, final Bitmap barcode) {
        try {
            Helper helper = new Helper(this);
            Profile profile = null;

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
                mVibrator.vibrate(400);

                login(profile);

            } else {
                mLoginNameView.setVisibility(View.INVISIBLE);
                mInfoView.setText(R.string.authentication_step1);

                if (!scanFailed) {
                    changeCameraFeedBorderColor(getResources().getColor(R.color.wrong_qr_camera_border_color)); // Error color (red)
                    mScanStatus.setText(getString(R.string.wrong_qr_code_msg));

                    scanFailed = true;

                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeCameraFeedBorderColor(getResources().getColor(R.color.default_camera_border_color));
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
    }
*/
    private void login(final Profile profile) {

        this.changeCameraFeedBorderColor(getResources().getColor(R.color.success_qr_camera_border_color)); // Success color (green)
        mLoginNameView.setText(profile.getName());
        mLoginNameView.setVisibility(View.VISIBLE);

        mHomeIntent = new Intent(AuthenticationActivity.this, HomeActivity.class);
        mHomeIntent.putExtra(Constants.GUARDIAN_ID, profile.getId());
        mHomeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // If authentication_activity was not launched by the launcher...
        if (!getIntent().hasCategory("dk.aau.cs.giraf.launcher.GIRAF")) {
            final Handler h = new Handler();

            mScanStatus.setText(getString(R.string.logging_in_msg));

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LauncherUtility.saveLogInData(AuthenticationActivity.this, profile.getId(), new Date().getTime());
                    startActivity(mHomeIntent);
                }
            }, 800);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent  = new Intent(AuthenticationActivity.this, ProfileChooserActivity.class);
        startActivity(intent);
    }

    public void LoginBtnPressed(View view){
        EditText text = (EditText)findViewById(R.id.pass_box);
        Toast.makeText(this, "You have entered: " + text.getText(), Toast.LENGTH_SHORT).show();
    }
}

