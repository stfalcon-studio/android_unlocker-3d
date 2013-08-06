package com.stfalcon.unlocker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static List<double[]> prepareArrays(double[] arrayX, double[] arrayY) {
        try {
            double offset = 0.00006;
            int len = 0;
            if (arrayX.length > arrayY.length) {
                len = arrayY.length;
            } else {
                len = arrayX.length;
            }
            int i;
            for (i = 0; i < len; i++) {
                if ((arrayX[i] < offset && arrayX[i] > (-offset)) && (arrayY[i] < offset && arrayY[i] > (-offset))) {
                } else {
                    break;
                }
            }
            double[] pArrayX = Arrays.copyOfRange(arrayX, i, arrayX.length);
            double[] pArrayY = Arrays.copyOfRange(arrayY, i, arrayY.length);
            for (i = len - 1; i >= 0; i--) {
                if ((arrayX[i] < offset && arrayX[i] > (-offset)) && (arrayY[i] < offset && arrayY[i] > (-offset))) {
                } else {
                    break;
                }
            }
            pArrayX = Arrays.copyOfRange(pArrayX, 0, i);
            pArrayY = Arrays.copyOfRange(pArrayY, 0, i);
            ArrayList<double[]> doubles = new ArrayList<double[]>();
            doubles.add(pArrayX);
            doubles.add(pArrayY);
            return doubles;
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
