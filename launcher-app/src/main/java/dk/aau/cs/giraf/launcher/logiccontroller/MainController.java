package dk.aau.cs.giraf.launcher.logiccontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TextView;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.LoginActivity;
import dk.aau.cs.giraf.launcher.activities.MainActivity;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;

import java.util.Date;

public class MainController {

    private MainActivity gui;
    private long oldSessionId = -1L;

    public MainController(MainActivity gui) {
        this.gui = gui;
    }

    /**
     * Launches the next relevant activity, according to the current debugging mode, and to
     * whether any valid login data is detected in {@code SharedPerferences}.
     *
     * @see dk.aau.cs.giraf.launcher.helper.LauncherUtility#sessionExpired(android.content.Context)
     */
    public void startNextActivity() {
        Intent intent;
        if (LauncherUtility.sessionExpired(gui)) {
            // If no valid session is found, start authentication_activity
            intent = new Intent(gui, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            //If a valid session is found, pass the profile ID along with the intent.
            intent = new Intent(gui, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            SharedPreferences sharedPreferences = gui.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            final long guardianId = sharedPreferences.getLong(Constants.GUARDIAN_ID, -1L);
            final long childId = sharedPreferences.getLong(Constants.CHILD_ID, -1L);

            intent.putExtra(Constants.GUARDIAN_ID, guardianId);
            intent.putExtra(Constants.CHILD_ID, childId);
        }

        gui.startActivity(intent);
        gui.finish();
    }

    public long getOldSessionId(){
        SharedPreferences sharedPreferences = gui.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
        return sharedPreferences.getLong(Constants.GUARDIAN_ID, -1L);
    }


    /**
     * Looks for a previously loaded session.
     * Loads the sharedpreferences if one exists
     */
    private void findOldSession() {
        if (LauncherUtility.sessionExpired(gui)) {
            LauncherUtility.logOutIntent(gui);
            oldSessionId = -1L;
        } else {
            final SharedPreferences sharedPreferences = gui.getSharedPreferences(Constants.LOGIN_SESSION_INFO, 0);
            oldSessionId = sharedPreferences.getLong(Constants.GUARDIAN_ID, -1L);
        }
    }
}
