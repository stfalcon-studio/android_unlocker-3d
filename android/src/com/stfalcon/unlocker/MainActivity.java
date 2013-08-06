package com.stfalcon.unlocker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends Activity implements SensorEventListener {

    public static String MY_PREF = "my_pref";
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
    //ArrayList<double[]> saveDataList = new ArrayList<double[]>();
    ComponentName compName;
    private TextView tv_time;
    private TextView tv_new_time;
    private Activity context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        UnlockApp.sPref = getSharedPreferences(MY_PREF, MODE_PRIVATE);

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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSensorOn) {
                    startTime = System.currentTimeMillis();
                    accDataList.clear();
                    gyrDataList.clear();
                    filterDataList.clear();
                    isSensorOn = true;
                    isPressed = true;
                    button.setText("STOP");
                } else {
                    isSensorOn = false;
                    isPressed = false;
                    SharedPreferences.Editor editor = UnlockApp.sPref.edit();
                    editor.putBoolean("isSave", false);
                    editor.commit();
                    button.setText("Record gesture");
                    onFinishSensorListen();
                }
            }
        });

        compar = (Button) findViewById(R.id.button2);
        compar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSensorOn) {
                    startTime = System.currentTimeMillis();
                    accDataList.clear();
                    gyrDataList.clear();
                    filterDataList.clear();
                    isSensorOn = true;
                    isPressed = true;
                    compar.setText("STOP");
                } else {
                    isSensorOn = false;
                    isPressed = false;
                    onFinishSensorListen();
                    compar.setText("Compare gesture");
                }
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
            {
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
                            double mGyr = event.values[0] + event.values[1] + event.values[2];
                            outputZ2.setText("Gyr:" + Double.toString(mGyr));
                            gyrDataList.add(gyrData);
                            break;
                    }
                }
            }
        }
    }

    public void onFinishSensorListen() {

        layout.removeAllViews();
        int len = 0;
        if (accDataList.size() > gyrDataList.size()) len = gyrDataList.size();
        else len = accDataList.size();
        for (int i = 0; i < len; i++) {
            filterDataList.add(UnlockApp.complementaryFilter(accDataList.get(i), gyrDataList.get(i)));
        }

        double[] pArr = new double[filterDataList.size()];
        for (int i = 0; i < filterDataList.size(); i++) {
            pArr[i] = filterDataList.get(i)[0];
        }
        double[] rArr = new double[filterDataList.size()];
        for (int i = 0; i < filterDataList.size(); i++) {
            rArr[i] = filterDataList.get(i)[1];
        }

        pArr = Comparison.prepareArray(pArr);
        rArr = Comparison.prepareArray(rArr);
        if (pArr == null || rArr == null) {
            Toast.makeText(context, "Повторите еще раз...", Toast.LENGTH_SHORT).show();
            return;
        }
        GraphView.GraphViewData[] pitchGraphViewData = new GraphView.GraphViewData[pArr.length];
        for (int i = 0; i < pArr.length; i++) {
            pitchGraphViewData[i] = new GraphView.GraphViewData(i, pArr[i]);
        }
        GraphView.GraphViewData[] rollGraphViewData = new GraphView.GraphViewData[rArr.length];
        for (int i = 0; i < rArr.length; i++) {
            rollGraphViewData[i] = new GraphView.GraphViewData(i, rArr[i]);
        }

        GraphViewSeries pitchDataSeries = new GraphViewSeries("pitch", new GraphViewSeries.GraphViewSeriesStyle(Color.BLACK, 4), pitchGraphViewData);
        GraphViewSeries rollDataSeries = new GraphViewSeries("roll", new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 4), rollGraphViewData);

        boolean isSave = UnlockApp.sPref.getBoolean("isSave", false);
        if (isSave) {
            double savePitch[] = UnlockApp.loadArrays().get(0);
            double saveRoll[] = UnlockApp.loadArrays().get(1);
            GraphView.GraphViewData[] pitchGraphViewsaveData = new GraphView.GraphViewData[savePitch.length];
            for (int i = 0; i < savePitch.length; i++) {
                pitchGraphViewsaveData[i] = new GraphView.GraphViewData(i, savePitch[i]);
            }
            GraphView.GraphViewData[] rollGraphViewsaveData = new GraphView.GraphViewData[saveRoll.length];
            for (int i = 0; i < saveRoll.length; i++) {
                rollGraphViewsaveData[i] = new GraphView.GraphViewData(i, saveRoll[i]);
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
        if (isSave) {
            double savePitch[] = UnlockApp.loadArrays().get(0);
            double saveRoll[] = UnlockApp.loadArrays().get(1);
            graphView.addSeries(pitchsaveDataSeries);
            graphView.addSeries(rollsaveDataSeries);
            double[] x = pArr;
            double[] x1 = savePitch;
            double[] y = rArr;
            double[] y1 = saveRoll;

            double xPirsonKoef = Comparison.pirsonCompare(x, x1);
            double yPirsonKoef = Comparison.pirsonCompare(y, y1);
            boolean unlock = (xPirsonKoef + yPirsonKoef >= 0.6);// && xPirsonKoef > 0.2 && yPirsonKoef > 0.2;
            proc.setText("Unlock: " + unlock + " " + "compare Pitch = " +
                    new DecimalFormat("#.##").format(xPirsonKoef) + " " +
                    "Roll = " + new DecimalFormat("#.##").format(yPirsonKoef));
            tv_new_time.setText("Time: " + new DecimalFormat("#.##").format((System.currentTimeMillis() - startTime) / 1000));
        }
        layout.addView(graphView);
        if (!isSave) {
            UnlockApp.saveArrays(pArr, rArr);
            tv_time.setText("Time: " + new DecimalFormat("#.##").format((System.currentTimeMillis() - startTime) / 1000));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}

