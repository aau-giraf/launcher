package dk.aau.cs.giraf.launcher.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.Comparator;

import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;

/**
 * Created by Frederik on 07-05-14.
 */
public class AppComparator implements Comparator<Object> {
    private Context context;

    public AppComparator(Context context){
        this.context = context;
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        int res = 0;
        if (lhs instanceof AppInfo && rhs instanceof AppInfo)
            res = ((AppInfo)lhs).getName().compareToIgnoreCase(((AppInfo) rhs).getName());
        else if (lhs instanceof ResolveInfo && rhs instanceof ResolveInfo){
            try {
                PackageManager packageManager = context.getPackageManager();
                String lhsName = ((ResolveInfo)lhs).activityInfo.loadLabel(packageManager).toString();
                String rhsName = ((ResolveInfo)rhs).activityInfo.loadLabel(packageManager).toString();
                res = lhsName.compareToIgnoreCase(rhsName);
            } catch (NullPointerException e){
                res = 0;
            }
        }
        return res;
    }
}
