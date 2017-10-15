package com.maiml.openglesdemo.renderer;



import android.content.Context;

import com.maiml.openglesdemo.filter.FlipFilter;
import com.maiml.openglesdemo.filter.FrameBuffer;
import com.maiml.openglesdemo.filter.OesFilter;
import com.maiml.openglesdemo.utils.MatrixUtils;

/**
 * Created by aiya on 2017/9/12.
 */

public class WrapRenderer implements Renderer{

    private Renderer mRenderer;
    private FlipFilter mFilter;
    private FrameBuffer mFrameBuffer;

    public static final int TYPE_MOVE=0;
    public static final int TYPE_CAMERA=1;

    public WrapRenderer(Renderer renderer, Context context){
        this.mRenderer=renderer;
        mFrameBuffer=new FrameBuffer();
        mFilter=new FlipFilter(context.getResources());
        if(renderer!=null){
            MatrixUtils.flip(mFilter.getVertexMatrix(),false,true);
        }
    }

    public void setFlag(int flag){
        if(flag==TYPE_MOVE){
            mFilter.setVertexCo(MatrixUtils.getOriginalVertexCo());
        }else if(flag==TYPE_CAMERA){
            mFilter.setVertexCo(new float[]{
                    -1.0f, 1.0f,
                    1.0f, 1.0f,
                    -1.0f, -1.0f,
                    1.0f, -1.0f,
            });
        }
    }

    public float[] getTextureMatrix(){
        return mFilter.getTextureMatrix();
    }

    @Override
    public void create() {
        mFilter.create();
        if(mRenderer!=null){
            mRenderer.create();
        }
    }

    @Override
    public void sizeChanged(int width, int height,int videoWidth, int videoHeight) {
        mFilter.sizeChanged(width, height, videoWidth, videoHeight);
        if(mRenderer!=null){
            mRenderer.sizeChanged(width, height, videoWidth, videoHeight);
        }
    }

    @Override
    public void draw(int texture) {
        if(mRenderer!=null){
            mRenderer.draw(mFilter.drawToTexture(texture));
        }else{
            mFilter.draw(texture);
        }
    }

    @Override
    public void destroy() {
        if(mRenderer!=null){
            mRenderer.destroy();
        }
        mFilter.destroy();
    }
}
