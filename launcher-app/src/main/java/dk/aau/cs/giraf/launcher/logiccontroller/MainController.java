package dk.aau.cs.giraf.launcher.logiccontroller;

import android.content.Intent;
import android.util.Log;
import dk.aau.cs.giraf.launcher.activities.LoginActivity;
import dk.aau.cs.giraf.launcher.activities.MainActivity;
import dk.aau.cs.giraf.utilities.IntentConstants;

public class MainController {

    private MainActivity gui;

    public MainController(MainActivity gui) {
        this.gui = gui;
    }

    /**
     * Launches the next relevant activity.
     */
    public void startNextActivity() {
        String restartString = null;
        if(gui.getIntent() != null && gui.getIntent().getExtras() != null) {
            restartString = gui.getIntent().getExtras().getString(IntentConstants.STARTED_BY);
        }
        Intent intent = new Intent(gui, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(restartString != null){
            Log.i("test","starting");
            intent.putExtra(IntentConstants.STARTED_BY,restartString);
        }
        gui.startActivity(intent);
        gui.finish();
    }
}
