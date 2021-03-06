package dk.aau.cs.giraf.launcher.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.widgets.GridPreviewView;
import dk.aau.cs.giraf.librest.requests.*;
import dk.aau.cs.giraf.models.core.Settings;
import dk.aau.cs.giraf.models.core.User;
import dk.aau.cs.giraf.utilities.GrayScaleHelper;
import dk.aau.cs.giraf.utilities.IntentConstants;

/**
 * This fragment contains the settings for Launcher itself, meaning the iconsize and
 * whether to show the starting animation or not.
 */
public class SettingsLauncher extends Fragment {

    private static String USER_IDENTIFICATION = "currentUser";
    private Switch grayScale;
    private GridPreviewView previewView;
    private User currentUser;
    private RequestQueueHandler handler;
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
        handler = RequestQueueHandler.getInstance(getActivity().getApplicationContext());
        queue = handler.getRequestQueue();
        final Bundle arguments = getArguments();
        if (arguments != null) {
            currentUser = (User) arguments.getSerializable(USER_IDENTIFICATION);
        }

        //Used to get an updated version of the user
        GetRequest<User> userGetRequest = new GetRequest<User>( User.class, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                currentUser = response;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    LoginRequest loginRequest = new LoginRequest(currentUser, new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            GetRequest<User> userGetRequest = new GetRequest<User>( User.class, new Response.Listener<User>() {
                                @Override
                                public void onResponse(User response) {
                                    currentUser = response;
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
        View view = inflater.inflate(R.layout.settings_launcher_new, null);
        grayScale = (Switch) view.findViewById(R.id.toggle_gray_scale);
        grayScale.setChecked(currentUser.getSettings().getUseGrayScale());
        grayScale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentUser.getSettings().setUseGrayScale(isChecked);
                GrayScaleHelper.setGrayScaleForActivity(getActivity(),isChecked);
                handler.resourceRequest(currentUser.getSettings(), new Response.Listener<Settings>() {
                    @Override
                    public void onResponse(Settings response) {
                        Log.i("Launcher", "Put user settings request success for SettingsLauncher");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handler.login(currentUser, new Response.Listener<Integer>() {
                            @Override
                            public void onResponse(Integer response) {
                                handler.resourceRequest(currentUser.getSettings(), new Response.Listener<Settings>() {
                                    @Override
                                    public void onResponse(Settings response) {
                                        Log.i("Launcher", "Put user settings request success for SettingsLauncher");
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("Launcher", "Put user request failed for SettingsLauncher");
                                    }
                                });
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Launcher", "Put user request failed for SettingsLauncher");
                            }
                        });
                    }
                });
            }
        });
        previewView = (GridPreviewView) view.findViewById(R.id.example_grid_layout);
        previewView.setColumnSize(currentUser.getSettings().getAppsGridSizeColumns());
        previewView.setRowSize(currentUser.getSettings().getAppsGridSizeRows());
        previewView.invalidate();

        //Set SeekBar Listeners
        final SeekBar sk = (SeekBar) view.findViewById(R.id.gridResizerSeekBar);
        sk.setProgress(currentUser.getSettings().getAppsGridSizeRows());
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                currentUser.getSettings().setAppsGridSizeRows(seekBar.getProgress());
                currentUser.getSettings().setAppsGridSizeColumns(1 + seekBar.getProgress());
                previewView.setRowSize(seekBar.getProgress());
                previewView.setColumnSize(seekBar.getProgress()+1);
                previewView.invalidate();
                handler.resourceRequest(currentUser.getSettings(), new Response.Listener<Settings>() {
                    @Override
                    public void onResponse(Settings response) {
                        Log.i("Launcher", "Put user settings request success for SettingsLauncher");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handler.login(currentUser, new Response.Listener<Integer>() {
                            @Override
                            public void onResponse(Integer response) {
                                handler.resourceRequest(currentUser.getSettings(), new Response.Listener<Settings>() {
                                    @Override
                                    public void onResponse(Settings response) {
                                        Log.i("Launcher", "Put user settings request success for SettingsLauncher");
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("Launcher", "Put user request failed for SettingsLauncher");
                                    }
                                });
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Launcher", "Put user request failed for SettingsLauncher");
                            }
                        });
                    }
                });
            }
        });

        return view;
    }
}
