package com.maiml.openglesdemo.filter;

import android.content.res.Resources;

/**
 * Created by Administrator on 2017/9/24 0024.
 */

public class SobelFilter extends GroupFilter {

    public SobelFilter(Resources resource) {
        super(resource);
    }

    @Override
    protected void initBuffer() {
        super.initBuffer();
//        addFilter(new GrayFilter(mRes));
        addFilter(new FlipFilter(mRes));
//        addFilter(new InSobelFilter(mRes));

    }

    private class InSobelFilter extends AFilter{

        InSobelFilter(Resources resource) {
            super(resource, "shader/base.vert", "shader/effect/sobel_base.frag");
            shaderNeedTextureSize(true);
        }
    }
}
