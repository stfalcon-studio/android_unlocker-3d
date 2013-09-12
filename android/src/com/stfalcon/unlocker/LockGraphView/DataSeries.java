package com.stfalcon.unlocker.LockGraphView;

import android.R;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 9/9/13
 * Time: 11:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataSeries {
    private GraphData[] graphData;
    private int color;
    private float lineWidth;

    public void setLineStyle(int color, float lineWidth) {
        this.color = color;
        this.lineWidth = lineWidth;
    }

    public DataSeries(GraphData[] graphData) {
        this.graphData = graphData;
        color = R.color.black;
        lineWidth = 1;
    }


    public GraphData[] getGraphData() {
        return graphData;
    }

    public void setGraphData(GraphData[] graphData) {
        this.graphData = graphData;
    }

    public int getColor() {
        return color;
    }

    public float getLineWidth() {
        return lineWidth;
    }
}
