package dk.aau.cs.giraf.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

/**
 * Created by Vagner on 28-02-14.
 */
public class SideBarLayout extends RelativeLayout {

    public boolean isSideBarHidden = true;

    public SideBarLayout(Context context)
    {
        super(context);
    }
    public SideBarLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public SideBarLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();

        SideBarLayout.LayoutParams params = (SideBarLayout.LayoutParams) this.getLayoutParams();

        if (isSideBarHidden) {
            params.leftMargin = 0;
            isSideBarHidden = false;
        } else {
            params.leftMargin = -Constants.DRAWER_WIDTH;
            isSideBarHidden = true;
        }
        this.setLayoutParams(params);
    }
}
