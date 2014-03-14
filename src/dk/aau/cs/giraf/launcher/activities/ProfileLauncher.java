package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;

class ProfileLauncher extends Activity implements AdapterView.OnClickListener{
        @Override
        public void onClick(View v) {
            AppInfo app = HomeActivity.getAppInfo((String) v.getTag());
            Intent intent;

            // If user chose Admin/OasisApp do not open profile selector
            if (app.getActivity().toLowerCase().contains("oasis.app"))
            {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName(app.getaPackage(), app.getActivity()));
            }
            else
            {
                intent = new Intent(v.getContext(),ProfileSelectActivity.class);
            }

            intent.putExtra(Constants.APP_PACKAGE_NAME, app.getaPackage());
            intent.putExtra(Constants.APP_ACTIVITY_NAME, app.getActivity());
            intent.putExtra(Constants.GUARDIAN_ID, app.getGuardianID());
            intent.putExtra(Constants.APP_COLOR, app.getBgColor());

            // Verify the intent will resolve to at least one activity
            LauncherUtility.secureStartActivity(v.getContext(), intent);
    }  
}