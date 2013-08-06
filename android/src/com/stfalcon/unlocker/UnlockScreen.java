package com.stfalcon.unlocker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by user on 8/2/13.
 */
public class UnlockScreen extends Activity implements SensorEventListener {
    SharedPreferences sPref;
    boolean isPressed = false;
    long startTime;
    ArrayList<double[]> accDataList = new ArrayList<double[]>();
    ArrayList<double[]> gyrDataList = new ArrayList<double[]>();
    ArrayList<double[]> filterDataList = new ArrayList<double[]>();
    boolean isSensorOn = false;
    GraphViewSeries pitchsaveDataSeries, rollsaveDataSeries;
    LinearLayout layout;
    private SensorManager sensorManager;
    private TextView tv_compare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        boolean isSave = UnlockApp.sPref.getBoolean("isSave", false);
        if (!isSave) {
            finish();
        }

        UnlockApp.keyguardLock.reenableKeyguard();
        startService(new Intent(this, LockService.class));
        Window wind = getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        View main = getWindow().getDecorView().findViewById(android.R.id.content);
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Button unlock = (Button) findViewById(R.id.button_unlock);
        unlock.requestFocus();
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinishSensorListen();
            }
        });

        layout = (LinearLayout) findViewById(R.id.ll_graph);
        tv_compare = (TextView) findViewById(R.id.tv_compare);

        showSaveGraph();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorManager.SENSOR_DELAY_FASTEST);
        startTime = System.currentTimeMillis();
        accDataList.clear();
        gyrDataList.clear();
        filterDataList.clear();
        isSensorOn = true;
        isPressed = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
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
                        Log.v("LOGER", "Move");
                    }
                    boolean isStop = mGyr < 0.001 && mGyr > -0.001;
                    if (isStop && isSensorOn) {
                        isSensorOn = false;
                        Log.v("LOGER", "STOP");
                        onFinishSensorListen();
                    }
                    break;
            }
            if (isSensorOn) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        double[] accData = {event.values[0], event.values[1], event.values[2]};
                        accDataList.add(accData);
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        double[] gyrData = {event.values[0], event.values[1], event.values[2]};
                        gyrDataList.add(gyrData);
                        break;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onFinishSensorListen() {
        layout.removeAllViews();
        int len = 0;
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

        GraphViewSeries pitchDataSeries = new GraphViewSeries("pitch", new GraphViewSeries.GraphViewSeriesStyle(Color.GREEN, 4), pitchGraphViewData);
        GraphViewSeries rollDataSeries = new GraphViewSeries("roll", new GraphViewSeries.GraphViewSeriesStyle(Color.BLUE, 4), rollGraphViewData);

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
        pitchsaveDataSeries = new GraphViewSeries("pitch1", new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 2), pitchGraphViewsaveData);
        rollsaveDataSeries = new GraphViewSeries("roll1", new GraphViewSeries.GraphViewSeriesStyle(Color.YELLOW, 2), rollGraphViewsaveData);


        GraphView graphView = new LineGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView.addSeries(pitchDataSeries); // data
        graphView.addSeries(rollDataSeries); // data

        graphView.addSeries(pitchsaveDataSeries);
        graphView.addSeries(rollsaveDataSeries);

        double[] x = new double[filterDataList.size()];
        double[] x1 = pitch.clone();
        double[] y = new double[filterDataList.size()];
        double[] y1 = roll.clone();
        for (int i = 0; i < filterDataList.size(); i++) {
            x[i] = filterDataList.get(i)[0];
        }
        for (int i = 0; i < filterDataList.size(); i++) {
            y[i] = filterDataList.get(i)[1];
        }

        double xPirsonKoef = Comparison.pirsonCompare(x, x1);
        double yPirsonKoef = Comparison.pirsonCompare(y, y1);
        Log.v("LOGER", "XXX" + xPirsonKoef);
        Log.v("LOGER", "YYY" + yPirsonKoef);
        boolean unlock = (xPirsonKoef + yPirsonKoef >= 0.6) && xPirsonKoef > 0.2 && yPirsonKoef > 0.2;
        tv_compare.setText("Unlock: " + unlock + " " + "compare = " + new DecimalFormat("#.##").format((xPirsonKoef + yPirsonKoef)));
        if (unlock) {
            UnlockApp.keyguardLock.disableKeyguard();
            finish();
        } else {

        }
        layout.addView(graphView);
    }

    private void showSaveGraph() {
        double[] savePitch = UnlockApp.loadArrays().get(0);
        double[] saveRoll = UnlockApp.loadArrays().get(1);
        GraphView.GraphViewData[] pitchGraphViewsaveData = new GraphView.GraphViewData[savePitch.length];
        for (int i = 0; i < savePitch.length; i++) {
            pitchGraphViewsaveData[i] = new GraphView.GraphViewData(i, savePitch[i]);
        }
        GraphView.GraphViewData[] rollGraphViewsaveData = new GraphView.GraphViewData[saveRoll.length];
        for (int i = 0; i < saveRoll.length; i++) {
            rollGraphViewsaveData[i] = new GraphView.GraphViewData(i, saveRoll[i]);
        }
        pitchsaveDataSeries = new GraphViewSeries("pitch1", new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 2), pitchGraphViewsaveData);
        rollsaveDataSeries = new GraphViewSeries("roll1", new GraphViewSeries.GraphViewSeriesStyle(Color.YELLOW, 2), rollGraphViewsaveData);
        GraphView graphView = new LineGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView.addSeries(pitchsaveDataSeries);
        graphView.addSeries(rollsaveDataSeries);
        layout.addView(graphView);
    }
}
