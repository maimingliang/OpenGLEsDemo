package com.maiml.openglesdemo.ui;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.codec.VideoEnCoder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

public class VideoEncoderActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, View.OnClickListener {


    String path = Environment.getExternalStorageDirectory() + "/easy.h264";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    int width = 640, height = 480;

    Button btnSwitch;
    boolean started = false;
    private VideoEnCoder mVideoEnCoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_encoder);
        btnSwitch = (Button) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(this);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        surfaceView.getHolder().setFixedSize(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels);

        mVideoEnCoder = new VideoEnCoder();
        mVideoEnCoder.setSavePath(path);
        try {
            mVideoEnCoder.prepare(width,height);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static int[] determineMaximumSupportedFramerate(Camera.Parameters parameters) {
        int[] maxFps = new int[]{0, 0};
        List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();
        for (Iterator<int[]> it = supportedFpsRanges.iterator(); it.hasNext(); ) {
            int[] interval = it.next();
            if (interval[1] > maxFps[1] || (interval[0] > maxFps[0] && interval[1] == maxFps[1])) {
                maxFps = interval;
            }
        }
        return maxFps;
    }
    private boolean ctreateCamera(SurfaceHolder surfaceHolder) {
        try {
            mCamera = Camera.open(mCameraId);
            Camera.Parameters parameters = mCamera.getParameters();
            int[] max = determineMaximumSupportedFramerate(parameters);
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, camInfo);
            int cameraRotationOffset = camInfo.orientation;
            int rotate = (360 + cameraRotationOffset - getDgree()) % 360;
            parameters.setRotation(rotate);
            parameters.setPreviewFormat(ImageFormat.NV21);
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            parameters.setPreviewSize(width, height);
            parameters.setPreviewFpsRange(max[0], max[1]);
            mCamera.setParameters(parameters);
            mCamera.autoFocus(null);
            int displayRotation;
            displayRotation = (cameraRotationOffset - getDgree() + 360) % 360;
            mCamera.setDisplayOrientation(displayRotation);
            mCamera.setPreviewDisplay(surfaceHolder);
            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stack = sw.toString();
            Toast.makeText(this, stack, Toast.LENGTH_LONG).show();
            destroyCamera();
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 开启预览
     */
    public synchronized void startPreview() {
        if (mCamera != null && !started) {
            mCamera.startPreview();
            int previewFormat = mCamera.getParameters().getPreviewFormat();
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            int size = previewSize.width * previewSize.height
                    * ImageFormat.getBitsPerPixel(previewFormat)
                    / 8;
            mCamera.addCallbackBuffer(new byte[size]);
            mCamera.setPreviewCallbackWithBuffer(this);
            started = true;
            btnSwitch.setText("停止");
        }
    }

    /**
     * 停止预览
     */
    public synchronized void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            started = false;
            btnSwitch.setText("开始");
        }
    }

    /**
     * 销毁Camera
     */
    protected synchronized void destroyCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            try {
                mCamera.release();
            } catch (Exception e) {

            }
            mCamera = null;
        }
    }


    private int getDgree() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        return degrees;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("tag","surfaceCreated");
        surfaceHolder = holder;
        ctreateCamera(surfaceHolder);
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("tag","surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("tag","surfaceDestroyed");
        stopPreview();
        destroyCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.e("tag","----- onPreviewFrame");
        mVideoEnCoder.feedData(data,0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyCamera();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                if (!started) {
                    startPreview();
                    try {
                        mVideoEnCoder.start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    stopPreview();
                    mVideoEnCoder.stop();
                }
                break;
        }
    }
}
