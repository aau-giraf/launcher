package dk.aau.cs.giraf.launcher.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;

import java.util.Comparator;

/**
 * Facilitates the comparing of two apps, either of the {@link dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo}
 * type, or the {@link android.content.pm.ResolveInfo} type. The compared objects must be of the same type.
 * The comparison is on the app name.
 */
public class AppComparator implements Comparator<Object> {
    private final Context context;

    /**
     * The constructor for the class.
     *
     * @param context The context of the current activity.
     */
    public AppComparator(Context context) {
        this.context = context;
    }

    /**
     * Compares two apps, either of the {@link dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo}
     * type, or the {@link android.content.pm.ResolveInfo} type. The compared objects must be of the same type.
     * The comparison is on the app name.
     *
     * @param lhs The first object to compare.
     * @param rhs The second object to compare.
     * @return An integer indicating the relationship between the two objects. Is 0 if the objects are equal.
     */
    @Override
    public int compare(Object lhs, Object rhs) {
        int res = 0;
        if (lhs instanceof AppInfo && rhs instanceof AppInfo) {
            //Display unavailable apps last, i.e. when offline mode is enabled
            if (((AppInfo) lhs).getPackage().isEmpty() && !((AppInfo) rhs).getPackage().isEmpty()) {
                res = 1;
            } else if (((AppInfo) rhs).getPackage().isEmpty() && !((AppInfo) lhs).getPackage().isEmpty()) {
                res = -1;
            } else {
                res = ((AppInfo) lhs).getName().compareToIgnoreCase(((AppInfo) rhs).getName());
            }
        } else if (lhs instanceof ResolveInfo && rhs instanceof ResolveInfo) {
            PackageManager packageManager = context.getPackageManager();
            if(packageManager != null) {
                String lhsName = ((ResolveInfo) lhs).activityInfo.loadLabel(packageManager).toString();
                String rhsName = ((ResolveInfo) rhs).activityInfo.loadLabel(packageManager).toString();
                if(lhsName != null && rhsName != null) {
                    res = lhsName.compareToIgnoreCase(rhsName);
                }
                else{
                    res = 0;
                }
            }
            else {
                res = 0;
            }
        } else {
            Log.e(Constants.ERROR_TAG, "Comparison of incompatible app types.");
        }
        return res;
    }
}
