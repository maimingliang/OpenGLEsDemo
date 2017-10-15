package com.maiml.openglesdemo.renderer;

public interface Renderer {

    void create();

    void sizeChanged(int width, int height,int videoWidth,int videoHeight);

    void draw(int texture);

    void destroy();

}
