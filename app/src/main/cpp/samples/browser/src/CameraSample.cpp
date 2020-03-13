//
// Created by JUN HU on 2020/3/12.
//

#include "CameraSample.h"

CameraSample::CameraSample() : _model(NULL), pixels(NULL) {

}

CameraSample::~CameraSample() {

}


void CameraSample::initializeTexture() {
    glBindTexture(GL_TEXTURE_2D, cameraTextureYID);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                    GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                    GL_CLAMP_TO_EDGE);
    glBindTexture(GL_TEXTURE_2D, cameraTextureUVID);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                    GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                    GL_CLAMP_TO_EDGE);
}

Material *getMaterial() {
    Effect *effect = Effect::createFromFile("res/shaders/nv21image.vert",
                                           "res/shaders/nv21image.frag");
    Material *material = Material::create(effect);
    effect->setValue(effect->getUniform("videoFrameY"),0);
    effect->setValue(effect->getUniform("videoFrameUV"),1);

    return material;
}

void CameraSample::initialize() {
    _model = Model::create(Mesh::createQuadFullscreen());
    Material*material=getMaterial();
    _model->setMaterial(material);

    GLuint textureNames[2];
    glGenTextures(2, textureNames);
    cameraTextureYID = textureNames[0];
    cameraTextureUVID = textureNames[1];
    initializeTexture();
    float width = getWidth() / (float) getHeight();
    float height = 1.0f;
    Matrix::createOrthographic(width, height, -1.0f, 1.0f, &_worldViewProjectionMatrix);
}

void CameraSample::finalize() {

}

void CameraSample::update(float elapsedTime) {
    _model->getMaterial()->getParameter("u_worldViewProjectionMatrix")->setValue(
            _worldViewProjectionMatrix);
}

void CameraSample::render(float elapsedTime) {
    if (pixels == NULL) {
        return;
    }

    glBindTexture(GL_TEXTURE_2D, cameraTextureYID);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                    GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                    GL_CLAMP_TO_EDGE);
//    glTexImage2D()
//    frameRenderBuffer?.position(0);
    glTexImage2D(GL_TEXTURE_2D, 0, 0, 0, width,
                    height, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                    pixels);

    glBindTexture(GL_TEXTURE_2D, cameraTextureUVID);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                    GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                    GL_CLAMP_TO_EDGE);

//    frameRenderBuffer?.position(4 * (width / 2) * (height / 2));
    glTexSubImage2D(GL_TEXTURE_2D, 0, width / 2, height / 2, width / 2,
                    height / 2, GL_LUMINANCE_ALPHA,
                    GL_UNSIGNED_BYTE, pixels);

    glDepthFunc(GL_LEQUAL);


    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, cameraTextureYID);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, cameraTextureUVID);

    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

    glBindVertexArray(0);

    glDisable(GL_BLEND);
    glBindTexture(GL_TEXTURE_2D, 0);
    glUseProgram(0);
}

void CameraSample::offerData(const void *data, int size, int width, int height) {
    if (pixels == NULL) {
        pixels = new u_char[size];
    }
    memcpy(pixels, data, size);
    this->width = width;
    this->height = height;
}
