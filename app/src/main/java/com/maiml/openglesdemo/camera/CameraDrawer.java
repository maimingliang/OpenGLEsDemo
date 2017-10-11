package com.maiml.openglesdemo.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.maiml.openglesdemo.utils.Gl2Utils;
import com.maiml.openglesdemo.utils.MatrixUtils;
import com.maiml.openglesdemo.utils.ShaderUtils;
import com.maiml.openglesdemo.utils.TextureHelper;

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


public class CameraDrawer implements GLSurfaceView.Renderer{


    private float[] matrix = new float[16];

    private float[] mCoordMatrix= Arrays.copyOf(OM,16);
    /**
     * 单位矩阵
     */
    public static final float[] OM= MatrixUtils.getOriginalMatrix();

    private Context mContext;
    private int dataWidth,  dataHeight;
    private int width,height;
    private int cameraId;
    private SurfaceTexture mSurfaceTexture;

    private  FloatBuffer coordBuffer;
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
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f,  -1.0f,
    };

    //纹理坐标
    private float[] sCoord={
            0.0f, 0.0f,
            0.0f,  1.0f,
            1.0f,  0.0f,
            1.0f, 1.0f,
    };


    private final short[] indexData = {
            0,1,2,
            0,2,3
    };

    private int textureType=0;      //默认使用Texture2D0

    private int mPositionHandle;
    private int vCoordHandle;
    private int uMatrixHandle;
    private int uCoordMatrixHandle;
    private int uTextureSamplerHandle;
    private int mTextureID;


    public CameraDrawer(Context context) {
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

    public void setCoordMatrix(float[] coordMatrix) {
        mCoordMatrix = coordMatrix;
    }

    public void setDateSize(int dataWidth, int dataHeight) {
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        calculateMatrix();
    }

    public void setViewSize(int width, int height) {
        this.width = width;
        this.height = height;
        calculateMatrix();
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
        calculateMatrix();
    }

    private void calculateMatrix() {

        Gl2Utils.getShowMatrix(matrix,dataWidth,dataHeight,width,height);

        if(cameraId == 1){
            Gl2Utils.flip(matrix,true,false);
            Gl2Utils.rotate(matrix,90);
        }else{
            Gl2Utils.rotate(matrix,270);
        }


    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        mTextureID = TextureHelper.createTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mProgram = ShaderUtils.createProgram(mContext.getResources(),"vshader/oes_base_vertex.sh","fshader/oes_base_fragment.sh");

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vCoordHandle=GLES20.glGetAttribLocation(mProgram,"vCoord");
        uMatrixHandle=GLES20.glGetUniformLocation(mProgram,"vMatrix");
        uCoordMatrixHandle=GLES20.glGetUniformLocation(mProgram,"vCoordMatrix");
        uTextureSamplerHandle=GLES20.glGetUniformLocation(mProgram,"vTexture");

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        setViewSize(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }

         draw();
    }


    public void draw(){
        onClear();
        onUseProgram();
        onSetExpandData();
        onBindTexture();
        onDraw();
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected void onDraw(){
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle,2, GLES20.GL_FLOAT, false, 0,vertexBuffer);

        GLES20.glEnableVertexAttribArray(vCoordHandle);
        GLES20.glVertexAttribPointer(vCoordHandle, 2, GLES20.GL_FLOAT, false, 0, coordBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(vCoordHandle);
    }

    /**
     * 清除画布
     */
    protected void onClear(){
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void onUseProgram(){
        GLES20.glUseProgram(mProgram);
    }

    /**
     * 设置其他扩展数据
     */
    protected void onSetExpandData(){
        GLES20.glUniformMatrix4fv(uMatrixHandle,1,false,matrix,0);
        GLES20.glUniformMatrix4fv(uCoordMatrixHandle,1,false,mCoordMatrix,0);
    }
    /**
     * 绑定默认纹理
     */
    protected void onBindTexture(){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureID);
        GLES20.glUniform1i(uTextureSamplerHandle,textureType);
    }


}
