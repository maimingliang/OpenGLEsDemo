package com.maiml.openglesdemo.filter;

import android.content.res.Resources;
import android.opengl.Matrix;
import android.util.Log;

import com.maiml.openglesdemo.utils.MatrixUtils;

public class FlipFilter extends AFilter {

    public FlipFilter(Resources resource) {
        super(resource,"shader/base.vert","shader/base.frag");
    }

    public FlipFilter(String vert, String frag){
        super(null,vert,frag);
    }

    public FlipFilter(){
        super(null,"attribute vec4 aVertexCo;\n" +
                "attribute vec2 aTextureCo;\n" +
                "\n" +
                "uniform mat4 uVertexMatrix;\n" +
                "uniform mat4 uTextureMatrix;\n" +
                "\n" +
                "varying vec2 vTextureCo;\n" +
                "\n" +
                "void main(){\n" +
                "    gl_Position = uVertexMatrix*aVertexCo;\n" +
                "    vTextureCo = (uTextureMatrix*vec4(aTextureCo,0,1)).xy;\n" +
                "}",
                "precision mediump float;\n" +
                "varying vec2 vTextureCo;\n" +
                "uniform sampler2D uTexture;\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D( uTexture, vTextureCo);\n" +
                "}");
    }

    @Override
    protected void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onSizeChanged(int width, int height,int videoWidth,int videoHeight) {


        updateProjection(width, height, videoWidth, videoHeight);
    }

    private void updateProjection(int screenWidth,int screenHeight,int videoWidth, int videoHeight){


        if(screenWidth == 0 || screenHeight == 0){
            return;
        }

        Log.e("tag","---- screenWidth = " +screenWidth +"  screenHeight = " +screenHeight);
        Log.e("tag","---- videoWidth = " +videoWidth +"  videoHeight = " +videoHeight);
        float screenRatio=(float)screenWidth/screenHeight;
        float videoRatio=(float)videoWidth/videoHeight;
        if (videoRatio>screenRatio){


            Log.e("tag","---- > 1");
            Matrix.orthoM(getVertexMatrix(),0,-1f,1f,-videoRatio/screenRatio,videoRatio/screenRatio,-1f,1f);

            MatrixUtils.scale(getVertexMatrix(),videoRatio/screenRatio,videoRatio/screenRatio);
        }else {

            Log.e("tag","---- > 2");
            Matrix.orthoM(getVertexMatrix(),0,-screenRatio/videoRatio,screenRatio/videoRatio,-1f,1f,-1f,1f);
        }


    }
}
