package dk.aau.cs.giraf.launcher.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.logiccontroller.LoginController;

public class LoginActivity extends GirafActivity{

    private LoginController controller;
    private TextView usernameTextBox;
    private TextView passwordTextBox;
    private Button loginButton;
    private Animation loadAnimation;

/*
* Constructer
*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTrackingId("UA-48608499-1");
        controller = new LoginController(this);
        setContentView(R.layout.authentication_activity2);
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_infinite);
        loadAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);
        usernameTextBox = (TextView) findViewById(R.id.username_box);
        passwordTextBox = (TextView) findViewById(R.id.pass_box);
        loginButton = (Button) findViewById(R.id.login_btn);
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
    public void loginBtnPressed(View view){
        loginButton.setEnabled(false);
        String username = usernameTextBox.getText().toString();
        String password = passwordTextBox.getText().toString();
        usernameTextBox.setEnabled(false);
        passwordTextBox.setEnabled(false);
        findViewById(R.id.girafHeaderIcon).startAnimation(loadAnimation);
        controller.login(username, password);
    }



    public void showDialogWithMessage(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this,R.style.GirafTheme);
        dialog.setTitle(R.string.error_login);
        dialog.setMessage(message);
        dialog.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                findViewById(R.id.girafHeaderIcon).clearAnimation();
                dialogInterface.cancel();
                reEnableGuiControls();
            }
        });
        dialog.show();
    }


    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }


    private void reEnableGuiControls(){
        loginButton.setEnabled(true);
        usernameTextBox.setEnabled(true);
        passwordTextBox.setEnabled(true);
    }
}
