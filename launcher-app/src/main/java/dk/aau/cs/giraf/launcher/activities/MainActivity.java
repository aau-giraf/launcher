package dk.aau.cs.giraf.launcher.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Date;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.core.data.Data;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.launcher.BuildConfig;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;

/**
 * Displays the splash logo of Launcher, and then starts {@link dk.aau.cs.giraf.launcher.activities.AuthenticationActivity}.
 * Provides variables for enabling debug mode before compilation.
 */
public class MainActivity extends GirafActivity {
    //private Context mContext;
    private long oldSessionGuardianID = -1;

    /* ************* DEBUGGING MODE ************* */
    // TODO: ONLY USED FOR DEBUGGING PURPOSES!!!
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


        //Load the preference determining whether the animation should be shown
        findOldSession();

        boolean showAnimation = true;

        if (oldSessionGuardianID != -1) {
            Profile oldSessionProfile = new ProfileController(this).getProfileById(oldSessionGuardianID);
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



        Intent downloadService = new Intent(this, Data.class); //dk.aau.cs.giarf.core.data.Data.class

        downloadService.putExtra("ENABLE_SYMMETRICDS", BuildConfig.ENABLE_SYMMETRICDS);
        downloadService.putExtra("USE_TEST_SERVER", BuildConfig.USE_TEST_SERVER);
        downloadService.putExtra("USE_PRODUCTION_SERVER", BuildConfig.USE_PRODUCTION_SERVER);


        startActivity(downloadService);

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
        }
        //If a valid session is found, pass the profile ID along with the intent.
        else {
            intent = new Intent(this, HomeActivity.class);

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
}