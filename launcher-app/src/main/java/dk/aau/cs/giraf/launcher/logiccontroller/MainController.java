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
import dk.aau.cs.giraf.models.core.User;

import java.util.Date;

public class MainController {

    private MainActivity gui;

    public MainController(MainActivity gui) {
        this.gui = gui;
    }

    /**
     * Launches the next relevant activity.
     */
    public void startNextActivity() {
        Intent intent = new Intent(gui, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        gui.startActivity(intent);
        gui.finish();
    }
}
