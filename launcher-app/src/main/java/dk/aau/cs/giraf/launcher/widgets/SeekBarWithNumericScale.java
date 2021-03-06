package dk.aau.cs.giraf.launcher.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.SeekBar;
import dk.aau.cs.giraf.launcher.R;

/**
 * Created by Marhlder on 26-02-2015.
 */
public class SeekBarWithNumericScale extends SeekBar {

    private Paint paint;

    private float textSize;
    private int firstScaleItemValue;
    private int lastScaleItemValue;

    /**
     * One of the constructers.
     * @param context the context
     * @param firstScaleItemValue the first scale value
     * @param lastScaleItemValue the last scale value
     * @param scaleFontSize the font scale size
     */
    public SeekBarWithNumericScale(final Context context,
                                   final int firstScaleItemValue, final int lastScaleItemValue, final int scaleFontSize)
    {
        super(context);

        init(firstScaleItemValue, lastScaleItemValue, toSp(scaleFontSize));
    }

    /**
     * One of the constructers.
     * @param context the context
     * @param attrs some attributes
     */
    public SeekBarWithNumericScale(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SeekBarWithNumericScale, 0, 0);

        init(a.getInteger(R.styleable.SeekBarWithNumericScale_firstScaleItemValue, -1),
            a.getInteger(R.styleable.SeekBarWithNumericScale_lastScaleItemValue, -1),
            a.getDimension(R.styleable.SeekBarWithNumericScale_scaleTextSize, toSp(14)));
    }

    /**
     * On of the constructers.
     * @param context the context
     * @param attrs some attributes
     * @param defStyle the def style
     */
    public SeekBarWithNumericScale(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SeekBarWithNumericScale, 0, 0);

        init(a.getInteger(R.styleable.SeekBarWithNumericScale_firstScaleItemValue, -1),
            a.getInteger(R.styleable.SeekBarWithNumericScale_lastScaleItemValue, -1),
            a.getDimension(R.styleable.SeekBarWithNumericScale_scaleTextSize, toSp(14)));
    }

    /**
     * Converts a float value to the corresponding sp value.
     *
     * @param integer input
     * @return float value
     */
    private float toSp(final float integer) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                integer, getResources().getDisplayMetrics());
    }

    /**
     * Initialize the seekbar.
     * @param firstScaleItemValue the first scale value
     * @param lastScaleItemValue the last scale value
     * @param fontSize the font size
     */
    public void init(final int firstScaleItemValue, final int lastScaleItemValue, final float fontSize) {

        this.firstScaleItemValue = firstScaleItemValue;
        this.lastScaleItemValue = lastScaleItemValue;

        if (lastScaleItemValue - firstScaleItemValue < 0) {
            throw new IllegalArgumentException("lastScaleItemValue should be bigger than firstScaleItemValue");
        }

        textSize = fontSize;

        // Initialize paint object for text drawing
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        // Add some more padding to the bottom to allow text to be within canvas
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + (int) textSize);
    }

    /**
     * This is where the magic happens.
     * We draw scale
     *
     * @param canvas the canvas
     */
    @Override
    protected synchronized void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (firstScaleItemValue != -1 && lastScaleItemValue != -1) {

            final int scaleElementCount = (lastScaleItemValue - firstScaleItemValue) + 1;

            // Get the size of the canvas
            final int width = canvas.getWidth();
            final int height = canvas.getHeight();

            // Get the padding
            final int paddingLeft = this.getPaddingLeft();
            final int paddingRight = this.getPaddingRight();

            final int distanceBetweenScaleElements = (width - paddingLeft - paddingRight) / (scaleElementCount - 1);

            for (int scaleElementCounter = 0; scaleElementCounter < scaleElementCount; scaleElementCounter++) {
                final int rowCount = (scaleElementCounter + firstScaleItemValue);
                final int columnCount = (scaleElementCounter + firstScaleItemValue + 1);

                final String text = "" + rowCount * columnCount;

                // Lesser hax math that makes it look pretty on the x axis
                final float xCoordinate = paddingLeft + distanceBetweenScaleElements *
                    scaleElementCounter - paint.measureText(text) / 2;

                // We want the scale items to be slightly off the mid on the y axis (below the SeekBar)
                final float yCoordinate = height / 2 + textSize * 1.5f;

                // Print text for the current scale item for the SeekBar
                canvas.drawText(text, xCoordinate, yCoordinate, paint);
            }
        }
    }
}
