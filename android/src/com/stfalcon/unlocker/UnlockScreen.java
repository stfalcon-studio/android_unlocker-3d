package com.stfalcon.unlocker;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 8/2/13.
 */
public class UnlockScreen extends Activity implements SensorEventListener, View.OnClickListener {

    final ArrayList<double[]> accDataList = new ArrayList<double[]>();
    final ArrayList<double[]> gyrDataList = new ArrayList<double[]>();
    final ArrayList<double[]> filterDataList = new ArrayList<double[]>();
    boolean isSensorOn = false;
    boolean isUnlockScreen = true;
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private View rl_unlock_screen;
    private View ll_move_to_unlock;
    private View ll_gesture_not_correct;
    private Context context;
    Drawable wallpaperDrawable;
    private Typeface robotoThin;

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

    private String getDateString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMMM d");
        return simpleDateFormat.format(new Date()).toUpperCase();
    }

    private void initView() {
        robotoThin = Typeface.createFromAsset(this.getAssets(), "Roboto-Thin.ttf");
        DigitalClockHM digitalClockHM = (DigitalClockHM) findViewById(R.id.clock);
        digitalClockHM.setTypeface(robotoThin);
        Window wind = getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ll_gesture_not_correct = findViewById(R.id.ll_gesture_not_correct);
        ll_gesture_not_correct.setOnClickListener(this);
        TextView tv_gesture_not_correct = (TextView) findViewById(R.id.tv_unlock_gesture_not_correct);
        tv_gesture_not_correct.setTypeface(robotoThin);
        TextView tv_date = (TextView) findViewById(R.id.tv_date);
        tv_date.setText(getDateString());
        ll_move_to_unlock = findViewById(R.id.ll_move_to_unlock);
        rl_unlock_screen = findViewById(R.id.rl_unlock_screen);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        wallpaperDrawable = wallpaperManager.peekFastDrawable();
        View ll_move_to_unlock = findViewById(R.id.ll_move_to_unlock);
        ll_move_to_unlock.setOnClickListener(this);
        viewToUnlockScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewToUnlockScreen();
        isUnlockScreen = true;
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        isSensorOn = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    private void viewToUnlockScreen() {
        View rl_unlock_screen = findViewById(R.id.rl_unlock_screen);
        rl_unlock_screen.setBackgroundDrawable(wallpaperDrawable);
        rl_unlock_screen.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_move_to_unlock.setVisibility(View.VISIBLE);
        ll_gesture_not_correct.setVisibility(View.GONE);
    }

    private void viewToGestureNotCorrect() {
        rl_unlock_screen.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        rl_unlock_screen.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_red));
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
                            accDataList.clear();
                            gyrDataList.clear();
                            filterDataList.clear();
                            isSensorOn = true;
                        }
                        boolean isStop = mGyr < 0.0025 && mGyr > -0.0025;
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
        double[] pitch = UnlockApp.loadArrays().get(0);
        double[] roll = UnlockApp.loadArrays().get(1);
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
        double xPirsonKoef = Comparison.pirsonCompare(new_pitch, save_pitch);
        double yPirsonKoef = Comparison.pirsonCompare(new_roll, save_roll);
        UnlockApp.FACTOR factor = UnlockApp.getInstance().getFactors();
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
                factor = UnlockApp.getInstance().getFactors();
                unlock = (xPirsonKoef + yPirsonKoef >= factor.getFactor())
                        && ((xPirsonKoef > factor.getPitchFactor() && yPirsonKoef > factor.getRollFactor())
                        || (yPirsonKoef > factor.getPitchFactor() && xPirsonKoef > factor.getRollFactor()));
            }
        }
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
        }
    }
}
