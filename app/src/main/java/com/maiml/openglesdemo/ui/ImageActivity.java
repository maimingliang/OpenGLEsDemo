package com.maiml.openglesdemo.ui;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.renderer.SGLRenderer;

public class ImageActivity extends AppCompatActivity {

    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        initViews();

    }

    private void initViews() {
        mGlSurfaceView = (GLSurfaceView)findViewById(R.id.surface_view);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setRenderer(new SGLRenderer(this));
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
    }
}
