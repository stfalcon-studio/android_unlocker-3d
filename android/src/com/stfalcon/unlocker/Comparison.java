package com.stfalcon.unlocker;

import java.util.TreeSet;

/**
 * Created by user on 7/31/13.
 */
public class Comparison {

    private static final int smoothing = 10;


    public static boolean comparisonArray(int[] a, int[] a1){
        int point = a[0];
        int point1 = a1[0];
        int dx = Math.abs(a[0] - a1[0]);

        if (a[0] <= a1[0]) point = point + dx;
        if (a[0] > a1[0]) point = point - dx;

        if (makeHashSet(a1[0]).contains(new Integer(point))){
            for (int i = 0; i < a1.length; i++){
                if (a[0] <= a1[0]) point = a[i] + dx;
                if (a[0] > a1[0]) point = a[i] - dx;
                if (makeHashSet(a1[i]).contains(new Integer(point))) return false;
            }
        }
            return true;
    }

    private static TreeSet<Integer> makeHashSet(int x){
        TreeSet<Integer> set = new TreeSet();
        for (int i = x - smoothing; i < x + smoothing; i++) set.add(new Integer(i));
        return set;
    }


}
