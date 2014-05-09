package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import dk.aau.cs.giraf.launcher.R;

import java.util.ArrayList;

public class SettingsListAdapter extends BaseAdapter {

    private Activity mActivity;
    private ArrayList<SettingsListItem> mApplicationList;
    private static LayoutInflater mInflater = null;
    private static int mLastSelectedItem = 0;

    public SettingsListAdapter(Activity a, ArrayList<SettingsListItem> l) {
        mActivity = a;
        mApplicationList = l;
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mApplicationList.size();
    }

    public Object getItem(int position) {
        return mApplicationList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void setSelected(int position) {
        mLastSelectedItem = position;
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if(convertView == null)
            vi = mInflater.inflate(R.layout.settings_fragment_list_row, null);

        // Get current item in the list
        SettingsListItem item = mApplicationList.get(position);

        if (position == mLastSelectedItem) {
            setShadowRightCurrent(vi, false);
            ListView listView = (ListView)parent.findViewById(R.id.settingsListView);
            listView.setItemChecked(position, true);
        }
        else {
            setShadowRightCurrent(vi, true);
        }

        if (position == mLastSelectedItem - 1)
            setShadowAboveCurrent(vi, true);
        else
            setShadowAboveCurrent(vi, false);

        if (position == mLastSelectedItem + 1)
            setShadowBelowCurrent(vi, true);
        else
            setShadowBelowCurrent(vi, false);

        ImageView appIcon = (ImageView)vi.findViewById(R.id.settingsListAppLogo);
        TextView appNameText = (TextView)vi.findViewById(R.id.settingsListAppName);

        // Setting all values in ListView
        appIcon.setBackgroundDrawable(item.mAppIcon);
        appNameText.setText(item.mAppName);

        return vi;
    }

    private void setShadowRightCurrent(View currentView, boolean visible) {
        View rightShadow = currentView.findViewById(R.id.settingsListRowShadowRight);
        if (visible)
            rightShadow.setVisibility(View.VISIBLE);
        else
            rightShadow.setVisibility(View.GONE);
    }

    private void setShadowAboveCurrent(View aboveView, boolean visible) {
        // Add a shadow at the bottom of the list item ABOVE current
        View aboveViewShadow = aboveView.findViewById(R.id.settingsListRowShadowAbove);
        if (visible)
            aboveViewShadow.setVisibility(View.VISIBLE);
        else
            aboveViewShadow.setVisibility(View.GONE);
    }

    private void setShadowBelowCurrent(View belowView, boolean visible) {
        // Add a shadow at the top of the list item BELOW current
        View belowViewShadow = belowView.findViewById(R.id.settingsListRowShadowBelow);
        if (visible)
            belowViewShadow.setVisibility(View.VISIBLE);
        else
            belowViewShadow.setVisibility(View.GONE);
    }
}