package dk.aau.cs.giraf.launcher.logiccontroller;

import android.content.Intent;
import com.android.volley.*;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.LoginActivity;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.User;

import java.util.Date;

public class LoginController {

    private LoginActivity gui;
    private RequestQueue queue;

    public LoginController(LoginActivity gui) {
        this.gui = gui;
    }

    /**
     * Verifies a username and password and if correct logs in with the information.
     *
     * @param username The users username.
     * @param password The users password.
     */
    public void login(final String username, String password) {
        queue = RequestQueueHandler.getInstance(gui.getApplicationContext()).getRequestQueue();

        //Creates a login request which is then added to the queue later
        LoginRequest loginRequest = new LoginRequest(username, password,
            new Response.Listener<Integer>() {
                @Override
                public void onResponse(Integer statusCode) {
                    //Creates a GetRequest for the user, which is then added to the queue within the loginRequest
                    GetRequest<User> userGetRequest =
                        new GetRequest<User>(username, User.class, new Response.Listener<User>() {
                            //Passes the userinfo to homeIntent
                            @Override
                            public void onResponse(User response) {
                                Intent homeIntent = new Intent(gui, HomeActivity.class);
                                //ToDo Send user instead of ID
                                homeIntent.putExtra(Constants.GUARDIAN_ID, response.getId());
                                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                LauncherUtility.saveLogInData(gui, response.getId(), new Date().getTime());
                                gui.startActivity(homeIntent);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //The user is for some reason unavailable
                                gui.showDialogWithMessage(gui.getString(R.string.error_try_agian));
                            }
                        });
                    queue.add(userGetRequest);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    int errorCode = error.networkResponse.statusCode;
                    if (errorCode == 401) {
                        //The username and/or password are incorrect
                        gui.showDialogWithMessage(gui.getString(R.string.error_username_password));
                    } else {
                        //The server is for some reason unavailable
                        gui.showDialogWithMessage(gui.getString(R.string.error_try_agian));
                    }
                }
            }
        );
        //The login request is added to the queue
        queue.add(loginRequest);
    }
}
