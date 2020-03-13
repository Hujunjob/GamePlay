//
// Created by JUN HU on 2020/3/12.
//

#ifndef GAMEPLAY_CAMERASAMPLE_H
#define GAMEPLAY_CAMERASAMPLE_H

#include <gameplay.h>
#include "Sample.h"

using namespace gameplay;

class CameraSample : public Sample {
public:
    CameraSample();

    ~CameraSample();

    void offerData(const void *data, int size, int width, int height);

private:
    Model *_model;
    void *pixels;
    int width;
    int height;
    GLuint cameraTextureYID;
    GLuint cameraTextureUVID;
    void initializeTexture();
    Matrix _worldViewProjectionMatrix;

public:

    /**
     * @see Game::initialize
     */
    void initialize();

    /**
     * @see Game::finalize
     */
    void finalize();

    /**
     * @see Game::update
     */
    void update(float elapsedTime);

    /**
     * @see Game::render
     */
    void render(float elapsedTime);


};

#endif //GAMEPLAY_CAMERASAMPLE_H
