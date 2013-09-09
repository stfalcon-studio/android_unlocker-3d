package com.stfalcon.unlocker;

import android.app.Application;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by anton on 8/5/13.
 */
public class UnlockApp extends Application {

    private static final double a = 0.1;
    public static final String IS_ON = "ison";
    public static SharedPreferences sPref, prefs;
    public Context context;
    public static final String MY_PREF = "mupref";
    public static KeyguardManager.KeyguardLock keyguardLock;
    private static final double GYROSCOPE_SENSITIVITY = 65.536;
    private static final double dt = 0.005;
    private static UnlockApp self;
    private KeyguardManager keyguardManager;

    /**
     * Сохраняет состояние актиности приложения
     *
     * @param state
     */
    public void saveActivState(Boolean state) {

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(IS_ON, String.valueOf(state));
        editor.commit();
        String FILENAME = "active";
        String string = String.valueOf(state);
        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param dpitch
     * @param droll
     */
    public static void saveArrays(double[] dpitch, double[] droll) {

        SharedPreferences.Editor editor = sPref.edit();
        String[] pitch = new String[dpitch.length];
        for (int i = 0; i < dpitch.length; i++) {
            String s = String.valueOf(dpitch[i]);
            pitch[i] = s;
        }
        editor.putInt("pitch_size", pitch.length);
        for (int i = 0; i < pitch.length; i++) {
            editor.putString("pitch_" + i, pitch[i]);
        }

        String[] roll = new String[droll.length];
        for (int i = 0; i < droll.length; i++) {
            String s = String.valueOf(droll[i]);
            roll[i] = s;
        }
        editor.putInt("roll_size", roll.length);
        for (int i = 0; i < roll.length; i++) {
            editor.putString("roll_" + i, roll[i]);
        }
        editor.putBoolean("isSave", true);
        editor.commit();
    }

    /**
     * @param dpitch
     * @param droll
     */
    public static void confArrays(double[] dpitch, double[] droll) {

        SharedPreferences.Editor editor = sPref.edit();
        String[] pitch = new String[dpitch.length];
        for (int i = 0; i < dpitch.length; i++) {
            String s = String.valueOf(dpitch[i]);
            pitch[i] = s;
        }
        editor.putInt("conf_pitch_size", pitch.length);
        for (int i = 0; i < pitch.length; i++) {
            editor.putString("conf_pitch_" + i, pitch[i]);
        }

        String[] roll = new String[droll.length];
        for (int i = 0; i < droll.length; i++) {
            String s = String.valueOf(droll[i]);
            roll[i] = s;
        }
        editor.putInt("conf_roll_size", roll.length);
        for (int i = 0; i < roll.length; i++) {
            editor.putString("conf_roll_" + i, roll[i]);
        }
        editor.putBoolean("isConfirm", true);
        editor.commit();
    }

    /**
     * @return
     */
    public static ArrayList<double[]> loadArrays() {
        int pitchSize = sPref.getInt("pitch_size", 0);
        double[] pitch = new double[pitchSize];
        for (int i = 0; i < pitchSize; i++) {
            pitch[i] = Double.valueOf(sPref.getString("pitch_" + i, "0"));
        }

        int rollSize = sPref.getInt("roll_size", 0);
        double[] roll = new double[rollSize];
        for (int i = 0; i < rollSize; i++) {
            roll[i] = Double.valueOf(sPref.getString("roll_" + i, "0"));
        }

        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        arrayList.add(pitch);
        arrayList.add(roll);

        return arrayList;
    }

    /**
     * @return
     */
    public static ArrayList<double[]> loadConfArrays() {
        int pitchSize = sPref.getInt("conf_pitch_size", 0);
        double[] pitch = new double[pitchSize];
        for (int i = 0; i < pitchSize; i++) {
            pitch[i] = Double.valueOf(sPref.getString("conf_pitch_" + i, "0"));
        }

        int rollSize = sPref.getInt("conf_roll_size", 0);
        double[] roll = new double[rollSize];
        for (int i = 0; i < rollSize; i++) {
            roll[i] = Double.valueOf(sPref.getString("conf_roll_" + i, "0"));
        }

        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        arrayList.add(pitch);
        arrayList.add(roll);
        return arrayList;
    }

    /**
     * @param accData
     * @param gyrData
     * @return
     */
    public static double[] complementaryFilter(double accData[], double gyrData[]) {
        double pitchAcc, rollAcc;
        double pitch = 0;
        double roll = 0;
        double[] result = new double[2];

        // Integrate the gyroscope data -> int(angularSpeed) = angle
        pitch += ((float) gyrData[0] / GYROSCOPE_SENSITIVITY) * dt; // Angle around the X-axis
        roll -= ((float) gyrData[1] / GYROSCOPE_SENSITIVITY) * dt;    // Angle around the Y-axis

        // Compensate for drift with accelerometer data if !bullshit
        // Sensitivity = -2 to 2 G at 16Bit -> 2G = 32768 && 0.5G = 8192
        double forceMagnitudeApprox = Math.abs(accData[0]) + Math.abs(accData[1]) + Math.abs(accData[2]);
        if (forceMagnitudeApprox > 8192 && forceMagnitudeApprox < 32768) {
            // Turning around the X axis results in a vector on the Y-axis
            pitchAcc = Math.atan2((float) accData[1], (float) accData[2]) * 180 / Math.PI;
            pitch = pitch * 0.98 + pitchAcc * 0.02;
            // Turning around the Y axis results in a vector on the X-axis
            rollAcc = Math.atan2((float) accData[0], (float) accData[2]) * 180 / Math.PI;
            roll = roll * 0.98 + rollAcc * 0.02;
        }
        result[0] = pitch;
        result[1] = roll;
        return result;
    }

    /**
     * Уберает низкие частоты
     *
     * @param acceleration
     * @return
     */
    public static double lowPassFilterAcc(double acceleration) {
        double filteredValues = 0;
        filteredValues = acceleration * a + filteredValues * (1.0d - a);
        return filteredValues;
    }


    public static UnlockApp getInstance() {
        return self;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        sPref = getSharedPreferences(MY_PREF, 0);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        keyguardManager = (KeyguardManager) getSystemService(Service.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("Keyguard_Lock");
        self = this;
    }

    public FACTOR getFactors() {
        return new FACTOR(0.5, 0.3, 0.2);
    }

    public class FACTOR {
        final double factor;
        final double pitch_factor;
        final double roll_factor;

        public FACTOR(double factor, double pitch_factor, double roll_factor) {
            this.factor = factor;
            this.pitch_factor = pitch_factor;
            this.roll_factor = roll_factor;
        }

        public double getFactor() {
            return factor;
        }

        public double getPitchFactor() {
            return pitch_factor;
        }

        public double getRollFactor() {
            return roll_factor;
        }
    }
}
