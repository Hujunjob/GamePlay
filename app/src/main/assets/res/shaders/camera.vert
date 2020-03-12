#version 200 es

layout(location=0)in vec3 aPos;
layout(location=1)in vec2 aCoord;

out vec2 FragPos;

void main() {
    gl_Position = vec4(aPos, 1.0);
    FragPos = aCoord;
}
