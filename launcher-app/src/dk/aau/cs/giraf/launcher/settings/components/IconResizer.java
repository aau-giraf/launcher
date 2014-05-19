package dk.aau.cs.giraf.launcher.settings.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.AppViewCreationUtility;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;

/**
 * Custom preference class for the launcher settings. This preference provides a seeking bar and an
 * example icon to illustrate the chosen size.
 */
public class IconResizer extends Preference implements SeekBar.OnSeekBarChangeListener {
    private final String TAG = getClass().getName(); // Error tag - maybe use the one from constants instead

    // Namespaces for preference properties
    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
    private static final String APPLICATIONNS = "http://giraf.cs.aau.dk";

    // default value for the starting position of the seeking bar, if not set in the preference xml
    private static final int DEFAULT_VALUE = 50;

    private Context mContext;
    private int mMaxValue      = 100; // Max value for the seeking bar in units
    private int mMinValue      = 0; // Min value for the seeking bar in units
    private int mInterval      = 1; // each unit is 1 percent
    private int mCurrentValue;
    private String mUnitsLeft  = "";
    private String mUnitsRight = "";
    private SeekBar mSeekBar;
    private Bitmap mAppBitmap;
    private ImageView mAppImageView;

    // constructor inherited from superclass
    public IconResizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPreference(context, attrs);
    }

    // constructor inherited from superclass
    public IconResizer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initPreference(context, attrs);
    }

    /**
     * Initialize the fields of this class with values from the xml file.
     * @param context The context of the fragment which contains this setting.
     * @param attrs Attributes for this preference set in the xml file.
     */
    private void initPreference(Context context, AttributeSet attrs) {
        setValuesFromXml(attrs);
        mSeekBar = new SeekBar(context, attrs);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        setWidgetLayoutResource(R.layout.icon_resize_component);
    }

    /**
     * Initializes fields with values from the xml file.
     * @param attrs See {@link dk.aau.cs.giraf.launcher.settings.components.IconResizer#initPreference(android.content.Context, android.util.AttributeSet)}.
     */
    private void setValuesFromXml(AttributeSet attrs) {
        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
        mMinValue = attrs.getAttributeIntValue(APPLICATIONNS, "min", 0);

        mUnitsLeft = getAttributeStringValue(attrs, APPLICATIONNS, "unitsLeft", "");
        String units = getAttributeStringValue(attrs, APPLICATIONNS, "units", "");
        mUnitsRight = getAttributeStringValue(attrs, APPLICATIONNS, "unitsRight", units);

        try {
            String newInterval = attrs.getAttributeValue(APPLICATIONNS, "interval");
            if(newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        }
        catch(Exception e) {
            Log.e(TAG, "Invalid interval value", e);
        }
    }

    /**
     * Retrives a value from the XML file based on the namespace and name of the attribute.
     * @param attrs See {@link dk.aau.cs.giraf.launcher.settings.components.IconResizer#initPreference(android.content.Context, android.util.AttributeSet)}.
     * @param namespace The Namespace in which the attribute is defined.
     * @param name Attribute name.
     * @param defaultValue If the attribute is not found, this will be the value.
     * @return The attribute value of the given attribute as a string.
     */
    private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
        String value = attrs.getAttributeValue(namespace, name);
        if(value == null)
            value = defaultValue;

        return value;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        // The basic preference layout puts the widget frame to the right of the title and summary,
        // so we need to change it a bit - the seekbar should be under them.
        LinearLayout layout = (LinearLayout) view;
        layout.setOrientation(LinearLayout.VERTICAL);

        return view;
    }

    /**
     * Ensures that we do not have multiple seeking bars in the view at the same time.
     * So before we add our seeking bar we clear the container for any views which may be present.
     * @param view The view where the preferences are bound on.
     */
    @Override
    public void onBindView(View view) {
        super.onBindView(view);

        try {
            // move our seekbar to the new view we've been given
            ViewParent oldContainer = mSeekBar.getParent();
            ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);

            if (oldContainer != newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    ((ViewGroup) oldContainer).removeView(mSeekBar);
                }
                // remove the existing seekbar (there may not be one) and add ours
                newContainer.removeAllViews();
                newContainer.addView(mSeekBar, ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
        catch(Exception ex) {
            Log.e(TAG, "Error binding view: " + ex.toString());
        }

        //if dependency is false from the beginning, disable the seek bar
        if (view != null && !view.isEnabled())
        {
            mSeekBar.setEnabled(false);
        }

        updateView(view);
    }

    /**
     * Update a SeekBarPreference view with our current state. That is the new value of which the
     * seeking preference is set to.
     * @param view The view is passed on to get references to other elements.
     */
    protected void updateView(View view) {
        try {
            mSeekBar.setProgress(mCurrentValue - mMinValue);

            View appView = view.findViewById(R.id.app_bg);
            if (mAppBitmap == null){
                mAppBitmap = AppViewCreationUtility.createBitmapFromLayoutWithText(getContext(), appView, 200, 200);
            }
            mAppImageView = (ImageView) view.findViewById(R.id.app_image);

            updateAppSize(mCurrentValue);
        }
        catch(Exception e) {
            Log.e(TAG, "Error updating seek bar preference", e);
        }
    }

    /**
     * When the user changes the value of the seeking bar this function is called. It is responsible
     * for representing the changes in the UI.
     * @param seekBar The seeking bar which needs to be updated.
     * @param progress The change in progress.
     * @param fromUser Indicates if the change was initiated by a user, the true else false.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = progress + mMinValue;

        // Ensure that the new value is within bounds of the min and max
        if(newValue > mMaxValue)
            newValue = mMaxValue;
        else if(newValue < mMinValue)
            newValue = mMinValue;
        else if(mInterval != 1 && newValue % mInterval != 0)
            newValue = Math.round(((float)newValue)/mInterval)*mInterval;

        // change rejected, revert to the previous value
        if(!callChangeListener(newValue)){
            seekBar.setProgress(mCurrentValue - mMinValue);
            return;
        }

        // change accepted, store it
        mCurrentValue = newValue;

        try{
            updateAppSize(mCurrentValue);
        } catch (NullPointerException ex){
            Log.e(TAG, "Failed scaling icon.");
        }

        //mStatusText.setText(String.valueOf(newValue));
        persistInt(newValue);
    }

    /**
     * Updates the size of the icon to represent the chosen value.
     * @param newValue The value which the icon should be scaled to.
     */
    private void updateAppSize(int newValue){
        int dpValue = LauncherUtility.intToDP(mContext, newValue);
        mAppImageView.setImageBitmap(mAppBitmap);
        ViewGroup.LayoutParams params = mAppImageView.getLayoutParams();
        params.width = dpValue;
        params.height = dpValue;
        mAppImageView.setLayoutParams(params);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index){
        return ta.getInt(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if(restoreValue) {
            mCurrentValue = getPersistedInt(mCurrentValue);
        }
        else {
            int temp = 0;
            try {
                temp = (Integer)defaultValue;
            }
            catch(Exception ex) {
                Log.e(TAG, "Invalid default value: " + defaultValue.toString());
            }

            persistInt(temp);
            mCurrentValue = temp;
        }
    }

    /**
     * make sure that the seekbar is disabled if the preference is disabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mSeekBar.setEnabled(enabled);
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);

        //Disable movement of seek bar when dependency is false
        if (mSeekBar != null)
        {
            mSeekBar.setEnabled(!disableDependent);
        }
    }
}
