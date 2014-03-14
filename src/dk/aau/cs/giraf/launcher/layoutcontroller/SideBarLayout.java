package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import dk.aau.cs.giraf.launcher.R;

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
    protected void onAnimationStart() {
        super.onAnimationStart();
        this.bringToFront();
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();

        SideBarLayout.LayoutParams params = (SideBarLayout.LayoutParams) this.getLayoutParams();
        RelativeLayout homeDrawer = (RelativeLayout) findViewById(R.id.HomeDrawer);

        if (isSideBarHidden) {
            params.leftMargin = homeDrawer.getLeft();
            isSideBarHidden = false;
        } else {
            params.leftMargin -= homeDrawer.getMeasuredWidth();
            isSideBarHidden = true;
        }
        this.setLayoutParams(params);
    }
}
