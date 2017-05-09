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
import dk.aau.cs.giraf.gui.GirafPopupDialog;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.logiccontroller.LoginController;

public class LoginActivity extends GirafActivity {

    private LoginController controller;
    private TextView usernameTextBox;
    private TextView passwordTextBox;
    private Button loginButton;
    private Animation loadAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTrackingId("UA-48608499-1");
        controller = new LoginController(this);
        setContentView(R.layout.login_activity);
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.main_activity_rotatelogo_infinite);
        loadAnimation.setDuration(Constants.LOGO_ANIMATION_DURATION);
        usernameTextBox = (TextView) findViewById(R.id.username_textbox);
        passwordTextBox = (TextView) findViewById(R.id.password_textbox);
        loginButton = (Button) findViewById(R.id.login_button);
        findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftKeyboard();
                return false;
            }
        });
    }


    /**
     * Does nothing, to prevent the user from returning to the splash screen or native OS.
     */
    @Override
    public void onBackPressed() {
        //Do nothing, as the user should not be able to back out of this activity
    }


    /**
     * Called from login_activity.xml using the onClick event of the login button.
     * @param view the view.
     */
    public void onLoginButtonClick(View view) {
        loginButton.setEnabled(false);
        usernameTextBox.setEnabled(false);
        passwordTextBox.setEnabled(false);
        String username = usernameTextBox.getText().toString();
        String password = passwordTextBox.getText().toString();
        findViewById(R.id.girafHeaderIcon).startAnimation(loadAnimation);
        controller.login(username, password);
    }


    /**
     * Shows a dialog with a message.
     * ToDo change to giraf dialog when it is finished.
     * @param message the message.
     */
    public void showDialogWithMessage(String message) {
        final GirafPopupDialog errorDialog = new GirafPopupDialog(R.string.error_login,message,this);
        errorDialog.setButton1(android.R.string.ok, R.drawable.icon_accept, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.girafHeaderIcon).clearAnimation();
                errorDialog.cancel();
                reEnableGuiControls();
            }
        });
        errorDialog.show();
    }


    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }


    private void reEnableGuiControls() {
        loginButton.setEnabled(true);
        usernameTextBox.setEnabled(true);
        passwordTextBox.setEnabled(true);
    }
}
