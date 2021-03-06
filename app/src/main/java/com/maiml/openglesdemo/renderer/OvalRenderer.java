package com.maiml.openglesdemo.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.maiml.openglesdemo.utils.ShaderUtils.loadShader;

/**
 * 类       名:
 * 说       明:
 * date   2017/10/9
 * author   maimingliang
 */


public class OvalRenderer implements GLSurfaceView.Renderer {


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
    private final float[] vertexData;


    private final short[] indexData = {
            0,1,2,
            0,2,3,
            0,3,4,
            0,4,1
    };
    private float height=0.0f;

    private float radius=1.0f;
    private int n=360;  //切割份数

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;

    private final float[] projectionMatrix=new float[16];

    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mMVPMatrix=new float[16];
    private int uMatrixHandle;

    //顶点个数

    //顶点之间的偏移量
    private final int vertexStride = 5 * 4; // 每个顶点四个字节
    //设置颜色，依次为红绿蓝和透明通道
    float color[] =  { 1.0f, 1.0f, 1.0f, 1.0f };

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private  int mVertexShader;
    private  int mFragmentShader;
    public void setRadius(float radius){
        this.radius=radius;
    }
    public OvalRenderer(Context context) {
        mContext = context;
        vertexData = createPositions();
        ByteBuffer bb = ByteBuffer.allocateDirect(
                vertexData.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);



    }

    private float[]  createPositions(){
        ArrayList<Float> data=new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(height);
        float angDegSpan=360f/n;
        for(float i=0;i<360+angDegSpan;i+=angDegSpan){
            data.add((float) (radius*Math.sin(i*Math.PI/180f)));
            data.add((float)(radius*Math.cos(i*Math.PI/180f)));
            data.add(height);
        }
        float[] f=new float[data.size()];
        for (int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }
        return f;
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
    public void setMatrix(float[] matrix){
        this.mMVPMatrix=matrix;
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //计算宽高比
        float ratio=(float)width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);

        GLES20.glUniformMatrix4fv(uMatrixHandle,1,false,mMVPMatrix,0);

        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据 stride表示步长，因为一个顶点三个坐标，一个坐标是float（4字节），所以步长是12字节
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                12, vertexBuffer);
        //获取片元着色器的vColor成员的句柄

        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


//        GLES20.glDrawElements(GLES20.GL_TRIANGLES,indexData.length,GLES20.GL_UNSIGNED_SHORT,indexBuffer);

//        //绘制三角形扇
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexData.length/3);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }
}
