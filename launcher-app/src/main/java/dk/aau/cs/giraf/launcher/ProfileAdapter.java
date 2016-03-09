package dk.aau.cs.giraf.launcher;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ckucha13 on 08-03-2016.
 */
public class ProfileAdapter extends BaseAdapter {
    private Context mContext;
    private String[] mUsers;
    private int[] mImages;
    private static LayoutInflater inflater;

    public ProfileAdapter(Context ctx, String[] users, int[] images)
    {
        this.mContext = ctx;
        this.mImages = images;
        this.mUsers = users;
        inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class Holder {
        TextView textView;
        ImageView imageView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.grid_item, null);
        holder.textView = (TextView) rowView.findViewById(R.id.grid_item_text);
        holder.imageView = (ImageView) rowView.findViewById(R.id.grid_item_image);

        holder.textView.setText(mUsers[position]);
        holder.imageView.setImageResource(mImages[position]);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "You clicked" + mUsers[position], Toast.LENGTH_LONG).show();
            }
        });
        return rowView;
    }
}
