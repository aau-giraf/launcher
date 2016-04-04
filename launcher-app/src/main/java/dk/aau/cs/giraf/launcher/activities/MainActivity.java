package dk.aau.cs.giraf.launcher.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafCustomButtonsDialog;
import dk.aau.cs.giraf.launcher.BuildConfig;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.localdb.main;
import dk.aau.cs.giraf.utilities.NetworkUtilities;

/**
 * Displays the splash logo of Launcher, and then starts {@link dk.aau.cs.giraf.launcher.activities.AuthenticationActivity}.
 * Provides variables for enabling debug mode before compilation.
 */
public class MainActivity extends GirafActivity implements Animation.AnimationListener, GirafCustomButtonsDialog.CustomButtons {
    //private Context mContext;
    private long oldSessionGuardianID = -1;
    Animation startingAnimation;
    Animation loadAnimation;

    //Dialog for offline mode
    private final int OFFLINE_DIALOG_ID = 1337;
    private GirafCustomButtonsDialog offlineDialog;
    private final String OFFLINE_DIALOG_TAG = "DIALOG_TAG";



    /* ************* DEBUGGING MODE ************* */
    // NOTICE: ONLY USED FOR DEBUGGING PURPOSES!!!
    /**
     * If {@code true}, the Launcher is stated in debugging mode, where the splash screen and
     * authentication_activity is skipped.
     */
    private final boolean DEBUG_MODE = false;

    /**
     * If {@code true}, the authentication_activity screen is shown, despite debugging mode. Has no
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

    /**
     * Sets up the activity. Adds dummy data to the database if it's empty. Starts the splash animation,
     * if this is not disabled through debugging mode.
     *
     * @param savedInstanceState Information from the last launch of the activity.
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_MODE
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#SKIP_SPLASH_SCREEN
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        //Load the preference determining whether the animation should be shown
        findOldSession();

        boolean showAnimation = true;

        if (oldSessionGuardianID != -1) {
            Profile oldSessionProfile = new ProfileController(this).getById(oldSessionGuardianID);
            SharedPreferences prefs = SettingsUtility.getLauncherSettings(this, LauncherUtility.getSharedPreferenceUser(oldSessionProfile));
            showAnimation = prefs.getBoolean(getString(R.string.show_animation_preference_key), true);
        }

        // Skip loading screen if monkey test
        if (ActivityManager.isUserAMonkey()) {
            startNextActivity();
        }

        //Decide whether to skip animation, according to debug mode
        if ((DEBUG_MODE && SKIP_SPLASH_SCREEN) || !showAnimation) {
            startNextActivity();
        }
        //Load the splash animation
        startingAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_once);
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_infinite);

        startingAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);
        loadAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);

        findViewById(R.id.giraficon).startAnimation(startingAnimation);
        startingAnimation.setAnimationListener(this);

        //Check network status
        if (!NetworkUtilities.isNetworkAvailable(this)) {
            //Show offline mode dialog
            createAndShowOfflineDialog();
        } else {
            startSync();
        }
    }

    /**
     * Overrides the backbutton to do nothing, as the user should not be able to back out of this activity
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * Must be overridden to implement AnimationListener.
     * We do not need it for anything, though.
     *
     * @param animation The animation the function should act upon.
     */
    @Override
    public void onAnimationStart(Animation animation) {
    }

    /**
     * Changed the welcome text to a "fetching data" text and makes the application start loading data.
     *
     * @param animation The animation the function should act upon.
     */
    @Override
    public void onAnimationEnd(Animation animation) {
        findViewById(R.id.giraficon).startAnimation(loadAnimation);
    }

    /**
     * Must be overridden to implement AnimationListener.
     * We do not need it for anything, though.
     *
     * @param animation The animation the function should act upon.
     */
    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    /**
     * Starts the remote syncing service according to parameters set in the build config
     */
    private void startSync(){
        new main(this).startSynch(new MessageHandler(this, loadAnimation), BuildConfig.ENABLE_SYMMETRICDS, BuildConfig.USE_TEST_SERVER, BuildConfig.USE_PRODUCTION_SERVER);
    }

    /**
     * Starts offline
     */
    private void startOffline(){
        startNextActivity();
    }

    /**
     * Creates and shows the dialog which enables the user to launch
     * GIRAF in offline mode with limited functionality
     */
    private void createAndShowOfflineDialog(){
        offlineDialog = GirafCustomButtonsDialog.newInstance(
                getString(R.string.dialog_offline_title),
                getString(R.string.dialog_offline_message),
                OFFLINE_DIALOG_ID);
        offlineDialog.show(getSupportFragmentManager(), OFFLINE_DIALOG_TAG);
    }

    /**
     * Launches the next relevant activity, according to the current debugging mode, and to
     * whether any valid login data is detected in {@code SharedPerferences}.
     *
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_MODE
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#SKIP_AUTHENTICATION
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#DEBUG_AS_CHILD
     * @see dk.aau.cs.giraf.launcher.helper.LauncherUtility#sessionExpired(android.content.Context)
     */
    public void startNextActivity() {
        Intent intent;
        TextView welcomeText = (TextView) findViewById(R.id.welcome_title);
        welcomeText.setText("Klar!");

        if (DEBUG_MODE && SKIP_AUTHENTICATION) {
            intent = skipAuthentication(DEBUG_AS_CHILD);
        }
        //If no valid session is found, start authentication_activity
        else if (LauncherUtility.sessionExpired(this)) {
            intent = new Intent(this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        //If a valid session is found, pass the profile ID along with the intent.
        else {
            intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            final long guardianID = sharedPreferences.getLong(Constants.GUARDIAN_ID, -1L);
            final long childID = sharedPreferences.getLong(Constants.CHILD_ID, -1L);

            intent.putExtra(Constants.GUARDIAN_ID, guardianID);
            intent.putExtra(Constants.CHILD_ID, childID);
        }

        //If in debugging mode, set global variables.
        if (DEBUG_MODE) {
            LauncherUtility.setDebugging(DEBUG_MODE, DEBUG_AS_CHILD, this);
        }

        startActivity(intent);
        finish();
    }

    /**
     * Used for debugging mode.
     * Overrides the authentication by authenticating a test profile, and creating an intent
     * for starting {@code HomeActivity}. The guardian profile used is 'Tony Stark', and the child profile used
     * is 'Johnathan Doerwald'.
     *
     * @param asChild If {@code true}, a child profile is used for authentication. If {@code false}, a guardian
     *                profile is used for authentication.
     * @return An intent for starting {@code MainActivity} with the authenticated profile ID as an extra.
     */
    private Intent skipAuthentication(boolean asChild) {
        final Helper helper = LauncherUtility.getOasisHelper(this);
        Profile profile;

        //Get the relevant profile info.
        if (asChild) {
            profile = helper.profilesHelper.authenticateProfile("childqkxlnftvxquwrwcdloaumdhzkgyglezzsebpvnethrlstvmlorrolymdynjcyonkrtvcuagwigdqqkftsxxhklcnbhznthcqjxnjzzdoqvmfdlxrudcyakvrnfcbohdumawlwmfndjascmvrsoxfjgwzhdvcvqcroxoyjeazmxtrjtlkldoevgdrqvgfbklhtgm");
        } else {
            //profile = helper.profilesHelper.authenticateProfile("jkkxlagqyrztlrexhzofekyzrnppajeobqxcmunkqhsbrgpxdtqgygnmbhrgnpphaxsjshlpupgakmirhpyfaivvtpynqarxsghhilhkqvpelpreevykxurtppcggkzfaepihlodgznrmbrzgqucstflhmndibuymmvwauvdlyqnnlxkurinuypmqypspmkqavuhfwsh");
            profile = helper.profilesHelper.authenticateProfile("d74ecba82569eafc763256e45a126b4ce882f8a81327f28a380faa13eb2ec8f3");
        }

        final Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //Add the profile ID to the intent, and save information on the session.
        intent.putExtra(Constants.GUARDIAN_ID, profile.getId());

        LauncherUtility.saveLogInData(this, profile.getId(), new Date().getTime());
        return intent;
    }

    /**
     * Looks for a previously loaded session.
     * Loads the sharedpreferences if one exists
     */
    private void findOldSession() {
        if (LauncherUtility.sessionExpired(this)) {
            oldSessionGuardianID = -1L;
        } else {
            final SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            oldSessionGuardianID = sharedPreferences.getLong(Constants.GUARDIAN_ID, -1L);
        }
    }

    /**
     * Method which must be implemented for the CustomButtons dialog to work
     */
    @Override
    public void fillButtonContainer(int dialog_id, GirafCustomButtonsDialog.ButtonContainer buttonContainer) {
        if (dialog_id == OFFLINE_DIALOG_ID){
            GirafButton continueOffline = new GirafButton(this, getResources().getDrawable(R.drawable.icon_accept), getString(R.string.dialog_offline_just_do_it));
            continueOffline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startOffline();
                    offlineDialog.dismiss();
                }
            });
            GirafButton waitForConnection = new GirafButton(this, getResources().getDrawable(R.drawable.icon_cancel), getString(R.string.dialog_offline_wait_4_webz));
            waitForConnection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSync();
                    offlineDialog.dismiss();
                }
            });

            buttonContainer.addGirafButton(waitForConnection);
            buttonContainer.addGirafButton(continueOffline);
        }
    }

    /**
     * Used to communicate with DownloadService
     * To determine if a download is complete
     */
    public static class MessageHandler extends Handler {

        private final Animation loadAnimation;

        // Find the views that will be used
        final TextView welcomeTitle;
        final TextView welcomeDescription;
        final TextView progressTextPercent;
        final ProgressBar progressBar;
        final WeakReference<MainActivity> activity;

        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        public MessageHandler(final MainActivity activity, final Animation loadAnimation) {
            this.activity = new WeakReference<MainActivity>(activity);
            this.loadAnimation = loadAnimation;
            this.welcomeTitle = (TextView) activity.findViewById(R.id.welcome_title);
            this.welcomeDescription = (TextView) activity.findViewById(R.id.welcome_desciption);
            this.progressTextPercent = (TextView) activity.findViewById(R.id.progress_bar_text);
            this.progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
        }

        @Override
        public void handleMessage(Message message) {
            final int progress = message.arg1;
            if (progress >= 100) {
                executorService.shutdown();
                try {
                    while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) ;
                } catch (Exception e) { }
                this.activity.get().startNextActivity();
            } else {
                // Run the check on a background-thread
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        boolean hasConnectionTemp = NetworkUtilities.isNetworkAvailable(activity.get());

                        if (hasConnectionTemp) {
                            try {
                                HttpURLConnection urlc = (HttpURLConnection) new URL("http://clients3.google.com/generate_204").openConnection();
                                urlc.setRequestProperty("User-Agent", "Android");
                                urlc.setRequestProperty("Connection", "close");
                                urlc.setConnectTimeout(1500);
                                urlc.connect();
                                hasConnectionTemp = (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
                            } catch (IOException e) {
                                hasConnectionTemp = false;
                            }
                        }

                        final boolean hasConnection = hasConnectionTemp;

                        // Run the following on the UI-thread
                        activity.get().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (hasConnection) {
                                    loadAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);

                                    // Update the views accordingly to the progress
                                    welcomeTitle.setText("Henter data...");

                                    welcomeDescription.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.VISIBLE);
                                    progressTextPercent.setVisibility(View.VISIBLE);

                                    progressTextPercent.setText(progress + "%");
                                    progressBar.setProgress(progress);

                                    welcomeTitle.setTextColor(activity.get().getResources().getColor(R.color.giraf_loading_textColor));
                                } else {
                                    // Cancel the animation and make is very slow (in order to stop it)
                                    loadAnimation.cancel();
                                    loadAnimation.setDuration(Long.MAX_VALUE);

                                    progressBar.setVisibility(View.GONE);
                                    progressTextPercent.setVisibility(View.GONE);
                                    welcomeDescription.setVisibility(View.VISIBLE);

                                    welcomeTitle.setText("Ingen internet forbindelse!");
                                    welcomeDescription.setText("Opret forbindelse til internettet for at hente data");

                                    welcomeTitle.setTextColor(Color.parseColor("#900000"));
                                    welcomeDescription.setTextColor(Color.parseColor("#900000"));
                                }
                            }
                        });

                        Log.d("Progress", Integer.toString(progress));
                    }
                });
            }
        }
    }
}
