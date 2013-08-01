package com.stfalcon.unlocker;

import android.util.Log;

import java.util.TreeSet;

/**
 * Created by user on 7/31/13.
 */
public class Comparison {

    private static final double smoothing = 0.00005;
    private static int procent = 0;


    public static int comparisonArray(double[] a, double[] a1){
        double point = a[0];
        double point1 = a1[0];
        Log.i("Loger","point = " + point);
        /*double dx = Math.abs(a[0] - a1[0]);

        if (a[0] <= a1[0]) point = point + dx;
        if (a[0] > a1[0]) point = point - dx;*/

      //  if ((a1[0] > point - smoothing) && (a1[0] < point + smoothing)){
            for (int i = 0; i < a1.length - 1; i++){
                /*if (a[0] <= a1[0]) point = a[i] + dx;
                if (a[0] > a1[0]) point = a[i] - dx;*/
                if ((a1[i] > point - smoothing) && (a1[i] < point + smoothing)) procent++;
            }
       // }
            return procent/4;
    }


}
