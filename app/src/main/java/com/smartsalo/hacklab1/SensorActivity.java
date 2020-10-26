package com.smartsalo.hacklab1;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor mLight;
    private Sensor mAccelerometer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        /* Hacklab device has following sensors
         MPL Gyroscope
 MPL Gyroscope - Wakeup
 MPL Raw Gyroscope
 MPL Raw Gyroscope - Wakeup
 MPL Accelerometer
 MPL Accelerometer - Wakeup
 MPL Magnetic Field
 MPL Raw Magnetic Field
 MPL Orientation
 MPL Rotation Vector
 MPL Game Rotation Vector
MPL Linear Acceleration
MPL Gravity
MPL Step Detector
MPL Step Counter
MPL Geomagnetic Rotation Vector
Light Sensor
Pressure
         */
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_Q:
                navigateToFirstMenu();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void navigateToFirstMenu() {
        Intent intent = new Intent(this, FirstMenuActivity.class);
        startActivity(intent);
    }
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        //Accelelator
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float alpha = (float) 0.80;

            float[] gravity = {0, 0, 0};
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            float[] linear_acceleration = {0, 0, 0};
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            int val1 = (int) (linear_acceleration[0] * 1000 + 1000);
            int val2 = (int) (linear_acceleration[1] * 1000 + 1000);
            int val3 = (int) (linear_acceleration[2] * 1000 + 1000);
            /*ProgressBar pr1 = findViewById(R.id.progressBar);
            ProgressBar pr2 = findViewById(R.id.progressBar2);
            ProgressBar pr3 = findViewById(R.id.progressBar3);*/
           TextView tv = findViewById(R.id.textViewP);
            tv.setText("Accelelator:  " + val1 + ", " + val2 + ", " + val3);
        }

        if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            int val1 = (int) (event.values[0] * 10 + 10);
            TextView tv = findViewById(R.id.textView2);
            tv.setText("Light:  " + val1);
        }
       /*ProgressBar pr1 = findViewById(R.id.progressBar);
        ProgressBar pr2 = findViewById(R.id.progressBar2);
        ProgressBar pr3 = findViewById(R.id.progressBar3);
        if(val1> pr1.getMax()) {pr1.setMax(val1);}
        pr1.setProgress(val1);
        if(val2> pr2.getMax()) {pr2.setMax(val2);}
        pr2.setProgress(val2);
        if(val3> pr3.getMax()) {pr3.setMax(val3);}
        pr3.setProgress(val3);
*/

    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}