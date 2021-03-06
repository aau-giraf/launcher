package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * In order to easily handle the animation of the drawer, we extended the native RelativeLayout,
 * to handle the additional animations that the drawer needed.
 * The DrawerLayout class is currently not used, since the drawer itself was disabled
 */
public class DrawerLayout extends RelativeLayout {

    /**
     * A boolean variable to keep track of which way we should do the translateanimation
     * if true, translate right, else, translate left.
     */
    public boolean isSideBarHidden = true;

    /**
     * A constructor of the Layout.
     * @param context The context of the current Activity
     */
    public DrawerLayout(Context context) {
        super(context);
    }

    /**
     * A constructor of the Layout. Must be implemented when extending a RelativeLayout
     * @param context The context of the current Activity
     * @param attrs The attributes the layout should have
     */
    public DrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * A constructor of the Layout. Must be implemented when extending a RelativeLayout
     * @param context The context of the current Activity
     * @param attrs The attributes the layout should have
     * @param defStyle The style the layout should have
     */
    public DrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Describes what should happen when the translate animation of the layout begins.
     * Brings the layout to the front
     */
    @Override
    protected void onAnimationStart() {
        super.onAnimationStart();
        this.bringToFront();
    }
}
