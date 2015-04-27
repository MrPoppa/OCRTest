package com.mr.poppa.ocrtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.googlecode.tesseract.android.TessBaseAPI;


public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener,
        android.hardware.Camera.PictureCallback, android.hardware.Camera.ShutterCallback{

    static final String TAG = "DBG_" + MainActivity.class.getName();
    Button shutterButton;
    Button focusButton;
    FocusBoxView focusBox;
    SurfaceView cameraFrame;
    CameraEngine cameraEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        TessBaseAPI baseApi = new TessBaseAPI();
//        baseApi.setDebug(true); baseApi.init(DATA_PATH, LANG);
//        baseApi.setImage(bitmap);
//        String recognizedText = baseApi.getUTF8Text();
//        baseApi.end();


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Log.d(TAG, "Surface Created - starting camera");

        if (cameraEngine != null && !cameraEngine.isOn()) {
            cameraEngine.start();
        }

        if (cameraEngine != null && cameraEngine.isOn()) {
            Log.d(TAG, "Camera engine already on");
            return;
        }

        cameraEngine = CameraEngine.New(holder);
        cameraEngine.start();

        Log.d(TAG, "Camera engine started");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraFrame = (SurfaceView) findViewById(R.id.camera_frame);
        shutterButton = (Button) findViewById(R.id.shutter_button);
        focusBox = (FocusBoxView) findViewById(R.id.focus_box);
        focusButton = (Button) findViewById(R.id.focus_button);

        shutterButton.setOnClickListener(this);
        focusButton.setOnClickListener(this);

        SurfaceHolder surfaceHolder = cameraFrame.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraFrame.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (cameraEngine != null && cameraEngine.isOn()) {
            cameraEngine.stop();
        }

        SurfaceHolder surfaceHolder = cameraFrame.getHolder();
        surfaceHolder.removeCallback(this);
    }

    @Override
    public void onClick(View v) {
        if(v == shutterButton){
            if(cameraEngine != null && cameraEngine.isOn()){
                cameraEngine.takeShot(this, this, this);
            }
        }

        if(v == focusButton){
            if(cameraEngine!=null && cameraEngine.isOn()){
                cameraEngine.requestFocus();
            }
        }
    }


    @Override
    public void onShutter() {

    }

    public String detectText(Bitmap bitmap) {

        TessBaseAPI tessBaseAPI = new TessBaseAPI();

        String path = Environment.getExternalStorageDirectory().getPath();
//                "/mnt/sdcard/packagename/tessdata/eng_traineddata";

        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(path, "eng"); //Init the Tess with the trained data file, with english language

        //For example if we want to only detect numbers
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "1234567890");
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU " +
                "YTREWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");


        tessBaseAPI.setImage(bitmap);

        String text = tessBaseAPI.getUTF8Text();

        tessBaseAPI.end();

        return text;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
        Log.d(TAG, "Picture taken");

        if (data == null) {
            Log.d(TAG, "Got null data");
            return;
        }

        Bitmap bmp = Tools.getFocusedBitmap(this, camera, data, focusBox.getBox());

        Log.d(TAG, "Got bitmap");

        new TessAsyncEngine().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, this, bmp);


    }

}
