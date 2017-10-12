package com.maiml.openglesdemo.egl;

import android.content.Context;
import android.opengl.GLES20;

import com.maiml.openglesdemo.utils.Gl2Utils;
import com.maiml.openglesdemo.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 类       名:
 * 说       明:
 * 修 改 记 录:
 * 版 权 所 有:   Copyright © 2017
 * 公       司:   深圳市旅联网络科技有限公司
 * version   0.1
 * date   2017/10/12
 * author   maimingliang
 */


public class GrayFilter {

    private Context mContext;
    private int textureType=0;      //默认使用Texture2D0

    private float[] matrix = new float[16];
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
    private FloatBuffer coordBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer vertexBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int vCoordHandle;
    private int uMatrixHandle;
    private int uCoordMatrixHandle;
    private int uTextureSamplerHandle;
    private int mTextureID;
    private int vChangeColorHandle;

    public GrayFilter(Context context) {
        mContext = context;
        initBuffer();
    }
    /**
     * Buffer初始化
     */
    private void initBuffer(){

        ByteBuffer a=ByteBuffer.allocateDirect(32);
        a.order(ByteOrder.nativeOrder());
        vertexBuffer=a.asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);
        ByteBuffer b=ByteBuffer.allocateDirect(32);
        b.order(ByteOrder.nativeOrder());
        coordBuffer=b.asFloatBuffer();
        coordBuffer.put(sCoord);
        coordBuffer.position(0);
    }


    public void create(){


        mProgram = ShaderUtils.createProgram(mContext.getResources(),"vshader/base_vertex.sh","fshader/gray_fragment.frag");

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vCoordHandle=GLES20.glGetAttribLocation(mProgram,"vCoord");
        uMatrixHandle=GLES20.glGetUniformLocation(mProgram,"vMatrix");

        uTextureSamplerHandle=GLES20.glGetUniformLocation(mProgram,"vTexture");

    }


    public void setSize(int width,int height){
        onSizeChange(width,height);
    }

    public void setTextureID(int textureID) {
        mTextureID = textureID;
    }

    private void onSizeChange(int width, int height) {
        matrix = Gl2Utils.flip(Gl2Utils.getOriginalMatrix(), false, true);
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
