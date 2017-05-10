package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.librest.requests.GetRequest;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.PutRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.User;

/**
 * This fragment contains the settings for Launcher itself, meaning the iconsize and
 * whether to show the starting animation or not.
 */
public class SettingsLauncher extends Fragment {

    private static String USER_IDENTIFICATION = "currentUser";
    private Switch grayScale;
    private User currentUser;
    private RequestQueue queue;


    /**
     * Returns a new instance of the settings launcher.
     *
     * @param user The user.
     * @return the new instance.
     */
    public static SettingsLauncher newInstance(User user) {
        final SettingsLauncher newFragment = new SettingsLauncher();
        Bundle args = new Bundle();
        args.putSerializable(USER_IDENTIFICATION, user);
        newFragment.setArguments(args);
        return newFragment;
    }


    /**
     * OnCreate is per usual overridden to instantiate the mayority of the variables of the class
     * It loads the SharedPreferences of the user and sets the layout accordingly.
     *
     * @param savedInstanceState The previously SavedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = RequestQueueHandler.getInstance(getActivity().getApplicationContext()).getRequestQueue();
        final Bundle arguments = getArguments();
        if (arguments != null) {
            currentUser = (User) arguments.getSerializable(USER_IDENTIFICATION);
        }
        GetRequest<User> userGetRequest = new GetRequest<User>(currentUser.getId(), User.class, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                currentUser = response; //ToDo i know it is wrong, but it is okay.
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    LoginRequest loginRequest = new LoginRequest(currentUser, new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            GetRequest<User> userGetRequest = new GetRequest<User>(currentUser.getId(), User.class, new Response.Listener<User>() {
                                @Override
                                public void onResponse(User response) {
                                    currentUser = response; //ToDo i know it is wrong, but it is okay.
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("Launcher", "Something went wrong with get user request");
                                }
                            });
                            queue.add(userGetRequest);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Launcher", "Something went wrong with get user request");
                        }
                    });
                    queue.add(loginRequest);
                } else {
                    Log.e("Launcher", "Something went wrong with get user request");
                }
            }
        });
        queue.add(userGetRequest);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.settings_launcher, null);
        grayScale = (Switch) view.findViewById(R.id.toggle_gray_scale);
        grayScale.setChecked(currentUser.getSettings().getUseGrayScale());
        grayScale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentUser.getSettings().setUseGrayScale(isChecked);
                final PutRequest<User> userPutRequest = new PutRequest<User>(currentUser, currentUser.getId(), new Response.Listener<Integer>() {
                    @Override
                    public void onResponse(Integer response) {
                        Log.i("Launcher", "Put user request success for SettingsLauncher");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LoginRequest loginRequest = new LoginRequest(currentUser, new Response.Listener<Integer>() {
                            @Override
                            public void onResponse(Integer response) {
                                PutRequest<User> userPutRequest1 = new PutRequest<User>(currentUser, currentUser.getId(), new Response.Listener<Integer>() {
                                    @Override
                                    public void onResponse(Integer response) {
                                        Log.i("Launcher", "Put user request success for SettingsLauncher");
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("Launcher", "Put user request failed for SettingsLauncher");
                                    }
                                });
                                queue.add(userPutRequest1);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Launcher", "Put user request failed for SettingsLauncher");
                            }
                        });
                        queue.add(loginRequest);
                    }
                });
                queue.add(userPutRequest);
            }
        });

        return view;
    }
}
