attribute vec4 a_position;
attribute vec2 a_texCoord;

uniform mat4 transform;

varying vec2 texCoord;

void main() {
    gl_Position =  transform * a_position;
    texCoord = a_texCoord;
}