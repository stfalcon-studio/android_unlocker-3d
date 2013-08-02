package com.stfalcon.unlocker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
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


public class MainActivity extends Activity implements SensorEventListener {

    private static double GYROSCOPE_SENSITIVITY = 65.536;
    private static double ACCELEROMETER_SENSITIVITY = 8192.0;
    private static double dt = 0.01;
    SensorManager sensorManager = null;
    //for accelerometer values
    TextView outputX;
    TextView outputY;
    TextView outputZ;
    //for orientation values
    TextView outputX2;
    TextView outputY2;
    TextView outputZ2;
    LinearLayout layout;
    Button button, compar;
    TextView proc;
    GraphViewSeries pitchsaveDataSeries, rollsaveDataSeries;
    double startTime;
    boolean isSensorOn = false;
    boolean isPressed = false;
    ArrayList<double[]> accDataList = new ArrayList<double[]>();
    ArrayList<double[]> gyrDataList = new ArrayList<double[]>();
    ArrayList<double[]> filterDataList = new ArrayList<double[]>();
    ArrayList<double[]> saveDataList = new ArrayList<double[]>();
    ComponentName compName;
    private TextView tv_time;
    private TextView tv_new_time;
    private Activity context;

    public static String arrayListToString(ArrayList<double[]> dataList) {
        String s = "";
        for (int i = 0; i < dataList.size(); i++) {
            s += " [ ";
            for (int j = 0; j < dataList.get(i).length; j++) {
                s += " " + dataList.get(i)[j];
            }
            s += " ] " + "\n";
        }
        return s;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, LockService.class));
        proc = (TextView) findViewById(R.id.textView6);
        Window wind = getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        View main = getWindow().getDecorView().findViewById(android.R.id.content);
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        //wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        compName = new ComponentName(this, MyAdmin.class);

        button = (Button) findViewById(R.id.button);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isPressed) {
                        saveDataList.clear();
                        startTime = System.currentTimeMillis();
                        accDataList.clear();
                        gyrDataList.clear();
                        filterDataList.clear();
                        isSensorOn = true;
                        isPressed = true;
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isSensorOn = false;
                    isPressed = false;
                    onFinishSensorListen();
                }
                return false;
            }
        });
        compar = (Button) findViewById(R.id.button2);
        compar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isPressed) {
                        startTime = System.currentTimeMillis();
                        accDataList.clear();
                        gyrDataList.clear();
                        filterDataList.clear();
                        isSensorOn = true;
                        isPressed = true;
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isSensorOn = false;
                    isPressed = false;
                    onFinishSensorListen();
                }
                return false;
            }
        });


        compar = (Button) findViewById(R.id.button2);
        compar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        layout = (LinearLayout) findViewById(R.id.ll_graph);
        //just some textviews, for data output
        outputX = (TextView) findViewById(R.id.textView);
        outputY = (TextView) findViewById(R.id.textView1);
        outputZ = (TextView) findViewById(R.id.textView2);

        outputX2 = (TextView) findViewById(R.id.textView3);
        outputY2 = (TextView) findViewById(R.id.textView4);
        outputZ2 = (TextView) findViewById(R.id.textView5);

        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_new_time = (TextView) findViewById(R.id.tv_new_time);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (isSensorOn) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        outputX.setText("x:" + Float.toString(event.values[0]));
                        outputY.setText("y:" + Float.toString(event.values[1]));
                        outputZ.setText("z:" + Float.toString(event.values[2]));
                        double[] accData = {event.values[0], event.values[1], event.values[2]};
                        accDataList.add(accData);
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        outputX2.setText("x:" + Float.toString(event.values[0]));
                        outputY2.setText("y:" + Float.toString(event.values[1]));
                        outputZ2.setText("z:" + Float.toString(event.values[2]));
                        double[] gyrData = {event.values[0], event.values[1], event.values[2]};
                        gyrDataList.add(gyrData);
                        break;
                }
            }
        }
    }

    public void onFinishSensorListen() {
        /*Log.v("SENSOR", "---SENSOR LISTEN FINISH---");
        Log.v("SENSOR", "---ACCELEROMETER---");
        Log.v("SENSOR", "DATA LENGTH = " + accDataList.size());
        Log.v("SENSOR", "DATA = " + arrayListToString(accDataList));
        Log.v("SENSOR", "---GYROSCOPE---");
        Log.v("SENSOR", "DATA LENGTH = " + gyrDataList.size());
        Log.v("SENSOR", "DATA = " + arrayListToString(gyrDataList));*/
        layout.removeAllViews();
        int len = 0;
        if (accDataList.size() > gyrDataList.size()) len = gyrDataList.size();
        else len = accDataList.size();
        for (int i = 0; i < len; i++) {
            filterDataList.add(complementaryFilter(accDataList.get(i), gyrDataList.get(i)));
        }
      /*  Log.v("SENSOR", "---FILTER---");
        Log.v("SENSOR", "DATA LENGTH = " + filterDataList.size());
        Log.v("SENSOR", "DATA = " + arrayListToString(filterDataList));*/


        GraphView.GraphViewData[] pitchGraphViewData = new GraphView.GraphViewData[filterDataList.size()];
        for (int i = 0; i < filterDataList.size(); i++) {
            pitchGraphViewData[i] = new GraphView.GraphViewData(i, filterDataList.get(i)[0]);
        }
        GraphView.GraphViewData[] rollGraphViewData = new GraphView.GraphViewData[filterDataList.size()];
        for (int i = 0; i < filterDataList.size(); i++) {
            rollGraphViewData[i] = new GraphView.GraphViewData(i, filterDataList.get(i)[1]);
        }

        GraphViewSeries pitchDataSeries = new GraphViewSeries("pitch", new GraphViewSeries.GraphViewSeriesStyle(Color.BLACK, 4), pitchGraphViewData);
        GraphViewSeries rollDataSeries = new GraphViewSeries("roll", new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 4), rollGraphViewData);

        if (saveDataList.size() > 0) {

            GraphView.GraphViewData[] pitchGraphViewsaveData = new GraphView.GraphViewData[saveDataList.size()];
            for (int i = 0; i < saveDataList.size(); i++) {
                pitchGraphViewsaveData[i] = new GraphView.GraphViewData(i, saveDataList.get(i)[0]);
            }
            GraphView.GraphViewData[] rollGraphViewsaveData = new GraphView.GraphViewData[saveDataList.size()];
            for (int i = 0; i < saveDataList.size(); i++) {
                rollGraphViewsaveData[i] = new GraphView.GraphViewData(i, saveDataList.get(i)[1]);
            }
            pitchsaveDataSeries = new GraphViewSeries("pitch1", new GraphViewSeries.GraphViewSeriesStyle(Color.GREEN, 2), pitchGraphViewsaveData);
            rollsaveDataSeries = new GraphViewSeries("roll1", new GraphViewSeries.GraphViewSeriesStyle(Color.YELLOW, 2), rollGraphViewsaveData);
        }

        GraphView graphView = new LineGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView.addSeries(pitchDataSeries); // data
        graphView.addSeries(rollDataSeries); // data
        if (saveDataList.size() > 0) {
            graphView.addSeries(pitchsaveDataSeries);
            graphView.addSeries(rollsaveDataSeries);
            double[] x = new double[filterDataList.size()];
            double[] x1 = new double[saveDataList.size()];
            double[] mass;
            for (int i = 0; i < filterDataList.size(); i++) {
                mass = filterDataList.get(i);
                x[i] = mass[0];
            }
            for (int i = 0; i < saveDataList.size(); i++) {
                mass = saveDataList.get(i);
                x1[i] = mass[0];
            }
            double pirsonKoef = Comparison.pirsonCompare(x, x1);
            boolean unlock = pirsonKoef >= 0.4;
            proc.setText("Unlock: " + unlock + " " + "compare = " + new DecimalFormat("#.##").format(pirsonKoef));
            tv_new_time.setText("Time: " + new DecimalFormat("#.##").format((System.currentTimeMillis() - startTime) / 1000));
            if (unlock) {
                Window wind = getWindow();
                finish();
            }
        }
        layout.addView(graphView);
        if (!(saveDataList.size() > 0)) {
            saveDataList.addAll(filterDataList);
            tv_time.setText("Time: " + new DecimalFormat("#.##").format((System.currentTimeMillis() - startTime) / 1000));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    double[] complementaryFilter(double accData[], double gyrData[]) {
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
}

