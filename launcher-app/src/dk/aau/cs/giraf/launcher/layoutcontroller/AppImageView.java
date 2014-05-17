package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import dk.aau.cs.giraf.launcher.R;

/**
 * When apps are created as a view and filled into a targetlayout,
 * this is the type they are made of. The class adds a few methods to the native ImageView
 */
public class AppImageView extends ImageView {
    private boolean checked = false;
    private Context context;

    /**
     * A constructor for the class
     * @param context The context of the current activity
     */
    public AppImageView(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * A constructor for the class
     * This constructor is not used, but must be implemented in order to override the ImageView class
     * @param context The context of the current activity
     * @param attrs The set of attributes added to the AppImageView
     */
    public AppImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    /**
     * A constructor for the class
     * This constructor is not used, but must be implemented in order to override the ImageView class
     * @param context The context of the current activity
     * @param attrs The set of attributes added to the AppImageView
     * @param defStyle The defined style of the AppImageView
     */
    public AppImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    /**
     * Set an AppView as checked.
     * This is used in SettingsActivity for displaying which apps a user has chosen.
     * Also calls setRightColor to set the background color to the correct one.
     * @param checked
     */
    public void setChecked(boolean checked){
        this.checked = checked;
        setRightColor();
    }

    /**
     * Sets the right backgroundcolor of the AppImageView
     * If checked is not set, sets the background color to transparent
     * If checked is set, sets the background color to light orange.
     * This is only relevant for SettingsActivity to show which apps have been chosen by a user.
     */
    private void setRightColor(){
        if (checked){
            setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
        }
        else{
            setBackgroundColor(context.getResources().getColor(R.color.app_color_transparent));
        }
    }

    /**
     * Returns whether this AppImageView has been set as checked or unchecked
     * @return the value of checked.
     */
    public boolean isChecked(){
        return checked;
    }

    /**
     * Toogles an AppView as checked or unchecked.
     * This is used in SettingsActivity for displaying which apps a user has chosen.
     * Also calls setRightColor to set the background color to the correct one.
     * @return Whether checked was set to true or false
     */
    public boolean toggle(){
        if (!checked){
            checked = true;
        }
        else{
            checked = false;
        }

        setRightColor();

        return checked;
    }
}
