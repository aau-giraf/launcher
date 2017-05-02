package dk.aau.cs.giraf.launcher.logiccontroller;

import android.app.Activity;
import android.content.Intent;

//These are taken off the testapp
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.VolleyError;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.librest.serialization.Translator;
import dk.aau.cs.giraf.models.core.AccessLevel;
import dk.aau.cs.giraf.models.core.Department;
import dk.aau.cs.giraf.models.core.Pictogram;
import dk.aau.cs.giraf.models.core.User;
import java.util.HashMap;
import java.util.Map;

import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.LoginActivity;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;


import java.util.Date;

public class LoginController {

    private LoginActivity gui;
    private RequestQueue queue;


    public LoginController(LoginActivity gui) {
        this.gui = gui;
    }

    public void login(String username, String password) {
        queue = RequestQueueHandler.getInstance(gui.getApplicationContext()).getRequestQueue();


        LoginRequest loginRequest = new LoginRequest(username, password,
                new Response.Listener<Integer>() {
                    @Override
                    public void onResponse(Integer statusCode) {
                        Intent homeIntent = new Intent(gui, HomeActivity.class);
                        //homeIntent.putExtra(Constants.GUARDIAN_ID, id);
                        //homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //LauncherUtility.saveLogInData(gui, id, new Date().getTime());
                        gui.startActivity(homeIntent);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        gui.ShowDialogWithMessage("temp fejl msg");
                    }
                }
        );
        //</editor-fold>
        queue.add(loginRequest);
        //consoleWriteLine("Awaiting responses");
    }
}
