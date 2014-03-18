package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class ProfileSelectActivity extends Activity {

	private List<Profile> mChildren;
    private Helper mHelper;
	private Context mContext;
	private long mGuardianID;
	private String mPackageName;
	private String mActivityName;
	private int mAppColor;
    private boolean shouldReturnResult;
    private ListView listView;

    private long childID;

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

        // Only guardian id is required if expecting result returned
        mHelper = new Helper(mContext);
		mGuardianID = getIntent().getExtras().getLong(Constants.GUARDIAN_ID);
        mAppColor = getIntent().getExtras().getInt(Constants.APP_COLOR);
        mPackageName = getIntent().getExtras().getString(Constants.APP_PACKAGE_NAME);
        mActivityName = getIntent().getExtras().getString(Constants.APP_ACTIVITY_NAME);
		loadProfiles();

        // Start logging this activity
        EasyTracker.getInstance(this).activityStart(this);
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
        // Verify the intent will resolve to at least one activity
        LauncherUtility.secureStartActivity(this, intent);
    }
}