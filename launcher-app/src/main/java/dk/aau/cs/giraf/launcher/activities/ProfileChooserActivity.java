package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.SwipeAdapter;
import dk.aau.cs.giraf.librest.ContentProvider;
import dk.aau.cs.giraf.librest.User;

/**
 * Created by Caspar on 10-03-2016.
 */
public class ProfileChooserActivity extends Activity {
    private static final Uri userURI = Uri.parse("http://web.giraf.cs.aau.dk:8080/giraf-rest_service-1.0.0/user");

    ViewPager viewPager;
    SwipeAdapter adapter;

    Object handleSyncObserver;

    SyncStatusObserver syncObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshSyncStatus();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_chooser);

        Cursor c = getContentResolver().query(userURI,null,null,null,null,null);
        List<User> users = new LinkedList<User>();
        while(c.moveToNext()){
            users.add(User.fromCursor(c));
        }
                viewPager = (ViewPager)findViewById(R.id.viewPager);
        adapter = new SwipeAdapter(this);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        if(handleSyncObserver != null){
            ContentResolver.removeStatusChangeListener(handleSyncObserver);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleSyncObserver = ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE |
                ContentResolver.SYNC_OBSERVER_TYPE_PENDING, syncObserver);
    }

    //Use to update status depending on if the ContentProvider is Syncing
    private void refreshSyncStatus(){
        String status;
        }
}
