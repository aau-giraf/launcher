package dk.aau.cs.giraf.launcher.settings;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dk.aau.cs.giraf.launcher.R;

import java.util.ArrayList;

public class SettingsListAdapter extends BaseAdapter {

    private Activity mActivity;
    private ArrayList<SettingsListItem> mApplicationList;
    private int mSelectedItem;
    private static LayoutInflater mInflater = null;

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

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if(convertView == null)
            vi = mInflater.inflate(R.layout.settings_fragment_list_row, null);

        // Get current item in the list
        SettingsListItem item = mApplicationList.get(position);

        if (position == mSelectedItem) {
            // Do something to selected item here

        }

        ImageView appIcon = (ImageView)vi.findViewById(R.id.settingsListAppLogo);
        TextView appNameText = (TextView)vi.findViewById(R.id.settingsListAppName);

        // Setting all values in ListView
        appIcon.setBackgroundDrawable(item.appIcon);
        appNameText.setText(item.appName);

        return vi;
    }
}