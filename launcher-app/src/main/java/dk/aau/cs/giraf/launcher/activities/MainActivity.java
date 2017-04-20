package dk.aau.cs.giraf.launcher.activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
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

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.gui.GirafNotifyDialog;
import dk.aau.cs.giraf.launcher.BuildConfig;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.utilities.NetworkUtilities;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Displays the splash logo of Launcher, and then starts
 * {@link dk.aau.cs.giraf.launcher.activities.AuthenticationActivity}.
 * Provides variables for enabling debug mode before compilation.
 */
public class MainActivity extends GirafActivity implements Animation.AnimationListener, GirafNotifyDialog.Notification {

    private long oldSessionGuardianId = -1;
    private Animation startingAnimation;
    private Animation loadAnimation;

    //Dialog for offline mode
    private boolean offlineMode;
    private final int offlineDialogId = 1337;
    private GirafNotifyDialog offlineDialog;
    private final String offlineDialogTag = "DIALOG_TAG";
    private final Helper helper = new Helper(this);
    private final Handler handler = new Handler();
    /* ************* DEBUGGING MODE ************* */
    // NOTICE: ONLY USED FOR DEBUGGING PURPOSES!!!
    /**
     * If {@code true}, the Launcher is stated in debugging mode, where the splash screen and
     * authentication_activity is skipped.
     */
    private final boolean debugMode = false;

    /**
     * If {@code true}, the authentication_activity screen is shown, despite debugging mode. Has no
     * effect if {@code debugMode} is {@code false}.
     */
    private final boolean skipAuthentication = false;

    /**
     * If {@code true}, the splash screen is shown, despite debugging mode. Has no
     * effect if {@code debugMode} is {@code false}.
     */
    private final boolean skipSplashScreen = false;

    /**
     * If {@code true}, Launcher automatically logs in with a child profile. If {@code false},
     * Launcher logs in with a guardian profile. Has no effect if {@code debugMode} is {@code false},
     * or if {@code skipAuthentication} is {@code true}.
     */
    private final boolean debugAsChild = false;
    /* ****************************************** */

    /**
     * Sets up the activity. Adds dummy data to the database if it's empty. Starts the splash animation,
     * if this is not disabled through debugging mode.
     *
     * @param savedInstanceState Information from the last launch of the activity.
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#debugMode
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#skipSplashScreen
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        //Load the preference determining whether the animation should be shown
        findOldSession();

        boolean showAnimation = true;

        if (oldSessionGuardianId != -1) {
            Profile oldSessionProfile = new ProfileController(this).getById(oldSessionGuardianId);
            SharedPreferences prefs = SettingsUtility.getLauncherSettings(this,
                LauncherUtility.getSharedPreferenceUser(oldSessionProfile));
            showAnimation = prefs.getBoolean(getString(R.string.show_animation_preference_key), true);
        }

        // Skip loading screen if monkey test
        if (ActivityManager.isUserAMonkey()) {
            startNextActivity();
        }

        //Decide whether to skip animation, according to debug mode
        if ((debugMode && skipSplashScreen) || !showAnimation) {
            startNextActivity();
        }
        //Load the splash animation
        startingAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_once);
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_infinite);

        startingAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);
        loadAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);

        findViewById(R.id.giraficon).startAnimation(startingAnimation);
        startingAnimation.setAnimationListener(this);

        //Todo plz remember to remove this again
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        /*
        //Check network status
        if (offlineMode()  && !hasProfiles()) {
            //Show offline mode dialog because the local db has no profiles
            createAndShowOfflineDialog();
            //use start sync to show "no connection" feedback
            startSync();
            //Check every 2.5 seconds if internet is available
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!offlineMode()) {
                        //If a connection to the internet is made
                        restartLauncher();
                        return;
                    }
                    handler.postDelayed(this, 2500);
                }
            }, 2500);
        } else {
            if (offlineMode()) {
                startOffline();
            } else {
                startSync();
            }
        }*/
    }

    /**
     * Overrides the backbutton to do nothing, as the user should not be able to back out of this activity.
     *
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
     * Check if offline mode
     * @return true if offline i.e. no connection to outside world
     */
    private boolean offlineMode() {
        return !NetworkUtilities.isNetworkAvailable(this);
    }

    /**
     * Restart the launcher by killing myself then launching again.
     */
    private void restartLauncher() {
        Intent startActivity = new Intent(getApplicationContext(), MainActivity.class);
        int pendingIntentId = 123456;
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
            pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        System.exit(0);
    }

    /**
     * Check if the local db has any profiles.
     */
    private boolean hasProfiles() {
        return !(helper.profilesHelper.getListOfObjects() == null ||
            helper.profilesHelper.getListOfObjects().size() == 0);
    }

    /**
     * Starts the remote syncing service according to parameters set in the build config.
     */
    private void startSync() {
        //new main(this).startSynch(new MessageHandler(this, loadAnimation),
           //BuildConfig.ENABLE_SYMMETRICDS, BuildConfig.USE_TEST_SERVER, BuildConfig.USE_PRODUCTION_SERVER);
    }

    /**
     * Starts offline.
     */
    private void startOffline() {
        startNextActivity();
    }

    /**
     * Creates and shows the dialog which enables the user to launch.
     * GIRAF in offline mode with limited functionality
     */
    private void createAndShowOfflineDialog() {
        offlineDialog = GirafNotifyDialog.newInstance(
                getString(R.string.dialog_offline_title),
                getString(R.string.dialog_offline_message),
            offlineDialogId);
        offlineDialog.show(getSupportFragmentManager(), offlineDialogTag);
    }

    /**
     * Launches the next relevant activity, according to the current debugging mode, and to
     * whether any valid login data is detected in {@code SharedPerferences}.
     *
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#debugMode
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#skipAuthentication
     * @see dk.aau.cs.giraf.launcher.activities.MainActivity#debugAsChild
     * @see dk.aau.cs.giraf.launcher.helper.LauncherUtility#sessionExpired(android.content.Context)
     */
    public void startNextActivity() {
        Intent intent;
        TextView welcomeText = (TextView) findViewById(R.id.welcome_title);

        if (debugMode && skipAuthentication) {
            intent = skipAuthentication(debugAsChild);
        } else if (LauncherUtility.sessionExpired(this)) {
            // If no valid session is found, start authentication_activity
            intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            //If a valid session is found, pass the profile ID along with the intent.
            intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            final long guardianId = sharedPreferences.getLong(Constants.GUARDIAN_ID, -1L);
            final long childId = sharedPreferences.getLong(Constants.CHILD_ID, -1L);

            intent.putExtra(Constants.GUARDIAN_ID, guardianId);
            intent.putExtra(Constants.CHILD_ID, childId);
        }

        //If in debugging mode, set global variables.
        if (debugMode) {
            LauncherUtility.setDebugging(debugMode, debugAsChild, this);
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
            profile = helper.profilesHelper.authenticateProfile(
                "childqkxlnftvxquwrwcdloaumdhzkgyglezzsebpvnethrlstvmlorrolymdynjcyonkrtvcuagwigdqqkftsxxhk" +
                    "lcnbhznthcqjxnjzzdoqvmfdlxrudcyakvrnfcbohdumawlwmfndjascmvrsoxfjgwzhdvcvqcroxoyjeazmxt" +
                    "rjtlkldoevgdrqvgfbklhtgm");
        } else {
            //profile = helper.profilesHelper.authenticateProfile("jkkxlagqyrztlrexhzofekyzrnppajeobqxcmunkqhsbrgpxdtqgy
            // gnmbhrgnpphaxsjshlpupgakmirhpyfaivvtpynqarxsghhilhkqvpelpreevykxurtppcggkzfaepihlodgznrmbrzgqucstflhmndibu
            // ymmvwauvdlyqnnlxkurinuypmqypspmkqavuhfwsh");
            profile = helper.profilesHelper.authenticateProfile(
                "d74ecba82569eafc763256e45a126b4ce882f8a81327f28a380faa13eb2ec8f3");
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
            LauncherUtility.logOutIntent(this);
            oldSessionGuardianId = -1L;
        } else {
            final SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            oldSessionGuardianId = sharedPreferences.getLong(Constants.GUARDIAN_ID, -1L);
        }
    }

    /**
     * Method which must be implemented for the CustomButtons dialog to work.
     */
    @Override
    public void noticeDialog(int id) {
        if (id == offlineDialogId) {
            offlineDialog.dismiss();
        }
    }


    /**
     * Used to communicate with .
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

        /**
         * The constructer for the messageHandler.
         * @param activity the activity.
         * @param loadAnimation the loadanimation.
         */
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
                    while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) { //Does this code spam the log?
                        Log.d("Launcher","EcecutorService timeout before termination");
                    }
                } catch (InterruptedException e) {
                    Log.d("Launcher", "ExecutorService was interrupted");
                }
                this.activity.get().startNextActivity();
            } else {
                // Run the check on a background-thread
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {

                        // Run the following on the UI-thread
                        activity.get().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                boolean hasConnection = NetworkUtilities.isNetworkAvailable(activity.get());
                                if (hasConnection) {
                                    loadAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);

                                    // Update the views accordingly to the progress
                                    welcomeTitle.setText(R.string.main_activity_download_data);

                                    welcomeDescription.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.VISIBLE);
                                    progressTextPercent.setVisibility(View.VISIBLE);

                                    progressTextPercent.setText(progress + "%");
                                    progressBar.setProgress(progress);

                                    welcomeTitle.setTextColor(activity.get().getResources()
                                        .getColor(R.color.giraf_loading_textColor));
                                } else {
                                    // Cancel the animation and make is very slow (in order to stop it)
                                    loadAnimation.cancel();
                                    loadAnimation.setDuration(Long.MAX_VALUE);

                                    progressBar.setVisibility(View.GONE);
                                    progressTextPercent.setVisibility(View.GONE);
                                    welcomeDescription.setVisibility(View.VISIBLE);

                                    welcomeTitle.setText(R.string.main_activity_no_network);
                                    welcomeDescription.setText(R.string.main_activity_connect_to_download);

                                    welcomeTitle.setTextColor(Color.parseColor("#900000"));
                                    welcomeDescription.setTextColor(Color.parseColor("#900000"));
                                }
                                Log.d("Connection", hasConnection ? "True" : "False");
                            }
                        });
                        Log.d("Progress", Integer.toString(progress));
                    }
                });
            }
        }
    }
}
