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
import dk.aau.cs.giraf.librest.requests.PictogramRequest;
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

    Map<String, String> header = new HashMap<>();
    private String base_url ="http://web.giraf.cs.aau.dk:5000/pictogram/";
    private String get_url = "1";
    private String url = base_url + get_url;

    public LoginController(LoginActivity gui) {
        this.gui = gui;
    }

    public void login(Long id) {
        Intent homeIntent = new Intent(gui, HomeActivity.class);
        homeIntent.putExtra(Constants.GUARDIAN_ID, id);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LauncherUtility.saveLogInData(gui, id, new Date().getTime());
        gui.startActivity(homeIntent);
    }

    public void login(String username, String password){

    }

    public void authentication(String username, String password) {
        queue = RequestQueueHandler.getInstance(gui.getApplicationContext()).getRequestQueue();

        Department department = new Department("dep");
        User user = new User(department, username, password);

        LoginRequest loginRequest = new LoginRequest(user,
                new Response.Listener<Integer>() {
                    @Override
                    public void onResponse(Integer statusCode) {
                        //printLoginResponse(String.valueOf(statusCode));
                        //<editor-fold desc="PictogramRequest Get Test">
                        // Request a string response from the provided URL.
                        PictogramRequest requestGet = new PictogramRequest(Request.Method.GET, base_url + "35", header,
                                new Response.Listener<Pictogram>(){
                                    @Override
                                    public void onResponse(Pictogram response) {
                                        //printPictogramResponse(response, "GET");
                                    }
                                },
                                new Response.ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //printError(error, "GET");
                                    }
                                });

                        //</editor-fold>
                        queue.add(requestGet);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //printError(error, "LOGIN");
                    }
                }
        );
        //</editor-fold>
        queue.add(loginRequest);
        //consoleWriteLine("Awaiting responses");
    }
}
