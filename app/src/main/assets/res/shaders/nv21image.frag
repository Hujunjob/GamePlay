#ifdef OPENGL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

uniform sampler2D videoFrameY;
uniform sampler2D videoFrameUV;

varying vec2 texCoord;

const mat3 M = mat3(1, 1, 1, 0, -.18732, 1.8556, 1.57481, -.46813, 0);

void main() {
    lowp vec3 yuv;
    lowp vec3 rgb;
    yuv.x = texture2D(videoFrameY, texCoord).r;
    yuv.yz = texture2D(videoFrameUV, texCoord).ar - vec2(0.5, 0.5);
    rgb = M * yuv;
    gl_FragColor = vec4(rgb, 1.0);
}