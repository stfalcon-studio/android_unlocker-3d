package com.stfalcon.unlocker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Comparison {
    private final static double kFilteringFactor = 0.2;

    /**
     * Фильтер убирает низкие частоты
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double[] lowFilter(double x, double y, double z) {
        double[] acceleration = new double[3];
        acceleration[0] = x * kFilteringFactor + acceleration[0] * (1.0 - kFilteringFactor);
        x = x - acceleration[0];
        acceleration[0] = x;

        acceleration[1] = y * kFilteringFactor + acceleration[1] * (1.0 - kFilteringFactor);
        y = y - acceleration[1];
        acceleration[1] = y;

        acceleration[2] = z * kFilteringFactor + acceleration[2] * (1.0 - kFilteringFactor);
        z = z - acceleration[2];
        acceleration[2] = z;

        return acceleration;
    }

    /**
     * Обрезает начало или конец массива если значения означают бездействие пользователя
     *
     * @param arrayX
     * @param arrayY
     * @return
     */
    public static List<double[]> prepareArrays(double[] arrayX, double[] arrayY) {
        try {
            double offset = 0.00009;
            int len;
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

    /**
     * Сравнение графиков по методу Пирсона
     *
     * @param x
     * @param y
     * @return
     */
    public static double pirsonCompare(double[] x, double[] y) {
        int len;
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
