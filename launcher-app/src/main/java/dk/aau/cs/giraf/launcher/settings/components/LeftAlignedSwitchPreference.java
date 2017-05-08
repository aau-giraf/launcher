package dk.aau.cs.giraf.launcher.settings.components;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

/**
 * Created by Marhlder on 25-02-2015.
 */
public class LeftAlignedSwitchPreference extends Switch {


    public LeftAlignedSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LeftAlignedSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LeftAlignedSwitchPreference(Context context) {
        super(context);
    }


    /*
    @Override
    protected View onCreateView(ViewGroup parent) {
        final LinearLayout view = (LinearLayout) super.onCreateView(parent);
    */

        /**
         * There is a hidden linear layout with an ImageView as its first child, the layout of this linearlayout
         * messes up the alignment of the preference title
         * */
    /*
        view.getChildAt(0).setVisibility(View.GONE);

        return view;
    }
    */
}
