package dk.aau.cs.giraf.launcher.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncStatusObserver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;

import java.io.IOException;
import java.net.URI;
import java.nio.BufferUnderflowException;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.SwipeAdapter;
import dk.aau.cs.giraf.librest.ContentProvider;
import dk.aau.cs.giraf.librest.LoginProvider;
import dk.aau.cs.giraf.librest.User;
import dk.aau.cs.giraf.librest.UserService;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by Caspar on 10-03-2016.
 */
public class ProfileChooserActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final Uri userURI = Uri.parse("content://dk.aau.cs.giraf.provider.Users/users");
    private static final String ACCOUNT = "sync";
    private static final String ACCOUNT_TYPE = "web.giraf.cs.aau.dk";
    private static final String AUTHORITY = "dk.aau.cs.giraf.provider.Users";


    ViewPager viewPager;
    SwipeAdapter adapter;

    Object handleSyncObserver;
    AccountManager accountManager;
    Account mAccount;
    
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

        accountManager = AccountManager.get(this);
        mAccount = CreateSyncAccount(this);
        //ContentResolver.requestSync();

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);

        Cursor c = getContentResolver().query(userURI, null, null, null, null);

        viewPager = (ViewPager)findViewById(R.id.viewPager);
        adapter = new SwipeAdapter(this, null);
        viewPager.setAdapter(adapter);

        getSupportLoaderManager().initLoader(-1, null, this);
    }

    public static Account CreateSyncAccount(Context context) {
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountmanager =
                (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        if (accountmanager.addAccountExplicitly(newAccount, null, null)){
            ContentResolver.setIsSyncable(newAccount, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);
        } else {
            Log.d(null, "Some error occurred with the account");
        }
        return newAccount;
    }

    @Override
    protected void onPause() {
        if(handleSyncObserver != null){
            getContentResolver().removeStatusChangeListener(handleSyncObserver);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleSyncObserver = ContentResolver.addStatusChangeListener(
                getContentResolver().SYNC_OBSERVER_TYPE_ACTIVE |
                getContentResolver().SYNC_OBSERVER_TYPE_PENDING, syncObserver);
    }

    //Use to update status depending on if the ContentProvider is Syncing
    private void refreshSyncStatus(){
        String status;
    }

    // TODO: Refactor
    final String[] PROJECTION = new String[] {
            LoginProvider._ID,
            LoginProvider.USERNAME
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, LoginProvider.CONTENT_URI, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i("UserLoad", "Users have been loaded");
        adapter.changeCursor(cursor);
        System.out.println("Cursor count: " +cursor.getCount());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }


}
