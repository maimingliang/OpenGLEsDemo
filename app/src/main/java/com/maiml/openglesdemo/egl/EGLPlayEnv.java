package com.maiml.openglesdemo.egl;

import com.maiml.openglesdemo.utils.EGLHelper;

/**
 * Created by maimingliang on 2017/10/14.
 */

public class EGLPlayEnv {


    private static final String TAG = "GLES20BackEnv";
    private int width;
    private int height;
    private EGLHelper mEGLHelper;



    public EGLPlayEnv(int width, int height) {
        this.width = width;
        this.height = height;
        mEGLHelper = new EGLHelper();
        mEGLHelper.eglInit(width, height);

    }
}
