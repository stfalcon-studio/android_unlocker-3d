package com.stfalcon.unlocker.LockGraphView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import com.stfalcon.unlocker.R;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 9/9/13
 * Time: 10:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class LockGraphView extends View {

    private DataSeries[] series;
    private Canvas canvas;
    Double minY = null;
    Double minX = null;
    Double maxX = null;
    Double maxY = null;
    double diffY;
    double diffX;

    public LockGraphView(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    public void setData(DataSeries[] series) {
        this.series = series;
        if (minX == null) {
            minX = getMinX();
        }
        if (minY == null) {
            minY = getMinY();
        }
        if (maxX == null) {
            maxX = getMaxX();
        }
        if (maxY == null) {
            maxY = getMaxY();
        }
        diffX = maxX - minX;
        diffY = maxY - minY;
    }


    private void drawGraph(GraphData[] data, int color, float lineWidth) {

        // draw background
        double lastEndY = 0;
        double lastEndX = 0;
        int horstart = 0;


        // draw data
        Paint paint = new Paint();
        paint.setStrokeWidth(lineWidth);
        paint.setColor(color);

        lastEndY = 0;
        lastEndX = 0;
        for (int i = 0; i < data.length; i++) {
            double valY = data[i].getY() - minY;
            double ratY = valY / diffY;
            double y = getHeight() * ratY;

            double valX = data[i].getX() - minX;
            double ratX = valX / diffX;
            double x = getWidth() * ratX;

            if (i > 0) {
                float startX = (float) lastEndX + (horstart + 1);
                float startY = (float) (10 - lastEndY) + getHeight();
                float endX = (float) x + (horstart + 1);
                float endY = (float) (10 - y) + getHeight();
                canvas.drawLine(startX, startY, endX, endY, paint);
            }
            lastEndY = y;
            lastEndX = x;
        }
        // Log.v("LOGER", "X:" + data.length + "Y:" + data.length);
    }


    public void setMaxMin(double maxX, double maxY, double minX, double minY) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
    }

    public void setMaxMin(double maxY, double minY) {
        this.maxY = maxY;
        this.minY = minY;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);    //To change body of overridden methods use File | Settings | File Templates.
        this.canvas = canvas;
        if (series != null) {
            for (int i = 0; i < series.length; i++) {
                if (series[i] != null) {
                    drawGraph(series[i].getGraphData(), series[i].getColor(), series[i].getLineWidth());
                }
            }
        }

    }

    protected double getMaxX() {
        // if viewport is set, use this

        // otherwise use the max x value
        // values must be sorted by x, so the last value has the largest X value
        double highest = 0;
        if (series.length > 0) {
            GraphData[] values = series[0].getGraphData();
            if (values.length == 0) {
                highest = 0;
            } else {
                highest = values[values.length - 1].getX();
            }
            for (int i = 1; i < series.length; i++) {
                values = series[i].getGraphData();
                if (values.length > 0) {
                    highest = Math.max(highest, values[values.length - 1].getX());
                }
            }
        }
        return highest;
    }

    /**
     * returns the maximal Y value of all data.
     * <p/>
     * warning: only override this, if you really know want you're doing!
     */
    protected double getMaxY() {
        double largest = Integer.MIN_VALUE;
        for (int i = 0; i < series.length; i++) {
            GraphData[] values = series[i].getGraphData();
            for (int ii = 0; ii < values.length; ii++)
                if (values[ii].getY() > largest)
                    largest = values[ii].getY();

        }
        return largest;
    }

    /**
     * returns the minimal X value of the current viewport (if viewport is set)
     * otherwise minimal X value of all data.
     */
    protected double getMinX() {
        // if viewport is set, use this
        // otherwise use the min x value
        // values must be sorted by x, so the first value has the smallest X value
        double lowest = 0;
        if (series.length > 0) {
            GraphData[] values = series[0].getGraphData();
            if (values.length == 0) {
                lowest = 0;
            } else {
                lowest = values[0].getX();
            }
            for (int i = 1; i < series.length; i++) {
                values = series[i].getGraphData();
                if (values.length > 0) {
                    lowest = Math.min(lowest, values[0].getX());
                }
            }
        }
        return lowest;
    }

    /**
     * returns the minimal Y value of all data.
     * <p/>
     * warning: only override this, if you really know want you're doing!
     */
    protected double getMinY() {
        double smallest;
        smallest = Integer.MAX_VALUE;
        for (int i = 0; i < series.length; i++) {
            GraphData[] values = series[i].getGraphData();
            for (int ii = 0; ii < values.length; ii++)
                if (values[ii].getY() < smallest)
                    smallest = values[ii].getY();
        }
        return smallest;
    }
}
