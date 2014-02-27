package dk.aau.cs.giraf.launcher;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

	class ProfileLauncher extends Activity implements AdapterView.OnClickListener{
        @Override
        public void onClick(View v) {
            AppInfo app = HomeActivity.getAppInfo((String)v.getTag());
            
            Intent profileSelectIntent = new Intent(v.getContext(),ProfileSelectActivity.class);
            profileSelectIntent.putExtra(Constants.APP_PACKAGE_NAME, app.getaPackage());
            profileSelectIntent.putExtra(Constants.APP_ACTIVITY_NAME, app.getActivity());
            profileSelectIntent.putExtra(Constants.GUARDIAN_ID, app.getGuardianID());
            profileSelectIntent.putExtra(Constants.APP_COLOR, app.getBgColor());
            
			v.getContext().startActivity(profileSelectIntent);
    }  
}