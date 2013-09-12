package com.stfalcon.unlocker.LockGraphView;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 9/9/13
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class GraphData {
    private double x;
    private double y;

    public GraphData(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
