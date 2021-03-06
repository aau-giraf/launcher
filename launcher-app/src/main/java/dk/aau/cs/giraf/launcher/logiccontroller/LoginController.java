package dk.aau.cs.giraf.launcher.logiccontroller;

import android.content.Intent;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.LoginActivity;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.Department;
import dk.aau.cs.giraf.models.core.User;
import dk.aau.cs.giraf.utilities.IntentConstants;

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
                        new GetRequest<User>(User.class, new Response.Listener<User>() {
                            //Passes the userinfo to homeIntent
                            @Override
                            public void onResponse(User response) {
                                startLauncherHomeActivity(response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if(error!=null && error.networkResponse !=null) {
                                    //The user is for some reason unavailable
                                    Log.e("Launcher Network", "Error code "+  error.networkResponse.statusCode +" on get user request");
                                    RequestQueueHandler handler = RequestQueueHandler.getInstance(gui.getApplicationContext());
                                    handler.login(new Response.Listener<Integer>() {
                                        @Override
                                        public void onResponse(Integer response) {

                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                        }
                                    });
                                    gui.showDialogWithMessage(gui.getString(R.string.error_try_again));
                                }
                            }
                        });
                    queue.add(userGetRequest);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error!=null && error.networkResponse !=null) {
                        int errorCode = error.networkResponse.statusCode;
                        if (errorCode == 401) {
                            //The username and/or password are incorrect
                            Log.e("Launcher Network", "Error code 401 on login request");
                            gui.showDialogWithMessage(gui.getString(R.string.error_username_password));
                        }
                        else if (errorCode == 400) {
                            //Bad syntax sent to the server (aka blank password or username)
                            Log.e("Launcher Network","Error code 400 on login request");
                            gui.showDialogWithMessage(gui.getString(R.string.error_bad_syntax));
                        }
                        else {
                            //The server is for some reason unavailable
                            Log.e("Launcher Network", "Error code "+  error.networkResponse.statusCode +" on login request");
                            gui.showDialogWithMessage(gui.getString(R.string.error_try_again));
                        }
                    }
                    else{
                        if(error != null) {
                            Log.e("Launcher Network", error.getMessage());
                        }
                        else{
                            Log.e("Launcher Network", "Error was null");
                        }
                        gui.showDialogWithMessage(gui.getString(R.string.error_try_again));
                    }
                }
            }
        );
        //The login request is added to the queue
        queue.add(loginRequest);
    }

    public void startLauncherHomeActivity(User currentUser){

        Intent homeIntent = new Intent(gui, HomeActivity.class);

        //If the logged in user is a department, we open a profileSelector to choose guardian
        //ToDo Change to actually check if we are a department.
        //Also, lets finish this once we actually can compile and have the server backend
        if(false){
                                    /*GirafProfileSelectorDialog chooseGuardian = GirafProfileSelectorDialog.newInstance(
                                        LoginController.this, response, false, false, getString(R.string.home_activity_change_to_citizen_msg),
                                        CHANGE_USER_SELECTOR_DIALOG, queue);
                                    changeUser.show(getSupportFragmentManager(), "" + CHANGE_USER_SELECTOR_DIALOG);*/
        }











        homeIntent.putExtra(IntentConstants.CURRENT_USER,currentUser);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        gui.startActivity(homeIntent);
    }
}
