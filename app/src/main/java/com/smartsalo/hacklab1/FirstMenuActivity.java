package com.smartsalo.hacklab1;


import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
// keys remote
//s = network
//d = camera
//x = camera side outter button
//w= neywork side ou
//q = network side middle
//z= camera side middle

/**
 * full-screen activity that enables user to navigate in application.
 */
public class FirstMenuActivity extends AppCompatActivity {

    private Button mNavigationButton1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firstmenu);
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

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // navigateToCameraPreview();

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                navigateToCameraPreview();
                return true;
            case KeyEvent.KEYCODE_S:
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