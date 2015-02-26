package dk.aau.cs.giraf.launcher.settings.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Custom preference class for the launcher settings. This preference provides a seeking bar and an
 * example icon to illustrate the chosen size.
 */
public class ApplicationGridResizer extends Preference implements SeekBar.OnSeekBarChangeListener {
    private final String TAG = getClass().getName(); // Error tag - maybe use the one from constants instead

    // Namespaces for preference properties
    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
    private static final String APPLICATIONNS = "http://giraf.cs.aau.dk";

    private static final String COLUMNS_SIZE_PREFERENCE_TAG = "COLUMNS_SIZE_PREFERENCE_TAG";
    private static final String ROWS_SIZE_PREFERENCE_TAG = "ROWS_SIZE_PREFERENCE_TAG";

    // default value for the starting position of the seeking bar, if not set in the preference xml
    private static final int DEFAULT_ROWS_VALUE = 4;
    private static final int DEFAULT_COLUMNS_VALUE = 5;

    public static int getGridColumnSize(final Context context, final Profile currentUser) {

        SharedPreferences pref = SettingsUtility.getLauncherSettings(context, LauncherUtility.getSharedPreferenceUser(currentUser));


        return pref.getInt(ApplicationGridResizer.COLUMNS_SIZE_PREFERENCE_TAG, DEFAULT_COLUMNS_VALUE);
    }

    public static int getGridRowSize(final Context context, final Profile currentUser) {

        SharedPreferences pref = SettingsUtility.getLauncherSettings(context, LauncherUtility.getSharedPreferenceUser(currentUser));

        return pref.getInt(ApplicationGridResizer.ROWS_SIZE_PREFERENCE_TAG, DEFAULT_ROWS_VALUE);
    }

    private Context mContext;
    private int mMaxValue = 10; // Max value for the seeking bar in units
    private int mMinValue = 2; // Min value for the seeking bar in units
    private int mInterval = 1; // each unit is 1 percent
    //private int mCurrentValue;
    private int mRowsCurrentValue;
    private int mColumnsCurrentValue;
    private String mUnitsLeft = "";
    private String mUnitsRight = "";
    private SeekBar mRowsSeekBar;
    private SeekBar mColumnsSeekBar;
    private Bitmap mAppBitmap;
    private GridLayout mExampleGridLayout;

    // constructor inherited from superclass
    public ApplicationGridResizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPreference(context, attrs);
    }

    // constructor inherited from superclass
    public ApplicationGridResizer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initPreference(context, attrs);
    }

    /**
     * Initialize the fields of this class with values from the xml file.
     *
     * @param context The context of the fragment which contains this setting.
     * @param attrs   Attributes for this preference set in the xml file.
     */
    private void initPreference(Context context, AttributeSet attrs) {
        setValuesFromXml(attrs);

        setWidgetLayoutResource(R.layout.icon_resize_component);
    }

    /**
     * Initializes fields with values from the xml file.
     *
     * @param attrs See {@link ApplicationGridResizer#initPreference(android.content.Context, android.util.AttributeSet)}.
     */
    private void setValuesFromXml(AttributeSet attrs) {
        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", mMaxValue);
        mMinValue = attrs.getAttributeIntValue(APPLICATIONNS, "min", mMinValue);

        mUnitsLeft = getAttributeStringValue(attrs, APPLICATIONNS, "unitsLeft", "");
        String units = getAttributeStringValue(attrs, APPLICATIONNS, "units", "");
        mUnitsRight = getAttributeStringValue(attrs, APPLICATIONNS, "unitsRight", units);

        try {
            String newInterval = attrs.getAttributeValue(APPLICATIONNS, "interval");
            if (newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        } catch (Exception e) {
            Log.e(TAG, "Invalid interval value", e);
        }
    }

    /**
     * Retrives a value from the XML file based on the namespace and name of the attribute.
     *
     * @param attrs        See {@link ApplicationGridResizer#initPreference(android.content.Context, android.util.AttributeSet)}.
     * @param namespace    The Namespace in which the attribute is defined.
     * @param name         Attribute name.
     * @param defaultValue If the attribute is not found, this will be the value.
     * @return The attribute value of the given attribute as a string.
     */
    private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
        String value = attrs.getAttributeValue(namespace, name);
        if (value == null)
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

        mRowsSeekBar = (SeekBar) view.findViewById(R.id.iconsResizerRowsSeekBar);
        mRowsSeekBar.setMax(mMaxValue - mMinValue);

        mColumnsSeekBar = (SeekBar) view.findViewById(R.id.iconsResizerColumnsSeekBar);
        mColumnsSeekBar.setMax(mMaxValue - mMinValue);


        mExampleGridLayout = (GridLayout) view.findViewById(R.id.example_grid_layout);

        return view;
    }

    /**
     * Ensures that we do not have multiple seeking bars in the view at the same time.
     * So before we add our seeking bar we clear the container for any views which may be present.
     *
     * @param view The view where the preferences are bound on.
     */
    @Override
    public void onBindView(View view) {
        super.onBindView(view);



        /*
        try {
            // move our seekbar to the new view we've been given
            ViewParent oldContainer = mRowsSeekBar.getParent();
            ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);

            if (oldContainer != newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    ((ViewGroup) oldContainer).removeView(mRowsSeekBar);
                }
                // remove the existing seekbar (there may not be one) and add ours
                newContainer.removeAllViews();
                newContainer.addView(mRowsSeekBar, ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
        catch(Exception ex) {
            Log.e(TAG, "Error binding view: " + ex.toString());
        }

        //if dependency is false from the beginning, disable the seek bar
        if (view != null && !view.isEnabled())
        {
            mRowsSeekBar.setEnabled(false);
        }*/
        final SharedPreferences pref = getSharedPreferences();

        mRowsCurrentValue = pref.getInt(ROWS_SIZE_PREFERENCE_TAG, DEFAULT_ROWS_VALUE);
        mColumnsCurrentValue = pref.getInt(COLUMNS_SIZE_PREFERENCE_TAG, DEFAULT_COLUMNS_VALUE);

        mRowsSeekBar.setProgress(mRowsCurrentValue - mMinValue);
        mColumnsSeekBar.setProgress(mColumnsCurrentValue - mMinValue);

        mRowsSeekBar.setOnSeekBarChangeListener(this);
        mColumnsSeekBar.setOnSeekBarChangeListener(this);

        updateExampleGridSize(mRowsCurrentValue, mColumnsCurrentValue);
    }

    /**
     * Updates the size of the icon to represent the chosen value.
     *
     * @param newRowSize    The new row value for the example grid.
     * @param newColumnSize The new row value for the example grid.
     */
    private void updateExampleGridSize(final int newRowSize, final int newColumnSize) {

        mExampleGridLayout.removeAllViews();

        mExampleGridLayout.setRowCount(newRowSize);
        mExampleGridLayout.setColumnCount(newColumnSize);
        /*
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(mExampleGridLayout.getLayoutParams().height, View.MeasureSpec.EXACTLY);

        mExampleGridLayout.measure(widthMeasureSpec, heightMeasureSpec);
        */
        for (int exampleGridFillerCounter = 0; exampleGridFillerCounter < newRowSize * newColumnSize; exampleGridFillerCounter++) {
            final View view = new View(getContext());
            view.setBackgroundResource(R.drawable.example_grid_layout_drawable);

            int height = mExampleGridLayout.getLayoutParams().height / newRowSize;
            int width = mExampleGridLayout.getLayoutParams().width / newColumnSize;

            mExampleGridLayout.addView(view, new ViewGroup.LayoutParams(width, height));
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        return ta.getInt(index, DEFAULT_ROWS_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        final SharedPreferences pref = getSharedPreferences();

        mColumnsCurrentValue = pref.getInt(COLUMNS_SIZE_PREFERENCE_TAG, DEFAULT_COLUMNS_VALUE);
        mRowsCurrentValue = pref.getInt(ROWS_SIZE_PREFERENCE_TAG, DEFAULT_ROWS_VALUE);
    }

    /**
     * When the user changes the value of the seeking bar this function is called. It is responsible
     * for representing the changes in the UI.
     *
     * @param seekBar  The seeking bar which needs to be updated.
     * @param progress The change in progress.
     * @param fromUser Indicates if the change was initiated by a user, the true else false.
     */


    @Override
    public void onProgressChanged(final SeekBar seekBar, int progress, final boolean fromUser) {


        // Ensure that the new value is within bounds of the min and max
        /*
        if (newValue > mMaxValue)
            newValue = mMaxValue;
        else if (newValue < mMinValue)
            newValue = mMinValue;
        else if (mInterval != 1 && newValue % mInterval != 0)
            newValue = Math.round(((float) newValue) / mInterval) * mInterval;
        */
        // change rejected, revert to the previous value
        /*
        if (!callChangeListener(newValue)) {
            seekBar.setProgress(mColumnsCurrentValue - mMinValue);
            return;
        }
        */

        // change accepted, store it
        mRowsCurrentValue = mRowsSeekBar.getProgress();
        mColumnsCurrentValue = mColumnsSeekBar.getProgress();

        if (fromUser) {
            if (seekBar == mRowsSeekBar) {
                if (progress > mColumnsCurrentValue + 1) {
                    mColumnsCurrentValue = progress - 1;
                    mColumnsSeekBar.setProgress(mColumnsCurrentValue);
                } else if (progress < mColumnsCurrentValue - 1) {
                    mColumnsCurrentValue = progress + 1;
                    mColumnsSeekBar.setProgress(mColumnsCurrentValue);
                }

                mRowsCurrentValue = progress;
            }
            if (seekBar == mColumnsSeekBar) {
                if (progress > mRowsCurrentValue + 1) {
                    mRowsCurrentValue = progress - 1;
                    mRowsSeekBar.setProgress(mRowsCurrentValue);
                } else if (progress < mRowsCurrentValue - 1) {
                    mRowsCurrentValue = progress + 1;
                    mRowsSeekBar.setProgress(mRowsCurrentValue);
                }

                mColumnsCurrentValue = progress;
            }
        }

        if (fromUser) {
            //mStatusText.setText(String.valueOf(newValue));
            SharedPreferences.Editor editor = getSharedPreferences().edit();

            mRowsCurrentValue += mMinValue;
            mColumnsCurrentValue += mMinValue;

            editor.putInt(COLUMNS_SIZE_PREFERENCE_TAG, mColumnsCurrentValue);
            editor.putInt(ROWS_SIZE_PREFERENCE_TAG, mRowsCurrentValue);

            editor.commit();
            //notifyChanged();
            updateExampleGridSize(mRowsCurrentValue, mColumnsCurrentValue);
        }


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * make sure that the seekbar is disabled if the preference is disabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mRowsSeekBar.setEnabled(enabled);
        mColumnsSeekBar.setEnabled(enabled);
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);

        //Disable movement of seek bar when dependency is false
        if (mRowsSeekBar != null) {
            mRowsSeekBar.setEnabled(!disableDependent);
        }
        if (mColumnsSeekBar != null) {
            mColumnsSeekBar.setEnabled(!disableDependent);
        }
    }
}
