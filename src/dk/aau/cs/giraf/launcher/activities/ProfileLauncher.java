package dk.aau.cs.giraf.launcher.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;

public class ProfileLauncher extends Activity implements AdapterView.OnClickListener {
    @Override
    public void onClick(View v) {
        AppInfo app = HomeActivity.getAppInfo((String) v.getTag());
        Intent intent;

        // If user chose Admin/OasisApp do not open profile selector
        try{
            if (app.getActivity().toLowerCase().contains("oasis.app")) {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName(app.getPackage(), app.getActivity()));
            } else {
                intent = new Intent(v.getContext(), ProfileSelectActivity.class);
            }


            intent.putExtra(Constants.APP_PACKAGE_NAME, app.getPackage());
            intent.putExtra(Constants.APP_ACTIVITY_NAME, app.getActivity());
            intent.putExtra(Constants.GUARDIAN_ID_KEY, app.getGuardianID());
            intent.putExtra(Constants.APP_COLOR, app.getBgColor());
            intent.putExtra(Constants.APP_ID, app.getId());
            // Verify the intent will resolve to at least one activity
            LauncherUtility.secureStartActivity(v.getContext(), intent);
        }
        catch (NullPointerException e){ // App is not a Giraf app, get resolveInfo for the correct app and launch it
            List<ResolveInfo> apps = LauncherUtility.getDeviceApps(v.getContext());
            for(ResolveInfo realApp : apps){
                ActivityInfo activityInfo = realApp.activityInfo;
                if (activityInfo.name.equals(app.getPackage())){
                    ComponentName componentName = new ComponentName(activityInfo.packageName, activityInfo.name);

                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.setComponent(componentName);
                    // Verify the intent will resolve to at least one activity
                    LauncherUtility.secureStartActivity(v.getContext(), intent);
                    break;
                }
            }

        }
    }

    private boolean appIsGuardianOnly (String packageName) {
        for (String name : Constants.GUARDIAN_ONLY_APPS) {
            if (packageName.toLowerCase().contains(name)) {
                return true;
            }
        }

        return false;
    }
}