package com.maiml.openglesdemo.ui;

import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.codec.Mp4Processor;
import com.maiml.openglesdemo.filter.AFilter;
import com.maiml.openglesdemo.filter.SobelFilter;
import com.maiml.openglesdemo.renderer.PlayVideoRenderer;
import com.maiml.openglesdemo.renderer.Renderer;

import java.io.IOException;

public class OpenGlPlayVideoActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private PlayVideoRenderer playVideoRenderer;
    private Mp4Processor mProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl_play_video);


        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        glSurfaceView= (GLSurfaceView) findViewById(R.id.surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        String path = null;
        path = "/sdcard/videokit/out.mp4";
//         path = "/sdcard/DCIM/Camera/20170828_193410.mp4";
//         path = "/sdcard/DCIM/CUT/20171013_210948_meger.mp4";

        playVideoRenderer = new PlayVideoRenderer(this,path);
        glSurfaceView.setRenderer(playVideoRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);



        mProcessor=new Mp4Processor(this);
        mProcessor.setOutputPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/temp.mp4");
        mProcessor.setInputPath(path);
        mProcessor.setOnCompleteListener(new Mp4Processor.CompleteListener() {
            @Override
            public void onComplete(String path) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"处理完毕",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
//        mProcessor.setRenderer(new Renderer() {
//
//            AFilter filter;
//
//            @Override
//            public void create() {
//                filter=new SobelFilter(getResources());
//                filter.create();
//            }
//
//            @Override
//            public void sizeChanged(int width, int height, int videoWidth, int videoHeight) {
//                filter.sizeChanged(width, height,videoWidth,videoHeight);
//            }
//
//            @Override
//            public void draw(int texture) {
//                filter.draw(texture);
//            }
//
//            @Override
//            public void destroy() {
//                filter.destroy();
//            }
//        });
    }

    private void start() {
        try {
            mProcessor.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
