package com.maiml.openglesdemo.utils;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;



import java.io.InputStream;

public enum GpuUtils {
    ;
    /**
     * 读取Assets中的文本文件
     * @param mRes res
     * @param path 文件路径
     * @return 文本内容
     */
    public static String readText(Resources mRes,String path){
        StringBuilder result=new StringBuilder();
        try{
            InputStream is=mRes.getAssets().open(path);
            int ch;
            byte[] buffer=new byte[1024];
            while (-1!=(ch=is.read(buffer))){
                result.append(new String(buffer,0,ch));
            }
        }catch (Exception e){
            return null;
        }
        return result.toString().replaceAll("\\r\\n","\n");
    }

    /**
     * 加载Shader
     * @param shaderType Shader类型
     * @param source Shader代码
     * @return shaderId
     */
    public static int loadShader(int shaderType,String source){
        if(source==null){
            glError(1,"Shader source ==null : shaderType ="+shaderType);
            return 0;
        }
        int shader= GLES20.glCreateShader(shaderType);
        if(0!=shader){
            GLES20.glShaderSource(shader,source);
            GLES20.glCompileShader(shader);
            int[] compiled=new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,compiled,0);
            if(compiled[0]==0){
                glError(1,"Could not compile shader:"+shaderType);
                glError(1,"GLES20 Error:"+ GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader=0;
            }
        }
        return shader;
    }

    /**
     * 通过字符串创建GL程序
     * @param vertexSource 顶点着色器
     * @param fragmentSource 片元着色器
     * @return programId
     */
    public static int createGLProgram(String vertexSource, String fragmentSource){
        int vertex=loadShader(GLES20.GL_VERTEX_SHADER,vertexSource);
        if(vertex==0)return 0;
        int fragment=loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);
        if(fragment==0)return 0;
        int program= GLES20.glCreateProgram();
        if(program!=0){
            GLES20.glAttachShader(program,vertex);
            GLES20.glAttachShader(program,fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus=new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,linkStatus,0);
            if(linkStatus[0]!= GLES20.GL_TRUE){
                glError(1,"Could not link program:"+ GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program=0;
            }
        }
        return program;
    }

    /**
     * 通过assets中的文件创建GL程序
     * @param res res
     * @param vertex 顶点作色器路径
     * @param fragment 片元着色器路径
     * @return programId
     */
    public static int createGLProgramByAssetsFile(Resources res,String vertex,String fragment){
        return createGLProgram(readText(res,vertex),readText(res,fragment));
    }
    private static boolean debug = true;

    private static void glError(int code,Object index){
        if(debug&&code!=0){
            Log.e("tag","glError:"+code+"---"+index);
        }
    }

}
