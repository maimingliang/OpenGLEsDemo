package com.maiml.openglesdemo.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.maiml.openglesdemo.utils.MatrixUtils;
import com.maiml.openglesdemo.utils.ShaderUtils;
import com.maiml.openglesdemo.utils.TextureHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


public  class STextureRender {
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final String TAG = "STextureRendering";

    private Context context;
    private int aPositionHandle;
    private int programId;
    private FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            1f,-1f,0f,
            -1f,-1f,0f,
            1f,1f,0f,
            -1f,1f,0f
    };

    private final float[] projectionMatrix=new float[16];
    private int uMatrixHandle;

    private final float[] textureVertexData = {
            1f,0f,
            0f,0f,
            1f,1f,
            0f,1f
    };
    private FloatBuffer textureVertexBuffer;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;
    private int textureId;



    private float[] mSTMatrix = new float[16];
    private int uSTMMatrixHandle;

    private int screenWidth = 1080,screenHeight = 1920;

    public STextureRender(Context context) {

        this.context = context;

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);


    }


    public STextureRender() {
//        Matrix.setIdentityM(mSTMatrix, 0);
    }

    public int getTextureId() {
        return textureId;
    }

    /**
     * Draws the external texture in SurfaceTexture onto the current EGL surface.
     */
    public void drawFrame(SurfaceTexture st, boolean invert) {
//         st.getTransformMatrix(mSTMatrix);
//        if (invert) {
//            mSTMatrix[5] = -mSTMatrix[5];
//            mSTMatrix[13] = 1.0f - mSTMatrix[13];


//        }

        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        st.getTransformMatrix(mSTMatrix);
        GLES20.glUseProgram(programId);
        GLES20.glUniformMatrix4fv(uMatrixHandle,1,false,projectionMatrix,0);
        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, mSTMatrix, 0);

        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                12, vertexBuffer);

        textureVertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle,2,GLES20.GL_FLOAT,false,8,textureVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);

        GLES20.glUniform1i(uTextureSamplerHandle,0);
        GLES20.glViewport(0,0,screenWidth,screenHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }

    /**
     *
     * @param gl
     * @param width
     * @param height
     * @param tag 0: screen 1: videsize
     */
    public void onSurfaceChanged(GL10 gl, int width, int height,int tag) {
        Log.d(TAG, "onSurfaceChanged: "+width+" "+height);
        if(tag == 0){
            screenWidth=width; screenHeight=height;

        }else{
            updateProjection(width,height);
        }
    }
    private void updateProjection(int videoWidth, int videoHeight){


        if(screenWidth == 0 || screenHeight == 0){
            return;
        }

        Log.e("tag","---- screenWidth = " +screenWidth +"  screenHeight = " +screenHeight);
        Log.e("tag","---- videoWidth = " +videoWidth +"  videoHeight = " +videoHeight);
        float screenRatio=(float)screenWidth/screenHeight;
        float videoRatio=(float)videoWidth/videoHeight;
        if (videoRatio>screenRatio){


            Log.e("tag","---- > 1");
            Matrix.orthoM(projectionMatrix,0,-1f,1f,-videoRatio/screenRatio,videoRatio/screenRatio,-1f,1f);

            MatrixUtils.scale(projectionMatrix,videoRatio/screenRatio,videoRatio/screenRatio);
        }else {

            Log.e("tag","---- > 2");
            Matrix.orthoM(projectionMatrix,0,-screenRatio/videoRatio,screenRatio/videoRatio,-1f,1f,-1f,1f);
        }


    }
    /**
     * Initializes GL state.  Call this after the EGL surface has been created and made current.
     */
    public void surfaceCreated() {
        programId = ShaderUtils.createProgram(context.getResources(),"vshader/play_vetex.sh","fshader/play_fragment.sh");


        aPositionHandle= GLES20.glGetAttribLocation(programId,"aPosition");

        uMatrixHandle=GLES20.glGetUniformLocation(programId,"uMatrix");
        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");
        uTextureSamplerHandle=GLES20.glGetUniformLocation(programId,"sTexture");
        aTextureCoordHandle=GLES20.glGetAttribLocation(programId,"aTexCoord");
        textureId = TextureHelper.createTextureID();

    }



}
