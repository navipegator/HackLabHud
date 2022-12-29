package com.smartsalo.hacklab1;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.OnImageSavedCallback;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Draws camera preview on the screen, and with buttons, sends the preview or imagecapture to server, or saved to sdcard.
 * Author: Petri Kultanen
 */
public class CameraPreviewActivity extends AppCompatActivity {
    // Permissions to use camera and save pictures.
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    // Taking picture requires executor to do it.
    private Executor executor = Executors.newSingleThreadExecutor();
    // ImageCapture needed by Camera
    private ImageCapture imageCapture;
    // Preview View
    PreviewView mPreviewView;
    // Preview size
    private final int PREVIEWHEIGHT = 480;
    private final int PREVIEWWIDTH = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // Initialize preview and camera
        setContentView(R.layout.activity_camera_preview);
        mPreviewView = findViewById(R.id.previewView);
        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            Log.i("HackHud","Permissions not granted");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().setTargetResolution(new Size(PREVIEWWIDTH,PREVIEWHEIGHT))
               .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        ImageCapture.Builder builder = new ImageCapture.Builder();//.setTargetRotation(Surface.ROTATION_180);
        imageCapture = builder
                .setTargetRotation(/*this.getWindowManager().getDefaultDisplay().getRotation()*/Surface.ROTATION_0)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);

    }
    private void sendFullPicture(){
        Log.i("HackHud","SendFullPicture");
        ImageCapture.Builder builder = new ImageCapture.Builder();//.setTargetRotation(Surface.ROTATION_180);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        final ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(stream).build();
        imageCapture.takePicture (outputOptions,executor,new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.i("HackHud","Uploadinf");
                        uploadImage(stream.toByteArray());
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.e("HackHud",error.getMessage());
                        // insert your code here.
                    }
                }
        );

    }
    public void sendPreviewPicture(){
        uploadImage(getFileDataFromDrawable(mPreviewView.getBitmap()));
    }


    private void saveFullPictureToDisk() throws FileNotFoundException {
        Log.i("HackHud","SaveFullPicture");
        ImageCapture.Builder builder = new ImageCapture.Builder();//.setTargetRotation(Surface.ROTATION_180);
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "NEW_IMAGE");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        String name="kuva";
        File photo = new File("/storage/emulated/legacy/DCIM", name + ".jpg");
        if (photo.exists()) photo.delete();
        FileOutputStream fos = new FileOutputStream(photo.getPath());

        final ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(fos).build();
        imageCapture.takePicture (outputOptions,executor,new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.i("HackHud","Saved to Disk");

                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.e("HackHud",error.getMessage());
                        // insert your code here.
                    }
                }
        );

    }
    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getDataDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void uploadImage(final byte[] image) {

        //getting the tag from the edittext
        final String tags = "tageja";

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, "http://65.109.137.146/upload1",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.i("HackHud",response.data.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //params.put("file", tags);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();

                params.put("file", new DataPart(imagename + ".png",image));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }
    /*
     * The method is taking Bitmap as an argument
     * then it will return the byte[] array for the given bitmap
     * and we will send this array to the server
     * here we are using PNG Compression with 80% quality
     * you can give quality between 0 to 100
     * 0 means worse quality
     * 100 means best quality
     * */
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // keys remote
//s = network
//d = camera
//x = camera side outter button
//w= neywork side ou
//q = network side middle
//z= camera side middle
        switch (keyCode) {
            case KeyEvent.KEYCODE_Q:
                navigateToFirstMenu();
                return true;
            case KeyEvent.KEYCODE_S:
                sendFullPicture();
                return true;
            case KeyEvent.KEYCODE_D:
                sendPreviewPicture();
                return true;
            case KeyEvent.KEYCODE_W:

                    try {
                        saveFullPictureToDisk();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                return true;

            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void navigateToFirstMenu() {
        Intent intent = new Intent(this, FirstMenuActivity.class);
        startActivity(intent);
    }
}