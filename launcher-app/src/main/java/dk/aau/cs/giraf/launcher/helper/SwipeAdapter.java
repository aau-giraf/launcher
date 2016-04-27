package dk.aau.cs.giraf.launcher.helper;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.librest.LoginProvider;

/**
 * Created by Caspar on 10-03-2016.
 */
public class SwipeAdapter extends PagerAdapter {
    private List<Integer> mImages;
    private List<String> mUsers;
    private LayoutInflater layoutInflater;
    private static int NUM_OF_PROFILES_PR_PAGE = 8;
    private Cursor cursor;

    private int[] imageFields = {R.id.image_view1,R.id.image_view2,R.id.image_view3,R.id.image_view4,R.id.image_view5,R.id.image_view6,R.id.image_view7, R.id.image_view8};
    private int[] textFields = {R.id.image_text1,R.id.image_text2,R.id.image_text3,R.id.image_text4,R.id.image_text5,R.id.image_text6,R.id.image_text7,R.id.image_text8};

    public SwipeAdapter(Context context, Cursor cursor){
        this.cursor = cursor;
        this.layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        mImages = new LinkedList<Integer>();
        mUsers = new LinkedList<String>();

        //Filling in test data should be removed
        for(int i = 0;i<64;i++){
            mImages.add(i,R.drawable.sample_3);
            mUsers.add(i,"Jeff");
        }
    }

    public void changeCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==(RelativeLayout)object);
    }

    @Override
    public int getCount() {
        if (cursor == null)
            return 0;

        int count = cursor.getCount();
        int ceil = (int) Math.ceil((double)count / NUM_OF_PROFILES_PR_PAGE);
        return ceil;
    }

    //TODO: Fix the infinite scrolling to the right
    //TODO: Fix that the excess imageViews are hidden.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View itemView = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        int nextProfile = position * NUM_OF_PROFILES_PR_PAGE;

        cursor.moveToPosition(nextProfile);

        for(int i = 0; i < 8; i++){
            if(nextProfile >= cursor.getCount()){
                break;
            }
            ImageView imageView = (ImageView)itemView.findViewById(imageFields[i]);
            TextView  textView = (TextView)itemView.findViewById(textFields[i]);

            imageView.setImageResource(mImages.get(nextProfile));
            int columnIndex = cursor.getColumnIndex(LoginProvider.USERNAME);
            textView.setText(cursor.getString(columnIndex));

            System.out.println("Next Profile number: " + nextProfile);
            nextProfile++;
            cursor.moveToNext();
        }

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout)object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
