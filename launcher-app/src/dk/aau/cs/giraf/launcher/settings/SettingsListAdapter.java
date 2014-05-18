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

/**
 * Custom adapter to ListView.
 * The ListView is the one on the left side of SettingsActivity.
 * Because this ListView does a number of different things, depending on the contents of each SettingsListItem,
 * This adapter needs to handle many cases.
 * Furthermore, it also handles the shadowing and selecting animations, giving the user visual feedback from actions.
 * @see SettingsListFragment
 */
public class SettingsListAdapter extends BaseAdapter {

    /**
     * The variables needed by the adapter
     */
    private Activity mActivity;
    private LayoutInflater mInflater;

    // The list containing the items to add
    private ArrayList<SettingsListItem> mApplicationList;
    // Reference to the ListView using the adapter
    private ListView mListView;
    // Ensures the first item in the associated ListView is selected at first startup
    private static int mLastSelectedItem = 0;

    /**
     * The constructor for the class
     * @param activity The activity that contains the ListView
     * @param listView The ListView to be inflated
     * @param list the list of SettingsListItems that should populate the ListView.
     */
    public SettingsListAdapter(Activity activity, ListView listView, ArrayList<SettingsListItem> list) {
        mActivity = activity;
        mApplicationList = list;
        mListView = listView;

        // Get the layout inflater from the activity
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * This function returns the amount of applications in the list of applications.
     * @return The amount of applications in the list of applications
     */
    public int getCount() {
        return mApplicationList.size();
    }

    /**
     * Gets the item at a certain position.
     * @param position the position of the item
     * @return The item at the given position.
     */
    public Object getItem(int position) {
        return mApplicationList.get(position);
    }

    /**
     * returns the position of the item.
     * @param position the position of the item
     * @return the position of the item
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Used to set the last selected item in the adapter.
     * Can be used when switching fragment in the ListView and the last selected item
     * should be marked with seperate backgroundcolor and shadow.
     * @param position Position in the ListView of the current item.
     */
    public void setSelected(int position) {
        // Position in the view implementing the adapter
        mLastSelectedItem = position;
        // Notify adapter that a new item is selected to redraw views
        notifyDataSetChanged();
    }

    /**
     * Method that must be implemented by extending the BaseAdapter class.
     * Returns a view to be used as a row in a ListView.
     * @param position Position in the ListView of the current item.
     * @param convertView The view to be inflated containing the information to be shown in the ListView.
     * @param parent Parent view of the convertView (ListView).
     * @return The inflated view to be used in a ListView.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        /* Implementing the ViewHolder pattern to minimize the use of findViewById
        *  The views are set once for each convertView so we don't need to find them again */
        ViewHolder holder;

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.settings_fragment_list_row, null);

            // Filling the ViewHolder with views used to render a list item
            holder = new ViewHolder();
            holder.appIcon = (ImageView)convertView.findViewById(R.id.settingsListAppLogo);
            holder.appName = (TextView)convertView.findViewById(R.id.settingsListAppName);
            holder.shadowTop = convertView.findViewById(R.id.settingsListRowShadowBelow);
            holder.shadowBottom = convertView.findViewById(R.id.settingsListRowShadowAbove);
            holder.shadowRight = convertView.findViewById(R.id.settingsListRowShadowRight);

            // Tag the view with the holder object to be able to retrieve it again
            convertView.setTag(holder);
        }
        else
            // The view has been tagged with a ViewHolder, so we can just retrieve it
            // to avoid finding them again
            holder = (ViewHolder)convertView.getTag();

        // Get the item we are rendering the view for in the list
        SettingsListItem item = mApplicationList.get(position);

        // Set the visibility of the right shadow of current view
        if (position == mLastSelectedItem) {
            setShadowVisibility(holder.shadowRight, false);
            // Set the selected item in the ListView coming from the constructor
            // Minimizes the use of findViewById
            mListView.setItemChecked(position, true);
        }
        else
            setShadowVisibility(holder.shadowRight, true);

        // Set the visibility of bottom shadow in the view above
        if (position == mLastSelectedItem - 1)
            setShadowVisibility(holder.shadowBottom, true);
        else
            setShadowVisibility(holder.shadowBottom, false);

        // Set the visibility of top shadow in the view below
        if (position == mLastSelectedItem + 1)
            setShadowVisibility(holder.shadowTop, true);
        else
            setShadowVisibility(holder.shadowTop, false);

        // Setting all values in ListView
        holder.appIcon.setBackgroundDrawable(item.mAppIcon);
        holder.appName.setText(item.mAppName);

        return convertView;
    }

    /**
     * Set the visibility of the shadows according to list selection.
     * @param shadowView The view to change visibility of.
     * @param visible Boolean value reflecting if it should be visible or not.
     * @see SettingsListAdapter
     */
    private void setShadowVisibility(View shadowView, boolean visible) {
        if (visible)
            shadowView.setVisibility(View.VISIBLE);
        else
            shadowView.setVisibility(View.GONE);
    }

    /**
     * Class used in the adapter to hold a reference to each view in a list item (row)
     */
    private static class ViewHolder {
        public ImageView appIcon;
        public TextView appName;
        public View shadowTop;
        public View shadowBottom;
        public View shadowRight;
    }
}