package dk.aau.cs.giraf.launcher.helper;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Caspar on 10-03-2016.
 */
public class SwipeAdapter extends PagerAdapter {
    private List<Integer> mImages;
    private List<String> mUsers;
    private LayoutInflater layoutInflater;
    private Context mContext;
    private static int NUM_OF_PROFILES_PR_PAGE = 8;

    private int[] imageFields = {R.id.image_view1,R.id.image_view2,R.id.image_view3,R.id.image_view4,R.id.image_view5,R.id.image_view6,R.id.image_view7, R.id.image_view8};
    private int[] textFields = {R.id.image_text1,R.id.image_text2,R.id.image_text3,R.id.image_text4,R.id.image_text5,R.id.image_text6,R.id.image_text7,R.id.image_text8};

    public SwipeAdapter(Context ctx){
        this.mContext = ctx;
        mImages = new LinkedList<Integer>();
        mUsers = new LinkedList<String>();

        //Filling in test data should be removed
        for(int i = 0;i<64;i++){
            mImages.add(i,R.drawable.sample_3);
            mUsers.add(i,"Jeff");
        }
    }
    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==(RelativeLayout)object);
    }

    //TODO: Fix the infinite scrolling to the right
    //TODO: Fix that the excess imageViews are hidden.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.swipe_layout,container,false);
        int nextProfile = position * NUM_OF_PROFILES_PR_PAGE;
        for(int i=0;i<8; i++){
            if(nextProfile >= mUsers.size()-1){
                break;
            }
            ImageView imageView = (ImageView)itemView.findViewById(imageFields[i]);
            TextView  textView = (TextView)itemView.findViewById(textFields[i]);

            imageView.setImageResource(mImages.get(nextProfile));
            textView.setText(mUsers.get(nextProfile));

            System.out.println("Next Profile number:" + nextProfile);
            nextProfile++;
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout)object);
    }
}
