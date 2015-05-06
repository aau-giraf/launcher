package dk.aau.cs.giraf.launcher.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.Comparator;

import dk.aau.cs.giraf.launcher.layoutcontroller.AppInfo;

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
            res = ((AppInfo) lhs).getName().compareToIgnoreCase(((AppInfo) rhs).getName());
        } else if (lhs instanceof ResolveInfo && rhs instanceof ResolveInfo) {
            try {
                PackageManager packageManager = context.getPackageManager();
                String lhsName = ((ResolveInfo) lhs).activityInfo.loadLabel(packageManager).toString();
                String rhsName = ((ResolveInfo) rhs).activityInfo.loadLabel(packageManager).toString();
                res = lhsName.compareToIgnoreCase(rhsName);
            } catch (NullPointerException e) {
                res = 0;
            }
        } else {
            Log.e(Constants.ERROR_TAG, "Comparison of incompatible app types.");
        }
        return res;
    }
}
