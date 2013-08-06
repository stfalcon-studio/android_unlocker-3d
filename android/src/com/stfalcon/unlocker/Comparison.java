package com.stfalcon.unlocker;

import java.util.Arrays;

/**
 * Created by user on 7/31/13.
 */
public class Comparison {

    public static double[] prepareArray(double[] array) {
        try {
            double offset = 0.00009;
            int i;
            for (i = 0; i < array.length; i++) {
                if (array[i] < offset && array[i] > (-offset)) {
                } else {
                    break;
                }
            }
            double[] pArray = Arrays.copyOfRange(array, i, array.length);
            for (i = pArray.length - 1; i >= 0; i--) {
                if (pArray[i] < offset && pArray[i] > (-offset)) {
                } else {
                    break;
                }
            }
            pArray = Arrays.copyOfRange(pArray, 0, i);
            return pArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double pirsonCompare(double[] x, double[] y) {
        int len = 0;
        if (x.length > y.length) {
            len = y.length;
        } else {
            if (((y.length * 0.6) > x.length)) {
                //return -1;
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
