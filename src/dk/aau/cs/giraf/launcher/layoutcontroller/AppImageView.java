package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;

import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Frederik on 06-05-14.
 */
public class AppImageView extends ImageView {
    private boolean checked = false;
    private Context context;

    public AppImageView(Context context) {
        super(context);
        this.context = context;
    }

    public AppImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public AppImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void setChecked(boolean checked){
        this.checked = checked;
        setRightColor();
    }

    private void setRightColor(){
        if (checked){
            setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        }
        else{
            setBackgroundColor(context.getResources().getColor(R.color.app_color_transparent));
        }
    }

    public boolean isChecked(){
        return checked;
    }

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
