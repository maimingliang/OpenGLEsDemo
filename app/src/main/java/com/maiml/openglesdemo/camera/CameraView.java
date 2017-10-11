package com.maiml.openglesdemo.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 类       名:
 * 说       明:
 * date   2017/10/11
 * author   maimingliang
 */


public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private KitkatCamera mCamera;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private Context mContext;
    private Runnable mRunnable;
    private CameraDrawer mCameraDrawer;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mCamera = new KitkatCamera();
        mCameraDrawer = new CameraDrawer(mContext);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e("tag","---- onSurfaceCreated");
        mCameraDrawer.onSurfaceCreated(gl,config);
        if (mRunnable != null) {
            mRunnable.run();
            mRunnable = null;
        }
        mCamera.open(mCameraId);
        mCameraDrawer.setCameraId(mCameraId);
        Point point = mCamera.getPreviewSize();
        mCameraDrawer.setDateSize(point.x,point.y);

        mCamera.setPreviewTexture(mCameraDrawer.getSurfaceTexture());
        mCameraDrawer.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });

        mCamera.preview();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e("tag","---- onSurfaceChanged");
        mCameraDrawer.setViewSize(width,height);
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.e("tag","---- onDrawFrame");
        mCameraDrawer.onDrawFrame(gl);
    }


    public void switchCamera() {
        mRunnable = new Runnable() {
            @Override
            public void run() {

                mCamera.close();
                mCameraId = mCameraId == 1?0:1;
            }
        };

        onPause();
        onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.close();
        }
    }
}
