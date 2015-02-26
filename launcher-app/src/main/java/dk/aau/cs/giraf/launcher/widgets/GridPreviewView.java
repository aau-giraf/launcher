package dk.aau.cs.giraf.launcher.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Marhlder on 26-02-2015.
 */
public class GridPreviewView extends View {
    private int gridLinesSize;
    private Paint paint;

    private int rowSize;
    private int columnSize;

    public GridPreviewView(Context context) {
        super(context);
        init();
    }

    public GridPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {

        gridLinesSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8, getResources().getDisplayMetrics());


        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
    }

    public void setRowSize(final int rowSize) {
        this.rowSize = rowSize;
    }

    public void setColumnSize(final int columnSize) {
        this.columnSize = columnSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        final int columnSpacing = width / columnSize;
        final int rowSpacing = height / rowSize;

        for (int columnCounter = 0; columnCounter <= columnSize; columnCounter++) {

            canvas.drawLine(columnCounter * columnSpacing, 0, columnCounter * columnSpacing, height, paint);
        }

        for (int rowCounter = 0; rowCounter <= rowSize; rowCounter++) {
            canvas.drawLine(0, rowCounter * rowSpacing , width , rowCounter * rowSpacing, paint);
        }
    }
}
