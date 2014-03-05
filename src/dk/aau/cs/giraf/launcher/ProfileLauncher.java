package dk.aau.cs.giraf.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import dk.aau.cs.giraf.oasis.lib.models.Profile;

class ProfileLauncher extends Activity implements AdapterView.OnClickListener{
        @Override
        public void onClick(View v) {
            AppInfo app = HomeActivity.getAppInfo((String)v.getTag());
            Intent intent;

            intent = new Intent(v.getContext(),ProfileSelectActivity.class);
            intent.putExtra(Constants.APP_PACKAGE_NAME, app.getaPackage());
            intent.putExtra(Constants.APP_ACTIVITY_NAME, app.getActivity());
            intent.putExtra(Constants.GUARDIAN_ID, app.getGuardianID());
            intent.putExtra(Constants.APP_COLOR, app.getBgColor());
            
			v.getContext().startActivity(intent);
    }  
}