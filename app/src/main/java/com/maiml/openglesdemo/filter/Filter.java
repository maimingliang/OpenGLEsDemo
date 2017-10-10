package com.maiml.openglesdemo.filter;

/**
 * 类       名:
 * 说       明:
 * date   2017/10/10
 * author   maimingliang
 */


public enum  Filter {

    NONE(0,new float[]{0.0f,0.0f,0.0f}),
    GRAY(1,new float[]{0.299f,0.587f,0.114f}),
    COOL(2,new float[]{0.0f,0.0f,0.1f}),
    WARM(2,new float[]{0.1f,0.1f,0.0f}),
    BLUR(3,new float[]{0.006f,0.004f,0.002f}),
    MAGN(4,new float[]{0.0f,0.0f,0.4f});


    private int vChangeType;
    private float[] data;

    Filter(int vChangeType,float[] data){
        this.vChangeType=vChangeType;
        this.data=data;
    }

    public int getType(){
        return vChangeType;
    }

    public float[] data(){
        return data;
    }
}
