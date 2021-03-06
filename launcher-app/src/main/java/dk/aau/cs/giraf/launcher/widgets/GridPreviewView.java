package dk.aau.cs.giraf.launcher.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Marhlder on 26-02-2015.
 * View which draws a grid with rowSize rows and columnSize columns
 */
public class GridPreviewView extends View {
    private Paint paint;

    private int rowSize = 3;
    private int columnSize = 4;

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

    /**
     * Calculates gridLines size and initializes paint object.
     */
    public void init() {

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
    }

    /**
     * Sets the rowSize of this GridPreviewView
     * Clients should call .invalidate() to update the view
     *
     * @param rowSize the row size of the grid
     */
    public void setRowSize(final int rowSize) {
        if(rowSize > 3) {
            this.rowSize = rowSize;
        }
        else{
            this.rowSize = 3;
        }
    }

    /**
     * Sets the columnSize of this GridPreviewView
     * Clients should call .invalidate() to update the view
     *
     * @param columnSize the column size of the grid
     */
    public void setColumnSize(final int columnSize) {
        if(columnSize > 4) {
            this.columnSize = columnSize;
        }
        else{
            this.columnSize = 4;
        }
    }

    /**
     * Override on draw to draw a grid with this.rowSize rows and this.columnSize columns
     *
     * @param canvas the canvas on which will be drawn on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        final int columnSpacing = width / columnSize;
        final int rowSpacing = height / rowSize;

        int restWidth = width % columnSize;
        int restHeight = height % rowSize;

        int offsetX = 0;
        int offsetY = 0;

        //Draw all vertical lines
        for (int columnCounter = 0; columnCounter <= columnSize; columnCounter++) {

            if (restWidth > 0 && columnCounter != 0) {
                offsetX = columnCounter;
                restWidth--;
            }

            final int x = columnCounter * columnSpacing + offsetX;

            canvas.drawLine(x, 0, x, height, paint);
        }

        //Draw all horizontal lines
        for (int rowCounter = 0; rowCounter <= rowSize; rowCounter++) {

            if (restHeight > 0 && rowCounter != 0) {
                offsetY = rowCounter;
                restHeight--;
            }

            final int y = rowCounter * rowSpacing + offsetY;

            canvas.drawLine(0, y, width, y, paint);
        }
    }
}
