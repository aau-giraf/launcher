package dk.aau.cs.giraf.launcher.settings.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.helper.LauncherUtility;
import dk.aau.cs.giraf.launcher.settings.SettingsUtility;
import dk.aau.cs.giraf.launcher.widgets.GridPreviewView;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Custom preference class for the launcher settings. This preference provides two seeking bars and an
 * example grid to illustrate the chosen size.
 */
public class ApplicationGridResizer extends Preference implements SeekBar.OnSeekBarChangeListener {

    // Namespaces for preference properties
    private static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";
    private static final String APPLICATIONS = "http://giraf.cs.aau.dk";

    private static final String COLUMNS_SIZE_PREFERENCE_TAG = "COLUMNS_SIZE_PREFERENCE_TAG";
    private static final String ROWS_SIZE_PREFERENCE_TAG = "ROWS_SIZE_PREFERENCE_TAG";

    // default value for the starting position of the seeking bar, if not set in the preference xml
    private static final int DEFAULT_ROWS_VALUE = 4;
    private static final int DEFAULT_COLUMNS_VALUE = 5;

    /**
     * @param context
     * @param currentUser
     * @return The column size saved in the shared preference for the current user
     */
    public static int getGridColumnSize(final Context context, final Profile currentUser) {

        final SharedPreferences pref = SettingsUtility.getLauncherSettings(context, LauncherUtility.getSharedPreferenceUser(currentUser));

        return pref.getInt(ApplicationGridResizer.COLUMNS_SIZE_PREFERENCE_TAG, DEFAULT_COLUMNS_VALUE);
    }

    /**
     * @param context
     * @param currentUser
     * @return The row size saved in the shared preference for the current user
     */
    public static int getGridRowSize(final Context context, final Profile currentUser) {

        final SharedPreferences pref = SettingsUtility.getLauncherSettings(context, LauncherUtility.getSharedPreferenceUser(currentUser));

        return pref.getInt(ApplicationGridResizer.ROWS_SIZE_PREFERENCE_TAG, DEFAULT_ROWS_VALUE);
    }

    private int mMaxValue = 7; // Max value for the seeking bar in units
    private int mMinValue = 2; // Min value for the seeking bar in units

    private int mRowsCurrentValue;
    private int mColumnsCurrentValue;
    private SeekBar gridSizeSeekBar;

    private GridPreviewView mExampleGridLayout;
    private TextView ExampleTextView;

    // constructor inherited from superclass
    public ApplicationGridResizer(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initPreference(attrs);
    }

    // constructor inherited from superclass
    public ApplicationGridResizer(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initPreference(attrs);
    }

    /**
     * Initialize the fields of this class with values from the xml file.
     *
     * @param attrs Attributes for this preference set in the xml file.
     */
    private void initPreference(final AttributeSet attrs) {
        setValuesFromXml(attrs);

        setWidgetLayoutResource(R.layout.icon_resize_component);
    }

    /**
     * Initializes fields with values from the xml file.
     *
     * @param attrs See {@link ApplicationGridResizer#initPreference(android.util.AttributeSet)}.
     */
    private void setValuesFromXml(final AttributeSet attrs) {
        mMaxValue = attrs.getAttributeIntValue(ANDROID_SCHEMA, "max", mMaxValue);
        mMinValue = attrs.getAttributeIntValue(APPLICATIONS, "min", mMinValue);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {

        final View view = super.onCreateView(parent);

        // The basic preference layout puts the widget frame to the right of the title and summary,
        // so we need to change it a bit - the seekbar should be under them.
        final LinearLayout layout = (LinearLayout) view;
        layout.setOrientation(LinearLayout.VERTICAL);

        gridSizeSeekBar = (SeekBar) view.findViewById(R.id.gridResizerSeekBar);
        gridSizeSeekBar.setMax(mMaxValue - mMinValue);

        ExampleTextView = (TextView) view.findViewById(R.id.example_text);

        mExampleGridLayout = (GridPreviewView) view.findViewById(R.id.example_grid_layout);

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

        final SharedPreferences pref = getSharedPreferences();

        mRowsCurrentValue = pref.getInt(ROWS_SIZE_PREFERENCE_TAG, DEFAULT_ROWS_VALUE);
        mColumnsCurrentValue = pref.getInt(COLUMNS_SIZE_PREFERENCE_TAG, DEFAULT_COLUMNS_VALUE);

        ExampleTextView.setText(getContext().getString(R.string.setting_launcher_grid_example_text) + " " + mRowsCurrentValue + " × " + mColumnsCurrentValue);

        gridSizeSeekBar.setProgress(mRowsCurrentValue - mMinValue);

        gridSizeSeekBar.setOnSeekBarChangeListener(this);

        updateExampleGridSize(mRowsCurrentValue, mColumnsCurrentValue);
    }

    /**
     * Updates the size of the icon to represent the chosen value.
     *
     * @param newRowSize    The new row value for the example grid.
     * @param newColumnSize The new row value for the example grid.
     */
    private void updateExampleGridSize(final int newRowSize, final int newColumnSize) {

        mExampleGridLayout.setRowSize(newRowSize);
        mExampleGridLayout.setColumnSize(newColumnSize);
        mExampleGridLayout.invalidate();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        return ta.getInt(index, DEFAULT_ROWS_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        final SharedPreferences pref = getSharedPreferences();

        mRowsCurrentValue = pref.getInt(ROWS_SIZE_PREFERENCE_TAG, DEFAULT_ROWS_VALUE);
        mColumnsCurrentValue = pref.getInt(COLUMNS_SIZE_PREFERENCE_TAG, DEFAULT_COLUMNS_VALUE);
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
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

        // change accepted, store it
        mRowsCurrentValue = gridSizeSeekBar.getProgress();
        mColumnsCurrentValue = mRowsCurrentValue + 1;

        if (fromUser) {

            SharedPreferences.Editor editor = getSharedPreferences().edit();

            mRowsCurrentValue += mMinValue;
            mColumnsCurrentValue += mMinValue;

            editor.putInt(ROWS_SIZE_PREFERENCE_TAG, mRowsCurrentValue);
            editor.putInt(COLUMNS_SIZE_PREFERENCE_TAG, mColumnsCurrentValue);

            editor.commit();

            ExampleTextView.setText(getContext().getString(R.string.setting_launcher_grid_example_text) + " " + mRowsCurrentValue + " × " + mColumnsCurrentValue);

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
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        gridSizeSeekBar.setEnabled(enabled);
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);

        //Disable movement of seek bar when dependency is false
        if (gridSizeSeekBar != null) {
            gridSizeSeekBar.setEnabled(!disableDependent);
        }
    }
}
