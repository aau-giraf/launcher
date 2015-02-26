package dk.aau.cs.giraf.launcher.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.SeekBar;

/**
 * Created by Marhlder on 26-02-2015.
 */
public class SeekBarWithScale extends SeekBar {
    private Paint paint;

    private int textSize;

    public SeekBarWithScale(Context context) {
        super(context);
        init();
    }

    public SeekBarWithScale(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SeekBarWithScale(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {

        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                14, getResources().getDisplayMetrics());


        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + textSize);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int scaleElementCount = 9;

        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        final int distanceBetweenScaleElements = width / scaleElementCount;

        for (int scaleElementCounter = 0; scaleElementCounter < scaleElementCount; scaleElementCounter++) {
            canvas.drawText("" + (scaleElementCounter + 2), (distanceBetweenScaleElements / 5) * 2 + distanceBetweenScaleElements * scaleElementCounter, height / 2 + textSize * 1.5f, paint);
        }
    }
}
