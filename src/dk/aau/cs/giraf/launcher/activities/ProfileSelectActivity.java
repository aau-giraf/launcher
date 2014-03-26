package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GProfileAdapter;
import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Department;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ProfileSelectActivity extends Activity {

	private List<Profile> mChildren;
    private Helper mHelper;
	private Context mContext;
	private long mGuardianID;
	private String mPackageName;
	private String mActivityName;
    private long mAppId;
    private int mAppColor;
    private boolean shouldReturnResult;
    private ListView listView;

	/**
     * Called when the activity is first created.
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profileselect);
        listView = (ListView)findViewById(R.id.profilesList);
		mContext = this;
        mPackageName = Constants.APP_PACKAGE_NAME;

        mHelper = new Helper(mContext);
        Bundle extras = getIntent().getExtras();
        mGuardianID = extras.getLong(Constants.GUARDIAN_ID);
        mAppColor = extras.getInt(Constants.APP_COLOR);
        mPackageName = extras.getString(Constants.APP_PACKAGE_NAME);
        mActivityName = extras.getString(Constants.APP_ACTIVITY_NAME);
        mAppId = extras.getLong(Constants.APP_ID);
        loadProfiles();

        // Start logging this activity
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Retrive the screen dimensions
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.height = WRAP_CONTENT;
        params.width = screenSize.x / 2; // We only want to use half of the screen
        getWindow().setAttributes(params);
    }

    @Override
    public void finish() {
        super.finish();

        // Start logging this activity
        EasyTracker.getInstance(this).activityStop(this);
    }

	/**
	 * Finds children attached to the guardian or the institution, 
	 * and creates the list used to select which child to run an app with. 
	 */
	private void loadProfiles() {
		Profile.setOutput("{1} {2} {3}");
		
		mChildren = new ArrayList<Profile>();

		Profile guardianProfile = mHelper.profilesHelper.getProfileById(mGuardianID);
		
		List<Profile> guardianChildren = mHelper.profilesHelper.getChildrenByGuardian(guardianProfile);
		
		List<Department> guardianDepartments = mHelper.departmentsHelper.getDepartmentsByProfile(guardianProfile);
		
		List<Profile> totalChildren = new ArrayList<Profile>();
		totalChildren.addAll(guardianChildren);
		
		for (Department department : guardianDepartments) {
			List<Profile> childrenFromDepartments = mHelper.profilesHelper.getChildrenByDepartmentAndSubDepartments(department);
			
			totalChildren.addAll(childrenFromDepartments);
		}
		
		// Removing duplicate profiles
		outerloop:
		for (Profile child : totalChildren) {
			for (Profile mChild : mChildren) {
				if (mChild.getId() == child.getId()) {
					continue outerloop;
				}
			}
			mChildren.add(child);
		}
		
		GProfileAdapter childAdapter = new GProfileAdapter(this, mChildren);
		ListView listOfChildren = (ListView) findViewById(R.id.profilesList);
		listOfChildren.setAdapter(childAdapter);
 
		// When a child is selected, launch the app that was chosen with the correct data in the extras.
		listOfChildren.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get selected child id
                final long childID = ((Profile) parent.getAdapter().getItem(position)).getId();
                startSelectedApplication(childID);
			}
		});
	}

    /**
     * This is used when this activity has been started with startIntent()
     * @param childID
     */
    private void startSelectedApplication(final long childID) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(mPackageName, mActivityName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        intent.putExtra(Constants.CHILD_ID, childID);
        intent.putExtra(Constants.GUARDIAN_ID, mGuardianID);
        intent.putExtra(Constants.APP_COLOR, mAppColor);
        intent.putExtra(Constants.APP_ID, mAppId);
        // Verify the intent will resolve to at least one activity
        LauncherUtility.secureStartActivity(this, intent);
    }
}