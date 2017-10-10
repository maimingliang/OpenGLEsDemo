package com.maiml.openglesdemo.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.filter.Filter;
import com.maiml.openglesdemo.utils.ShaderUtils;
import com.maiml.openglesdemo.utils.TextureHelper;
import com.maiml.openglesdemo.utils.VaryTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 类       名:
 * 说       明:
 * date   2017/10/9
 * author   maimingliang
 */


public class FilterRenderer implements GLSurfaceView.Renderer {


    private final FloatBuffer coordBuffer;
    private  ShortBuffer indexBuffer;
    private Context mContext;


    private final float[] vertexData={
            -1f,1f,0f,
            -1f,-1f,0f,
            1f,-1f,0f,
            1f,1f,0f

    };


    private final float[] sCoord={
            0f,0f,
            0f,1f,
            1f,1f,
            1f,0f

    };
    private final short[] indexData = {
            0,1,2,
            0,2,3
    };
    private Bitmap mBitmap;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;
    private int textureId;
    private int vChangeColorHandle;
    private int vChangeTypeHandle;
    private final VaryTools varyTools;

    public void setImageBuffer(int[] buffer,int width,int height){
        mBitmap= Bitmap.createBitmap(buffer,width,height, Bitmap.Config.RGB_565);
    }

    public void setBitmap(Bitmap bitmap){
        this.mBitmap=bitmap;
    }

    private FloatBuffer vertexBuffer;

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
     public FilterRenderer(Context context) {
        mContext = context;
         varyTools = new VaryTools();
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
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //，初始化的东西要在render 的create里面完成

        mProgram = ShaderUtils.createProgram(mContext.getResources(),"vshader/filter_vetex.sh","fshader/filter_fragment.sh");
//        attribute vec4 aPosition;
//        attribute vec2 aTexCoord;
 //        uniform mat4 uMatrix;
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
         uMatrixHandle=GLES20.glGetUniformLocation(mProgram,"uMatrix");
        aTextureCoordHandle=GLES20.glGetAttribLocation(mProgram,"aTexCoord");
        vChangeColorHandle=GLES20.glGetUniformLocation(mProgram,"vChangeColor");
        vChangeTypeHandle=GLES20.glGetUniformLocation(mProgram,"vChangeType");
        uTextureSamplerHandle=GLES20.glGetUniformLocation(mProgram,"sTexture");


//        textureId = TextureHelper.loadTexture(mContext, R.mipmap.image01);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inScaled=false;

        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.image01,options);
        textureId = TextureHelper.loadTexture(mBitmap);

    }
     @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

//         GLES20.glViewport(0,0,width,height);
//
//         float ratio=width>height?
//                 (float)width/height:
//                 (float)height/width;
//         if (width>height){
//             //参数的含义：左右(x)下上(y)近远(z)
//             Matrix.orthoM(projectionMatrix,0,-ratio,ratio,-1f,1f,-1f,1f);
//         }else Matrix.orthoM(projectionMatrix,0,-1f,1f,-ratio,ratio,-1f,1f);
         //计算变换矩阵
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();

        float bRatio = w / (float)h; // 图片高宽比
        float sRatio = width / (float)height; //屏幕宽高比


        if (width > height) {
            //横屏
                if(sRatio > bRatio){
//                    Matrix.orthoM(mProjectMatrix, 0, -sRatio/bRatio,sRatio/bRatio, -1,1, 3, 7);
                    varyTools.ortho(-sRatio/bRatio,sRatio/bRatio, -1,1, 3, 7);

                }else{
//                    Matrix.orthoM(mProjectMatrix, 0, -sRatio*bRatio,sRatio*bRatio, -1,1, 3, 7);
                    varyTools.ortho(-sRatio*bRatio,sRatio*bRatio, -1,1, 3, 7);
                }
        }else{
            //竖屏
            if(sRatio > bRatio){
//                Matrix.orthoM(mProjectMatrix, 0, -1,1,-bRatio/sRatio,bRatio/sRatio, 3, 7);
                varyTools.ortho(-1,1,-bRatio/sRatio,bRatio/sRatio, 3, 7);
            }else{
//                Matrix.orthoM(mProjectMatrix, 0, -1,1, -1/sRatio*bRatio,1/sRatio*bRatio, 3, 7);
                varyTools.ortho(-1,1, -1/sRatio*bRatio,1/sRatio*bRatio, 3, 7);
            }
        }

         varyTools.setCamera(0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//        //设置相机位置
//        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//        //计算变换矩阵
//        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);


         //y轴正方形平移
        varyTools.pushMatrix();
//        varyTools.translate(0,3,0);
//         varyTools.rotate(30f,-1,-1,1);
//         varyTools.popMatrix();
         varyTools.scale(2.0f,2.0f,2.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);


        GLES20.glUniformMatrix4fv(uMatrixHandle,1,false,varyTools.getFinalMatrix(),0);


        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据 stride表示步长，因为一个顶点三个坐标，一个坐标是float（4字节），所以步长是12字节
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                12, vertexBuffer);

        GLES20.glUniform3fv(vChangeColorHandle,1, Filter.GRAY.data(),0);
        GLES20.glUniform1i(vChangeTypeHandle,Filter.GRAY.getType());

        //获取片元着色器的vColor成员的句柄
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle,2,GLES20.GL_FLOAT,false,8,coordBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);

        GLES20.glUniform1i(uTextureSamplerHandle,0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,indexData.length,GLES20.GL_UNSIGNED_SHORT,indexBuffer);

//        GLES20.glDrawElements(GLES20.GL_TRIANGLES,indexData.length,GLES20.GL_UNSIGNED_SHORT,indexBuffer);

//        //绘制三角形扇
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexData.length/3);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }
}
