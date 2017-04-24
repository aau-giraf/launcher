package dk.aau.cs.giraf.launcher.activities;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.logiccontroller.LoginController;
import dk.aau.cs.giraf.utilities.GrayScaleHelper;

public class LoginActivity extends GirafActivity{

    private LoginController controller;


/*
* Constructer
*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTrackingId("UA-48608499-1");
        controller = new LoginController(this);
        setContentView(R.layout.authentication_activity2);
        findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftKeyboard();
                return false;
            }
        });

    }


/*
* Override methods
*/
    /**
     * Does nothing, to prevent the user from returning to the splash screen or native OS.
     */
    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }

/*
* Gui methods
*/
    public void LoginBtnPressed(View view){
        Toast.makeText(this,"Test",Toast.LENGTH_LONG).show();
        controller.login(1L);
    }


    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
}
