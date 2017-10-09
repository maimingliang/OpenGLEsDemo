package com.maiml.openglesdemo.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.maiml.openglesdemo.utils.ShaderUtils.loadShader;

/**
 * 类       名:
 * 说       明:
 * date   2017/10/9
 * author   maimingliang
 */


public class TrangleRenderer implements GLSurfaceView.Renderer {


    private Context mContext;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 uMatrix;"+
                    "void main() {" +
                    "  gl_Position = uMatrix*vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    private final float[] vertexData = {
            0f,0f,0f,
            1f,-1f,0f,
            1f,1f,0f
    };

    private FloatBuffer vertexBuffer;

    private final float[] projectionMatrix=new float[16];
    private int uMatrixHandle;

    //顶点个数

    //顶点之间的偏移量
    private final int vertexStride = 3 * 4; // 每个顶点四个字节
    //设置颜色，依次为红绿蓝和透明通道
    float color[] = { 1.0f, 0.0f, 1.0f, 0.0f };

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private  int mVertexShader;
    private  int mFragmentShader;

    public TrangleRenderer(Context context) {
        mContext = context;

        ByteBuffer bb = ByteBuffer.allocateDirect(
                vertexData.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);




    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //，初始化的东西要在render 的create里面完成
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, mVertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, mFragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);

        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        uMatrixHandle=GLES20.glGetUniformLocation(mProgram,"uMatrix");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);

        float ratio=width>height?
                (float)width/height:
                (float)height/width;
        if (width>height){
            //参数的含义：左右(x)下上(y)近远(z)
            Matrix.orthoM(projectionMatrix,0,-ratio,ratio,-1f,1f,-1f,1f);
        }else Matrix.orthoM(projectionMatrix,0,-1f,1f,-ratio,ratio,-1f,1f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);

        GLES20.glUniformMatrix4fv(uMatrixHandle,1,false,projectionMatrix,0);

        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据 stride表示步长，因为一个顶点三个坐标，一个坐标是float（4字节），所以步长是12字节
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                12, vertexBuffer);
        //获取片元着色器的vColor成员的句柄

        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);



        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
