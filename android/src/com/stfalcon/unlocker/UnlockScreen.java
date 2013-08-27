package com.stfalcon.unlocker;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 8/2/13.
 */
public class UnlockScreen extends Activity implements SensorEventListener, View.OnClickListener {
    SharedPreferences sPref;
    boolean isPressed = false;
    long startTime;
    ArrayList<double[]> accDataList = new ArrayList<double[]>();
    ArrayList<double[]> gyrDataList = new ArrayList<double[]>();
    ArrayList<double[]> filterDataList = new ArrayList<double[]>();
    boolean isSensorOn = false;
    boolean isUnlockScreen = true;
    GraphViewSeries pitchsaveDataSeries, rollsaveDataSeries;
    LinearLayout layout;
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private View rl_unlock_screen;
    private View ll_move_to_unlock;
    private View ll_gesture_not_correct;
    private Context context;
    Drawable wallpaperDrawable;
    private Boolean isStartRecord = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        listener = this;
        context = this;
        UnlockApp.keyguardLock.reenableKeyguard();
        boolean isSave = UnlockApp.sPref.getBoolean("isSave", false);
        if (!isSave) {
            finish();
        }
        startService(new Intent(this, LockService.class));
        initView();
    }

    private void initView() {
        Window wind = getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 /*       View main = getWindow().getDecorView().findViewById(android.R.id.content);
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ll_gesture_not_correct = findViewById(R.id.ll_gesture_not_correct);
        ll_gesture_not_correct.setOnClickListener(this);
        ll_move_to_unlock = findViewById(R.id.ll_move_to_unlock);
        rl_unlock_screen = findViewById(R.id.rl_unlock_screen);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        wallpaperDrawable = wallpaperManager.peekFastDrawable();
        viewToUnlockScreen();
        View ll_move_to_unlock = findViewById(R.id.ll_move_to_unlock);
        ll_move_to_unlock.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        isSensorOn = false;
   /*     new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
                startTime = System.currentTimeMillis();
                accDataList.clear();
                gyrDataList.clear();
                filterDataList.clear();
                isSensorOn = true;
                isPressed = true;
            }
        }, 1000);*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    private void viewToUnlockScreen() {

        View rl_unlock_screen = findViewById(R.id.rl_unlock_screen);
        rl_unlock_screen.setBackground(wallpaperDrawable);
        ll_move_to_unlock.setVisibility(View.VISIBLE);
        ll_gesture_not_correct.setVisibility(View.GONE);
    }

    private void viewToGestureNotCorrect() {
        rl_unlock_screen.setBackground(getResources().getDrawable(R.drawable.bg_red));
        ll_move_to_unlock.setVisibility((View.GONE));
        ll_gesture_not_correct.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) {
            if (isUnlockScreen) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_GYROSCOPE:
                        double mGyr = event.values[0] + event.values[1] + event.values[2];
                        boolean isMove = mGyr > 0.4 || mGyr < -0.4;
                        if (isMove && !isSensorOn) {
                            startTime = System.currentTimeMillis();
                            accDataList.clear();
                            gyrDataList.clear();
                            filterDataList.clear();
                            isSensorOn = true;
                            isPressed = true;
                        }
                        boolean isStop = mGyr < 0.003 && mGyr > -0.003;
                        if (isStop && isSensorOn) {
                            isSensorOn = false;
                            onFinishSensorListen();
                        }
                        break;
                }
                if (isSensorOn) {
                    switch (event.sensor.getType()) {
                        case Sensor.TYPE_ACCELEROMETER:
                            double[] accData = Comparison.lowFilter(event.values[0], event.values[1], event.values[2]);
                            accDataList.add(accData);
                            break;
                        case Sensor.TYPE_GYROSCOPE:
                            double[] gyrData = Comparison.lowFilter(event.values[0], event.values[1], event.values[2]);
                            gyrDataList.add(gyrData);
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onFinishSensorListen() {
        int len;
        if (accDataList.size() > gyrDataList.size()) len = gyrDataList.size();
        else len = accDataList.size();
        for (int i = 0; i < len; i++) {
            filterDataList.add(UnlockApp.complementaryFilter(accDataList.get(i), gyrDataList.get(i)));
        }

        GraphView.GraphViewData[] pitchGraphViewData = new GraphView.GraphViewData[filterDataList.size()];
        for (int i = 0; i < filterDataList.size(); i++) {
            pitchGraphViewData[i] = new GraphView.GraphViewData(i, filterDataList.get(i)[0]);
        }
        GraphView.GraphViewData[] rollGraphViewData = new GraphView.GraphViewData[filterDataList.size()];
        for (int i = 0; i < filterDataList.size(); i++) {
            rollGraphViewData[i] = new GraphView.GraphViewData(i, filterDataList.get(i)[1]);
        }

        GraphViewSeries pitchDataSeries = new GraphViewSeries("pitch", new GraphViewSeries.GraphViewSeriesStyle(Color.GREEN, 5), pitchGraphViewData);
        GraphViewSeries rollDataSeries = new GraphViewSeries("roll", new GraphViewSeries.GraphViewSeriesStyle(Color.BLUE, 5), rollGraphViewData);

        double[] pitch = UnlockApp.loadArrays().get(0);
        double[] roll = UnlockApp.loadArrays().get(1);
        GraphView.GraphViewData[] pitchGraphViewsaveData = new GraphView.GraphViewData[pitch.length];
        for (int i = 0; i < pitch.length; i++) {
            pitchGraphViewsaveData[i] = new GraphView.GraphViewData(i, pitch[i]);
        }
        GraphView.GraphViewData[] rollGraphViewsaveData = new GraphView.GraphViewData[roll.length];
        for (int i = 0; i < roll.length; i++) {
            rollGraphViewsaveData[i] = new GraphView.GraphViewData(i, roll[i]);
        }
        pitchsaveDataSeries = new GraphViewSeries("save pitch", new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 1), pitchGraphViewsaveData);
        rollsaveDataSeries = new GraphViewSeries("save roll", new GraphViewSeries.GraphViewSeriesStyle(Color.YELLOW, 1), rollGraphViewsaveData);


        GraphView graphView = new LineGraphView(
                this // context
                , "SAVED GESTURE" // heading
        );
        graphView.addSeries(pitchDataSeries); // data
        graphView.addSeries(rollDataSeries); // data

        graphView.addSeries(pitchsaveDataSeries);
        graphView.addSeries(rollsaveDataSeries);
        Log.v("LOGER", filterDataList.size() + " SIZE");
        double[] new_pitch = new double[filterDataList.size()];
        double[] save_pitch = pitch.clone();
        double[] new_roll = new double[filterDataList.size()];
        double[] save_roll = roll.clone();
        for (int i = 0; i < filterDataList.size(); i++) {
            new_pitch[i] = filterDataList.get(i)[0];
        }
        for (int i = 0; i < filterDataList.size(); i++) {
            new_roll[i] = filterDataList.get(i)[1];
        }
        List<double[]> pList = Comparison.prepareArrays(new_pitch, new_roll);
        if (pList == null) {
            return;
        }
        new_pitch = pList.get(0);
        new_roll = pList.get(1);
        Log.v("LOGER", "new size " + new_pitch.length + "save size " + save_pitch.length);
        Log.v("LOGER", "new size " + new_roll.length + "save size " + save_roll.length);
        double xPirsonKoef = Comparison.pirsonCompare(new_pitch, save_pitch);
        double yPirsonKoef = Comparison.pirsonCompare(new_roll, save_roll);
        Log.v("LOGER", "XXX" + xPirsonKoef);
        Log.v("LOGER", "YYY" + yPirsonKoef);
        UnlockApp.FACTOR factor = UnlockApp.getInstance().getFactors();
        Log.v("LOGER", "FACTOR" + factor.getFactor());
        boolean unlock = (xPirsonKoef + yPirsonKoef >= factor.getFactor())
                && ((xPirsonKoef > factor.getPitchFactor() && yPirsonKoef > factor.getRollFactor())
                || (yPirsonKoef > factor.getPitchFactor() && xPirsonKoef > factor.getRollFactor()));

        if (!unlock) {
            boolean isConf = UnlockApp.sPref.getBoolean("isConfirm", false);
            if (isConf) {
                save_pitch = UnlockApp.loadConfArrays().get(0);
                save_roll = UnlockApp.loadConfArrays().get(1);
                xPirsonKoef = Comparison.pirsonCompare(new_pitch, save_pitch);
                yPirsonKoef = Comparison.pirsonCompare(new_roll, save_roll);
                Log.v("LOGER", "1XXX" + xPirsonKoef);
                Log.v("LOGER", "1YYY" + yPirsonKoef);
                factor = UnlockApp.getInstance().getFactors();
                Log.v("LOGER", "FACTOR" + factor.getFactor());
                unlock = (xPirsonKoef + yPirsonKoef >= factor.getFactor())
                        && ((xPirsonKoef > factor.getPitchFactor() && yPirsonKoef > factor.getRollFactor())
                        || (yPirsonKoef > factor.getPitchFactor() && xPirsonKoef > factor.getRollFactor()));
            }
        }
        int proc = (int) (((xPirsonKoef + yPirsonKoef + (double) 2)) / 0.04d);
        if (unlock) {
            UnlockApp.keyguardLock.disableKeyguard();
            finish();
        } else {
            viewToGestureNotCorrect();
            isUnlockScreen = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_gesture_not_correct:
                viewToUnlockScreen();
                isUnlockScreen = true;
                break;
            /*case R.id.ll_move_to_unlock:
                Log.v("LOGER", "CLICK");
                if (!isStartRecord) {
                    startTime = System.currentTimeMillis();
                    accDataList.clear();
                    gyrDataList.clear();
                    filterDataList.clear();
                    isSensorOn = true;
                    isPressed = true;
                    isStartRecord = true;
                } else {
                    isStartRecord = false;
                    isSensorOn = false;
                    onFinishSensorListen();
                }
                break;*/
        }
    }
}
