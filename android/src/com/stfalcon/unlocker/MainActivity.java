package com.stfalcon.unlocker;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

    SensorManager sensorManager = null;

    //for accelerometer values
    TextView outputX;
    TextView outputY;
    TextView outputZ;

    //for orientation values
    TextView outputX2;
    TextView outputY2;
    TextView outputZ2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.activity_main);

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
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
    }
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            switch (event.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    outputX.setText("x:"+Float.toString(event.values[0]));
                    outputY.setText("y:"+Float.toString(event.values[1]));
                    outputZ.setText("z:"+Float.toString(event.values[2]));
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    outputX2.setText("x:"+Float.toString(event.values[0]));
                    outputY2.setText("y:"+Float.toString(event.values[1]));
                    outputZ2.setText("z:"+Float.toString(event.values[2]));
                    break;

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

