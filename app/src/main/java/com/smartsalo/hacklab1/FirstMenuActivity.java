package com.smartsalo.hacklab1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * full-screen activity that enables user to navigate in application.
 */
public class FirstMenuActivity extends AppCompatActivity {

    private Button mNavigationButton1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mNavigationButton1 = findViewById(R.id.button4);

        // Set up the user interaction to navigate UI.
        mNavigationButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToCameraPreview();
            }
        });

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {


            final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //UUID for serial connection
            String mac = "98:D3:32:30:75:1B"; //my laptop's mac adress
            BluetoothDevice device = adapter.getRemoteDevice(mac); //get remote device by mac, we assume these two devices are already paired


            // Get a BluetoothSocket to connect with the given BluetoothDevice
            BluetoothSocket socket = null;
            OutputStream out = null;
            try {
                socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            } catch (IOException e) {
            }

            try {
                socket.connect();
                out = socket.getOutputStream();
                //now you can use out to send output via out.write
                out.write(69);
            } catch (IOException e) {
            }
        }  }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                navigateToCameraPreview();
                return true;
            case KeyEvent.KEYCODE_A:
                navigateToSensorPreview();
                return true;

            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void navigateToCameraPreview() {
        Intent intent = new Intent(this, CameraPreviewActivity.class);
        startActivity(intent);
    }
    private void navigateToSensorPreview() {
        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }
}