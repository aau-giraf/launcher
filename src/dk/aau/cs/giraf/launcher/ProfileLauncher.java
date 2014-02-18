package dk.aau.cs.giraf.launcher;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

	class ProfileLauncher extends Activity implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            AppInfo app = (AppInfo) parent.getItemAtPosition(position);
            
            Intent profileSelectIntent = new Intent(v.getContext(),ProfileSelectActivity.class);
            profileSelectIntent.putExtra(Data.APP_PACKAGENAME, app.getaPackage());
            profileSelectIntent.putExtra(Data.APP_ACTIVITYNAME, app.getActivity());
            profileSelectIntent.putExtra(Data.GUARDIANID, app.getGuardianID());
            profileSelectIntent.putExtra(Data.APP_COLOR, app.getBgColor());
            
			v.getContext().startActivity(profileSelectIntent);
    }  
}