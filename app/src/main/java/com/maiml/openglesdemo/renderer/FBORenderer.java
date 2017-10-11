package com.maiml.openglesdemo.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.maiml.openglesdemo.utils.Gl2Utils;
import com.maiml.openglesdemo.utils.MatrixUtils;
import com.maiml.openglesdemo.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 类       名:
 * 说       明:
 * version   0.1
 * date   2017/10/11
 * author   maimingliang
 */


public class FBORenderer implements GLSurfaceView.Renderer {


    private float[] matrix = new float[16];

    private float[] mCoordMatrix = Arrays.copyOf(OM, 16);
    /**
     * 单位矩阵
     */
    public static final float[] OM = MatrixUtils.getOriginalMatrix();

    private Context mContext;
    private int dataWidth, dataHeight;
    private int width, height;
    private int cameraId;
    private SurfaceTexture mSurfaceTexture;

    private FloatBuffer coordBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer vertexBuffer;
    private int mProgram;

    //    private final float[] vertexData={
    //            -1f,1f,0f,
    //            -1f,-1f,0f,
    //            1f,-1f,0f,
    //            1f,1f,0f
    //
    //    };
    //
    //
    //    private final float[] sCoord={
    //            0f,0f,
    //            0f,1f,
    //            1f,1f,
    //            1f,0f
    //
    //    };


    //顶点坐标
    private float vertexData[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };

    //纹理坐标
    private float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };


    private final short[] indexData = {
            0, 1, 2,
            0, 2, 3
    };

    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[2];
    private ByteBuffer mBuffer;
    private int textureType = 0;      //默认使用Texture2D0

    private int mPositionHandle;
    private int vCoordHandle;
    private int uMatrixHandle;
    private int uTextureSamplerHandle;
    private int mTextureID;
    private Bitmap mBitmap;

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    private Callback mCallback;

    public interface Callback {
        void onCall(ByteBuffer data);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public FBORenderer(Context context) {
        mContext = context;


        ByteBuffer bb = ByteBuffer.allocateDirect(
                vertexData.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indexData.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indexData);
        indexBuffer.position(0);


        ByteBuffer dd = ByteBuffer.allocateDirect(
                sCoord.length * 4);
        dd.order(ByteOrder.nativeOrder());
        coordBuffer = dd.asFloatBuffer();
        coordBuffer.put(sCoord);
        coordBuffer.position(0);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {



        mProgram = ShaderUtils.createProgram(mContext.getResources(), "vshader/base_vertex.sh", "fshader/gray_fragment.frag");

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vCoordHandle = GLES20.glGetAttribLocation(mProgram, "vCoord");
        uMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        uTextureSamplerHandle = GLES20.glGetUniformLocation(mProgram, "vTexture");


        matrix = Gl2Utils.flip(Gl2Utils.getOriginalMatrix(), false, true);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {


        if (mBitmap != null && !mBitmap.isRecycled()) {
            Log.e("tag","onDrawFrame");
            createEnvi();
            //绑定FrameBuffer
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
            //为FrameBuffer挂载Texture[1]来存储颜色
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fTexture[1], 0);

            //为FrameBuffer挂载fRender[0]来存储深度
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER, fRender[0]);
            GLES20.glViewport(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            mTextureID = fTexture[0];
            draw();
            GLES20.glReadPixels(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, mBuffer);
            if(mCallback!=null){
                mCallback.onCall(mBuffer);
            }
            deleteEnvi();
            mBitmap.recycle();

        }

    }

    private void deleteEnvi() {
        GLES20.glDeleteTextures(2, fTexture, 0);
        GLES20.glDeleteRenderbuffers(1, fRender, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
    }

    private void createEnvi() {
        GLES20.glGenFramebuffers(1, fFrame, 0);
        GLES20.glGenRenderbuffers(1, fRender, 0);

        //绑定Render Buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]);
        //设置为深度的Render Buffer，并传入大小
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                mBitmap.getWidth(), mBitmap.getHeight());
        //为FrameBuffer挂载fRender[0]来存储深度
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]);
        //解绑Render Buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);

        GLES20.glGenTextures(2, fTexture, 0);
        for (int i = 0; i < 2; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[i]);
            if (i == 0) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap, 0);
            } else {
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap.getWidth(), mBitmap.getHeight(),
                        0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            }
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        mBuffer = ByteBuffer.allocate(mBitmap.getWidth() * mBitmap.getHeight() * 4);

    }


    public void draw() {
        onClear();
        onUseProgram();
        onSetExpandData();
        onBindTexture();
        onDraw();
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected void onDraw() {
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(vCoordHandle);
        GLES20.glVertexAttribPointer(vCoordHandle, 2, GLES20.GL_FLOAT, false, 0, coordBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(vCoordHandle);
    }

    /**
     * 清除画布
     */
    protected void onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void onUseProgram() {
        GLES20.glUseProgram(mProgram);
    }

    /**
     * 设置其他扩展数据
     */
    protected void onSetExpandData() {
        GLES20.glUniformMatrix4fv(uMatrixHandle, 1, false, matrix, 0);

    }

    /**
     * 绑定默认纹理
     */
    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        GLES20.glUniform1i(uTextureSamplerHandle, textureType);
    }


}
