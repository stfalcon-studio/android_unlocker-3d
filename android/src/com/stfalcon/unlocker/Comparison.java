package com.stfalcon.unlocker;

import android.util.Log;

/**
 * Created by user on 7/31/13.
 */
public class Comparison {

    private static final double smoothing = 0.0500;
    private static int procent = 0;

    public static int comparisonArray(double[] a, double[] a1) {
        /*double point = a[0];
        double point1 = a1[0];
        Log.i("Loger","point = " + point);*/
        /*double dx = Math.abs(a[0] - a1[0]);

        if (a[0] <= a1[0]) point = point + dx;
        if (a[0] > a1[0]) point = point - dx;*/

        //  if ((a1[0] > point - smoothing) && (a1[0] < point + smoothing)){
        for (int i = 0; i < a1.length; i++) {
                /*if (a[0] <= a1[0]) point = a[i] + dx;
                if (a[0] > a1[0]) point = a[i] - dx;*/
            if ((a1[i] > a[i] - smoothing) && (a1[i] < a[i] + smoothing)) procent++;
        }
        Log.v("LOGER", "proc" + procent);
        // }
        return (procent / a1.length) * 100;
    }

    public static double pirsonCompare(double[] x, double[] y) {
        int len = 0;
        if (x.length > y.length) {
            len = y.length;
        } else {
            if (((y.length * 0.8) > x.length)) {
                return -1;
            }
            len = x.length;

        }
        double xs = 0;
        for (int i = 0; i < len; i++) {
            xs += x[i];
        }
        xs = xs / x.length;
        double ys = 0;
        for (int i = 0; i < len; i++) {
            ys += y[i];
        }
        ys = ys / y.length;
        double dxy = 0;
        for (int i = 0; i < len; i++) {
            dxy += (x[i] - xs) * (y[i] - ys);
        }

        double mqx = 0;
        for (int i = 0; i < len; i++) {
            mqx += Math.pow(x[i] - xs, 2);
        }
        mqx = Math.sqrt(mqx);

        double mqy = 0;
        for (int i = 0; i < len; i++) {
            mqy += Math.pow(y[i] - ys, 2);
        }
        mqy = Math.sqrt(mqy);

        double rxy = dxy / (mqx * mqy);
        return rxy;
    }

}
