attribute vec4 position;
attribute vec4 inputTextureCoordinate;
varying vec2 textureCoordinate;

void main(){
    // 世界坐标
    gl_Position = position;
    // 纹理坐标
    textureCoordinate = inputTextureCoordinate.xy;
}