package dk.aau.cs.giraf.launcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;

import java.util.zip.Inflater;

/**
 * Created by caspar on 3/9/16.
 */
public class ShowProfiles {
    private Context mContext;
    private String[] mUsers;
    private int[] mImages;
    private GridLayout mLayout;
    public static LayoutInflater inflater;

    public ShowProfiles(Context ctx, String[] users, int[] images, GridLayout layout){
        mContext = ctx;
        mUsers = users;
        mImages = images;
        this.mLayout = layout;
    }

    public View updateGrid(){
        View view;
        view = inflater.inflate(R.layout.grid_item,null);
        for(int i=1; i<7; i++){

        }
        return view;
    }
}
