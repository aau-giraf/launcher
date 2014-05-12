package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
    private ListView mListView;
    private static LayoutInflater mInflater = null;
    private static int mLastSelectedItem = 0;

    public SettingsListAdapter(Activity activity, ArrayList<SettingsListItem> list) {
        mActivity = activity;
        mApplicationList = list;
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
        ViewHolder holder;

        if (mListView == null)
            mListView = (ListView) parent.findViewById(R.id.settingsListView);

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.settings_fragment_list_row, null);

            holder = new ViewHolder();
            holder.appIcon = (ImageView)convertView.findViewById(R.id.settingsListAppLogo);
            holder.appName = (TextView)convertView.findViewById(R.id.settingsListAppName);
            holder.shadowTop = convertView.findViewById(R.id.settingsListRowShadowBelow);
            holder.shadowBottom = convertView.findViewById(R.id.settingsListRowShadowAbove);
            holder.shadowRight = convertView.findViewById(R.id.settingsListRowShadowRight);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder)convertView.getTag();

        // Get current item in the list
        SettingsListItem item = mApplicationList.get(position);

        if (position == mLastSelectedItem) {
            setShadowVisibility(holder.shadowRight, false);
            mListView.setItemChecked(position, true);
        }
        else
            setShadowVisibility(holder.shadowRight, true);

        if (position == mLastSelectedItem - 1)
            setShadowVisibility(holder.shadowBottom, true);
        else
            setShadowVisibility(holder.shadowBottom, false);

        if (position == mLastSelectedItem + 1)
            setShadowVisibility(holder.shadowTop, true);
        else
            setShadowVisibility(holder.shadowTop, false);

        // Setting all values in ListView
        holder.appIcon.setBackgroundDrawable(item.mAppIcon);
        holder.appName.setText(item.mAppName);

        return convertView;
    }

    private void setShadowVisibility(View shadowView, boolean visible) {
        if (visible)
            shadowView.setVisibility(View.VISIBLE);
        else
            shadowView.setVisibility(View.GONE);
    }

    private static class ViewHolder {
        public ImageView appIcon;
        public TextView appName;
        public View shadowTop;
        public View shadowBottom;
        public View shadowRight;
    }
}