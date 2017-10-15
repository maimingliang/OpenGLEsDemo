package com.maiml.openglesdemo.filter;

import android.content.res.Resources;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Administrator on 2017/9/24 0024.
 */

public class GroupFilter extends BaseFilter {

    private Vector<AFilter> mGroup;
    private Vector<AFilter> mTempGroup;

    public GroupFilter(Resources resource) {
        super(resource);
    }

    public GroupFilter(){
        super();
    }

    @Override
    protected void initBuffer() {
        super.initBuffer();
        mGroup=new Vector<>();
        mTempGroup=new Vector<>();
    }

    public synchronized void addFilter(AFilter filter){
        mGroup.add(filter);
        mTempGroup.add(filter);
    }

    public synchronized void addFilter(int index,AFilter filter){
        mGroup.add(index, filter);
        mTempGroup.add(filter);
    }

    public synchronized AFilter removeFilter(int index){
        return mGroup.remove(index);
    }

    public boolean removeFilter(AFilter filter){
        return mGroup.remove(filter);
    }

    public synchronized AFilter element(int index){
        return mGroup.elementAt(index);
    }

    public synchronized Iterator<AFilter> iterator(){
        return mGroup.iterator();
    }

    public synchronized boolean isEmpty(){
        return mGroup.isEmpty();
    }

    @Override
    protected synchronized void onCreate() {
        super.onCreate();
        for (AFilter filter : mGroup) {
            filter.create();
        }
        mTempGroup.clear();
    }

    private void tempFilterInit(int width,int height,int videoWidth,int videoHeight){
        for (AFilter filter : mTempGroup) {
            filter.create();
            filter.sizeChanged(width, height, videoWidth, videoHeight);
        }
        mTempGroup.removeAllElements();
    }

    @Override
    protected synchronized void onSizeChanged(int width, int height,int videoWidth,int videoHeight) {
        super.onSizeChanged(width, height, videoWidth, videoHeight);
        for (AFilter filter : mGroup) {
            filter.sizeChanged(width, height, videoWidth, videoHeight);
        }
    }

    @Override
    public void draw(int texture) {
        if(mTempGroup.size()>0){
            tempFilterInit(mWidth,mHeight,videoWidth, videoHeight);
        }
        int tempTextureId=texture;
        for (int i=0;i<mGroup.size();i++){
            AFilter filter=mGroup.get(i);
            tempTextureId=filter.drawToTexture(tempTextureId);
        }
        super.draw(tempTextureId);
    }

    @Override
    public int drawToTexture(int texture) {
        if(mTempGroup.size()>0){
            tempFilterInit(mWidth,mHeight, videoWidth, videoHeight);
        }
        int tempTextureId=texture;
        for (int i=0;i<mGroup.size();i++){
            AFilter filter=mGroup.get(i);
            tempTextureId=filter.drawToTexture(tempTextureId);
        }
        return super.drawToTexture(tempTextureId);
    }

}
