package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marhlder on 18-02-15.
 */
public class AppsFragmentAdapter extends FragmentStatePagerAdapter {

    private List<AppInfo> appInfoList;
    private int rowSize;
    private int coloumnSize;


    public AppsFragmentAdapter(final FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(final int position) {
        return AppsGridFragment.newInstance(new ArrayList<AppInfo>(appInfoList.subList(position * rowSize * coloumnSize, ((position + 1) * rowSize * coloumnSize) - 1)), rowSize, coloumnSize);
    }

    @Override
    public int getCount() {

        if (appInfoList != null)
        {
            return (int) Math.ceil(appInfoList.size() / (rowSize * coloumnSize));
        }

        return 0;
    }

    public void swapApps(final List<AppInfo> appInfoList, final int rowSize, final int coloumnSize) {

        this.appInfoList = appInfoList;
        this.rowSize = rowSize;
        this.coloumnSize = coloumnSize;

        this.notifyDataSetChanged();
    }


}
