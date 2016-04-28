package dk.aau.cs.giraf.launcher.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.SwipeAdapter;
import dk.aau.cs.giraf.librest.accounts.AuthenticatorService;
import dk.aau.cs.giraf.librest.provider.GirafContract;
import dk.aau.cs.giraf.librest.provider.GirafProvider;

/**
 * Created by Caspar on 10-03-2016.
 */
public class ProfileChooserActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final Uri userURI = Uri.parse("content://dk.aau.cs.giraf.provider.Users/users");


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
        ContentResolver.requestSync(mAccount, GirafContract.CONTENT_AUTHORITY, settingsBundle);

        Cursor c = getContentResolver().query(userURI, null, null, null, null);

        viewPager = (ViewPager)findViewById(R.id.viewPager);
        adapter = new SwipeAdapter(this, null);
        viewPager.setAdapter(adapter);

        getSupportLoaderManager().initLoader(-1, null, this);
    }

    public static Account CreateSyncAccount(Context context) {
        Account account = AuthenticatorService.GetAccount();
        AccountManager accountmanager =
                (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        if (accountmanager.addAccountExplicitly(account, null, null)){
            ContentResolver.setIsSyncable(account, GirafContract.CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, GirafContract.CONTENT_AUTHORITY, true);
        } else {
            Log.d(null, "Some error occurred with the account");
        }
        return account;
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
            GirafProvider._ID,
            GirafProvider.USERNAME
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, GirafContract.CONTENT_URI, PROJECTION, null, null, null);
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
