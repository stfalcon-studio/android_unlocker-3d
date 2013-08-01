package com.stfalcon.unlocker;

import java.util.TreeSet;

/**
 * Created by user on 7/31/13.
 */
public class Comparison {

    private static final double smoothing = 0.05;


    public static boolean comparisonArray(double[] a, double[] a1){
        double point = a[0];
        double point1 = a1[0];
        double dx = Math.abs(a[0] - a1[0]);

        if (a[0] <= a1[0]) point = point + dx;
        if (a[0] > a1[0]) point = point - dx;

        if (makeHashSet(a1[0]).contains(new Double(point))){
            for (int i = 0; i < a1.length; i++){
                if (a[0] <= a1[0]) point = a[i] + dx;
                if (a[0] > a1[0]) point = a[i] - dx;
                if (makeHashSet(a1[i]).contains(new Double(point))) return false;
            }
        }
            return true;
    }

    private static TreeSet<Double> makeHashSet(double x){
        TreeSet<Double> set = new TreeSet();
        for (double i = x - smoothing; i < x + smoothing; i =+ 0.0001) set.add(new Double(i));
        return set;
    }


}
