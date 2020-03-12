#version 200 es

precision lowp float;

uniform sampler2D texture;

in vec2 FragPos;

void main() {
    gl_FragColor = texture2D(texture,FragPos);
}
