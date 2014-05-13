package dk.aau.cs.giraf.launcher.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.settingslib.settingslib.Fragments.LauncherSettings;

/**
 * Created by Frederik on 29-04-14.
 */
public class SettingsUtility {

    public static final String LAUNCHER_SETTINGS_TAG = LauncherSettings.class.getName();

    public static String getLauncherSettingsTag(String user){
        return LAUNCHER_SETTINGS_TAG + "." + user + ".prefs";
    }

    public static SharedPreferences getLauncherSettings(Context context, String user){
        return context.getSharedPreferences(getLauncherSettingsTag(user), Context.MODE_PRIVATE);
    }

    public static int convertToDP(Context context, int value){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    public static Bitmap createBitmapFromLayoutWithText(Context context, View view, int widthInDP, int heightInDP) {
        int width = convertToDP(context, widthInDP);
        int height = convertToDP(context, heightInDP);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        //Create a canvas with the specified bitmap to draw into
        Canvas c = new Canvas(bitmap);

        //Render this view (and all of its children) to the given Canvas
        view.draw(c);
        view.setVisibility(View.GONE);
        return bitmap;
    }
}
