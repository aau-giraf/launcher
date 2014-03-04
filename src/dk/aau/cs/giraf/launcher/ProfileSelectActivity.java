package dk.aau.cs.giraf.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GProfileAdapter;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Department;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public class ProfileSelectActivity extends Activity {

	private List<Profile> mChildren;
	private Context mContext;
	private long mGuardianID;
	private String mPackageName;
	private String mActivityName;
	private int mAppColor;
    private boolean shouldReturnResult;

	/**
     * Called when the activity is first created.
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profileselect);

		mContext = getApplicationContext();

        // determine whether to return result or not
        shouldReturnResult = callingActivityIsExpectingResult();

        // Only guardian id is required if expecting result returned
		mGuardianID = getIntent().getExtras().getLong(Constants.GUARDIAN_ID);

        /* If we should not return a result, it means that we should start a new activity when a profile
         * has been selected. If this activity was started for result it PackageName and ActivityName
         * has not been provided
         */
        if (!shouldReturnResult){
            mAppColor = getIntent().getExtras().getInt(Constants.APP_COLOR);
            mPackageName = getIntent().getExtras().getString(Constants.APP_PACKAGE_NAME);
            mActivityName = getIntent().getExtras().getString(Constants.APP_ACTIVITY_NAME);
        }

		loadProfiles();
	}

	/**
	 * Finds children attached to the guardian or the institution, 
	 * and creates the list used to select which child to run an app with. 
	 */
	private void loadProfiles() {
		Helper helper = new Helper(mContext);
		Profile.setOutput("{1} {2} {3}");
		
		mChildren = new ArrayList<Profile>();

		Profile guardianProfile = helper.profilesHelper.getProfileById(mGuardianID);
		
		List<Profile> guardianChildren = helper.profilesHelper.getChildrenByGuardian(guardianProfile);
		
		List<Department> guardianDepartments = helper.departmentsHelper.getDepartmentsByProfile(guardianProfile);
		
		List<Profile> totalChildren = new ArrayList<Profile>();
		totalChildren.addAll(guardianChildren);
		
		for (Department department : guardianDepartments) {
			List<Profile> childrenFromDepartments = helper.profilesHelper.getChildrenByDepartmentAndSubDepartments(department);
			
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

                if (shouldReturnResult){
                    returnSelectedProfile(childID);
                    finish();
                } else {
                    startSelectedApplication(childID);
                }

			}
		});
	}

    /**
     * This function is called when a profile has been selected and it should be returned to the
     * calling activity.
     * @param childID
     */
    private void returnSelectedProfile(final long childID){
        Intent intentResult = new Intent();
        intentResult.putExtra(Constants.CHILD_ID, childID);
        setResult(RESULT_OK, intentResult);
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

        startActivity(intent);
    }

    /**
     * Determine whether the calling activity is expecting a result.
     * Null means that the this activity was started with startIntent and is not expecting a return.
     * @return Whether  calling activity expects result.
     */
    private boolean callingActivityIsExpectingResult(){
        if (getCallingActivity() == null){
            return false;
        }

        return true;
    }

}