#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES vTexture;

uniform vec3 vChangeColor;

void main() {


    vec4 nColor = texture2D( vTexture, textureCoordinate );

    float c=nColor.r*vChangeColor.r+nColor.g*vChangeColor.g+nColor.b*vChangeColor.b;
    gl_FragColor=vec4(c,c,c,nColor.a);
}