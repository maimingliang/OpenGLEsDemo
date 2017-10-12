package com.maiml.openglesdemo.egl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.maiml.openglesdemo.utils.EGLHelper;
import com.maiml.openglesdemo.utils.TextureHelper;

import java.nio.IntBuffer;


/**
 * 类       名:
 * 说       明:
 * date   2017/10/12
 * author   maimingliang
 */


public class GLES20BackEnv {


    private static final String TAG = "GLES20BackEnv";
    private int width;
    private int height;
    private EGLHelper mEGLHelper;

    private String mThreadOwner;//判断当前环境是否egl

    private GrayFilter mGrayFilter;

    private Bitmap mBitmap;

    public GLES20BackEnv(int width, int height) {
        this.width = width;
        this.height = height;
        mEGLHelper = new EGLHelper();
        mEGLHelper.eglInit(width, height);

    }

    public void setGrayFilter(GrayFilter grayFilter) {
        mGrayFilter = grayFilter;

        // Does this thread own the OpenGL context?
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "setRenderer: This thread does not own the OpenGL context.");
            return;
        }

        mGrayFilter.create();
        mGrayFilter.setSize(width,height);
    }

    public Bitmap getBitmap() {
        if (mGrayFilter == null) {
            Log.e(TAG, "getBitmap: Renderer was not set.");
            return null;
        }
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "getBitmap: This thread does not own the OpenGL context.");
            return null;
        }
        mGrayFilter.setTextureID(TextureHelper.loadTexture(mBitmap));
        mGrayFilter.draw();
        return convertToBitmap();
    }


    private Bitmap convertToBitmap() {
        int[] iat = new int[width * height];
        IntBuffer ib = IntBuffer.allocate(width * height);
        mEGLHelper.mGL.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                ib);
        int[] ia = ib.array();

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        for (int i = 0; i < height; i++) {
            System.arraycopy(ia, i * width, iat, (height - i - 1) * width, width);
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(iat));
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public void setThreadOwner(String threadOwner) {
        mThreadOwner = threadOwner;
    }

    public void destroy() {
        mEGLHelper.destroy();
    }

}
