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

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.LinkedList;
import java.util.List;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.AuthenticationActivity;
import dk.aau.cs.giraf.launcher.activities.ProfileChooserActivity;
import dk.aau.cs.giraf.librest.provider.GirafProvider;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by Caspar on 10-03-2016.
 */
public class SwipeAdapter extends PagerAdapter {
    private static final String PROFILE_TAG = "Swipe adapter";
    private List<Integer> mImages;
    private List<String> mUsers;
    private LayoutInflater layoutInflater;
    private static int NUM_OF_PROFILES_PR_PAGE = 10;
    private Cursor cursor;
    private Context mContext;
    /*
    * Fields used for defining the rounding of the corners of the profileimages
    */
    private final int radius = 5;
    private final int margin = 5;
    private Transformation transformation = new RoundedCornersTransformation(radius,margin);

    private int[] imageFields = {R.id.image_view1,R.id.image_view2,R.id.image_view3,R.id.image_view4,R.id.image_view5,R.id.image_view6,R.id.image_view7, R.id.image_view8,R.id.image_view9,R.id.image_view10};
    private int[] textFields = {R.id.image_text1,R.id.image_text2,R.id.image_text3,R.id.image_text4,R.id.image_text5,R.id.image_text6,R.id.image_text7, R.id.image_text8,R.id.image_text9,R.id.image_text10};
    private int[] profileObjects = {R.id.profile1, R.id.profile2, R.id.profile3, R.id.profile4, R.id.profile5, R.id.profile6, R.id.profile7, R.id.profile8, R.id.profile9, R.id.profile10};

    public SwipeAdapter(Context context, Cursor cursor){
        this.cursor = cursor;
        this.layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;

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

    //TODO: Fix that the excess imageViews are hidden.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View itemView = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        int nextProfile = position * NUM_OF_PROFILES_PR_PAGE;

        cursor.moveToPosition(nextProfile);

        for(int i = 0; i < 10; i++){
            if(nextProfile >= cursor.getCount()){
                break;
            }
            ImageView imageView = (ImageView)itemView.findViewById(imageFields[i]);
            final TextView  textView = (TextView)itemView.findViewById(textFields[i]);

            imageView.setImageResource(mImages.get(nextProfile));
            int columnIndex = cursor.getColumnIndex(GirafProvider.USERNAME);

            Picasso.with(mContext).load(mImages.get(nextProfile))
                    .fit()
                    .centerCrop()
                    .transform(transformation)
                    .placeholder(R.drawable.bg)
                    .into(imageView);

            textView.setText(cursor.getString(columnIndex));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AuthenticationActivity.class);
                    String userName = (String) textView.getText();
                    intent.putExtra("username", userName);
                    mContext.startActivity(intent);
                }
            });

            nextProfile++;
            cursor.moveToNext();
        }

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
