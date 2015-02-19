package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.helper.AppViewCreationUtility;

/**
 * Created by Marhlder on 18-02-15.
 */
public class AppsGridFragment extends Fragment {

    private static final String ROW_SIZE_INT_TAG = "ROW_SIZE_INT_TAG";
    private static final String COLUMN_SIZE_INT_TAG = "COLUMN_SIZE_INT_TAG";
    private static final String APPINFOS_PARCELABLE_TAG = "APPINFOS_PARCELABLE_TAG";

    public static AppsGridFragment newInstance(final ArrayList<AppInfo> appInfos, final int rowSize, final int columnSize) {
        AppsGridFragment newFragment = new AppsGridFragment();

        Bundle args = new Bundle();
        args.putInt(ROW_SIZE_INT_TAG, rowSize);
        args.putInt(COLUMN_SIZE_INT_TAG, columnSize);
        args.putParcelableArrayList(APPINFOS_PARCELABLE_TAG, appInfos);
        newFragment.setArguments(args);

        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        GridLayout appsGridLayout = (GridLayout) inflater.inflate(R.layout.apps_grid_fragment, null);

        final Bundle arguments = getArguments();

        if (arguments != null)
        {
            final int rowSize = arguments.getInt(ROW_SIZE_INT_TAG);
            final int columnSize = arguments.getInt(COLUMN_SIZE_INT_TAG);
            final ArrayList<AppInfo> appInfos = arguments.getParcelableArrayList(APPINFOS_PARCELABLE_TAG);

            appsGridLayout.setRowCount(rowSize);
            appsGridLayout.setColumnCount(columnSize);

            for(int rowCounter = 0; rowCounter < rowSize; rowCounter++)
            {
                for(int columnCounter = 0; columnCounter < columnSize; rowCounter++)
                {
                    final AppInfo currentAppInfo = appInfos.get(columnCounter + rowCounter * rowSize);


                    final HomeActivity activity = (HomeActivity)getActivity();

                    //Create a new AppImageView and set its properties
                    AppImageView newAppView = AppViewCreationUtility.createAppImageView(getActivity(), activity.getCurrentUser(), activity.getLoggedInGuardian(), currentAppInfo, appsGridLayout, getOnClickListener());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params.setMargins(2, 2, 2, 2);
                    newAppView.setLayoutParams(params);
                    appsGridLayout.addView(newAppView, columnCounter + rowCounter * rowSize);

                }
            }
            //

        }

        return appsGridLayout;
    }

    protected View.OnClickListener getOnClickListener()
    {
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
