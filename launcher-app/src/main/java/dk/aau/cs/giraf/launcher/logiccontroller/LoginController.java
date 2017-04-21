package dk.aau.cs.giraf.launcher.logiccontroller;

import android.app.Activity;
import android.content.Intent;
import dk.aau.cs.giraf.launcher.activities.AuthenticationActivity;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.LoginActivity;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;

import java.util.Date;

public class LoginController {

    private LoginActivity gui;

    public LoginController(LoginActivity gui) {
        this.gui = gui;
    }

    private void login(Long id) {
        Intent homeIntent = new Intent(gui, HomeActivity.class);
        homeIntent.putExtra(Constants.GUARDIAN_ID, id);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LauncherUtility.saveLogInData(gui, id, new Date().getTime());
        gui.startActivity(homeIntent);
    }


}
