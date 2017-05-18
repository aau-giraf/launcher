package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Marhlder on 18-02-15.
 */
public class AppsFragmentAdapter extends FragmentStatePagerAdapter {

    private List<AppInfo> appInfoList;
    private int rowSize;
    private int columnSize;
    private Context context;

    /**
     * Creates an AppsFragmentAdapter.
     * Based on a FragmentManager and how many apps the user can see.
     *
     * @param fm A FragmentManager
     * @param appInfoList A list of AppInfo
     * @param rowSize The amount of rows which the user can see
     * @param columnSize The amount of columns which the user can see
     */
    public AppsFragmentAdapter(Context context, final FragmentManager fm, final List<AppInfo> appInfoList,
                               final int rowSize, final int columnSize)
    {
        super(fm);
        this.context = context;
        this.appInfoList = appInfoList;
        this.rowSize = rowSize;
        this.columnSize = columnSize;
    }

    @Override
    public Fragment getItem(final int position) {
        final int from = position * rowSize * columnSize;
        final int to = ((position + 1) * rowSize * columnSize);

        if (to < appInfoList.size()) {
            return AppsGridFragment.newInstance(context,new ArrayList<AppInfo>(appInfoList.subList(from, to)),
                rowSize, columnSize);

        } else {
            return AppsGridFragment.newInstance(context,new ArrayList<AppInfo>(appInfoList.subList(from, appInfoList.size())),
                rowSize, columnSize);
        }
    }

    @Override
    public int getCount() {

        if (appInfoList != null) {
            return (int) Math.ceil(((double) appInfoList.size()) / (rowSize * columnSize));
        }

        return 0;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * Updates the AppFragmentAdapter with a list of new AppInfo, rows and columns.
     *
     * @param appInfoList A list of AppInfo
     * @param rowSize The amount of rows which the user can see
     * @param columnSize The amount of columns which the user can see
     */
    public void swapApps(final List<AppInfo> appInfoList, final int rowSize, final int columnSize) {

        this.appInfoList = appInfoList;
        this.rowSize = rowSize;
        this.columnSize = columnSize;

        //We only got this exception once, so we could not test it, as such we use try / catch
        try {
            this.notifyDataSetChanged();
        } catch (IllegalStateException e) {
            //ToDo Find out if the app can run if this happens
            Log.e("Launcher","Fragment is not in FragmentManager");
        }

    }
}
