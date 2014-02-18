package dk.aau.cs.giraf.launcher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profileselect);

		mContext = getApplicationContext();
		
		mPackageName = getIntent().getExtras().getString(Data.APP_PACKAGENAME);
		mActivityName = getIntent().getExtras().getString(Data.APP_ACTIVITYNAME);
		mAppColor = getIntent().getExtras().getInt(Data.APP_COLOR);
		
		mGuardianID = getIntent().getExtras().getLong(Data.GUARDIANID);

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
				final long childID = ((Profile) parent.getAdapter().getItem(position)).getId();

				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setComponent(new ComponentName(mPackageName, mActivityName));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

				intent.putExtra(Data.CHILDID, childID);
				intent.putExtra(Data.GUARDIANID, mGuardianID);
				intent.putExtra(Data.APP_COLOR, mAppColor);

				startActivity(intent);
			}
		});
	}
}