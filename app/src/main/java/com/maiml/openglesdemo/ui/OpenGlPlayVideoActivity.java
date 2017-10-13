package com.maiml.openglesdemo.ui;

import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.renderer.PlayVideoRenderer;

public class OpenGlPlayVideoActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private PlayVideoRenderer playVideoRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl_play_video);


        glSurfaceView= (GLSurfaceView) findViewById(R.id.surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        String path = null;
        path = "/sdcard/videokit/out.mp4";
//         path = "/sdcard/DCIM/Camera/20170828_193410.mp4";
//         path = "/sdcard/DCIM/CUT/20171013_210948_meger.mp4";

        playVideoRenderer = new PlayVideoRenderer(this,path);
        glSurfaceView.setRenderer(playVideoRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        playVideoRenderer.getMediaPlayer().release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        playVideoRenderer.getMediaPlayer().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
}
