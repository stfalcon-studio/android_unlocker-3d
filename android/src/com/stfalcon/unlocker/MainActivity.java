package com.stfalcon.unlocker;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

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
    Button button;
    double startTime;
    boolean isSensorOn = false;
    ArrayList<double[]> accDataList = new ArrayList<double[]>();
    ArrayList<double[]> gyrDataList = new ArrayList<double[]>();
    ArrayList<double[]> filterDataList = new ArrayList<double[]>();

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
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = System.currentTimeMillis();
                accDataList.clear();
                gyrDataList.clear();
                filterDataList.clear();
                isSensorOn = true;
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


    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorManager.SENSOR_DELAY_FASTEST);
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
                if ((System.currentTimeMillis() - startTime) > 1000) {
                    isSensorOn = false;
                    onFinishSensorListen();
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


        GraphViewSeries pitchDataSeries = new GraphViewSeries("pitch", new GraphViewSeries.GraphViewSeriesStyle(Color.BLACK, 3), pitchGraphViewData);
        GraphViewSeries rollDataSeries = new GraphViewSeries("roll", new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 3), rollGraphViewData);

        GraphView graphView = new LineGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView.addSeries(pitchDataSeries); // data
        graphView.addSeries(rollDataSeries); // data
        layout.addView(graphView);
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

