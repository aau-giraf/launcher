package dk.aau.cs.giraf.launcher.logiccontroller;

import android.content.Intent;
import dk.aau.cs.giraf.launcher.activities.LoginActivity;
import dk.aau.cs.giraf.launcher.activities.MainActivity;

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
