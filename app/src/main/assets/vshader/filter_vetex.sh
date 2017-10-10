attribute vec4 aPosition;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;
uniform mat4 uMatrix;
varying vec4 aPos;
void main() {
    vTexCoord=aTexCoord;
    gl_Position = uMatrix*aPosition;
    aPos=aPosition;
}