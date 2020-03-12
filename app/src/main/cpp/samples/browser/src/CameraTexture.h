//
// Created by JUN HU on 2020/3/12.
//

#ifndef GAMEPLAY_CAMERATEXTURE_H
#define GAMEPLAY_CAMERATEXTURE_H

#include "gameplay.h"
#include "Sample.h"

using namespace gameplay;

class CameraTexture : public Sample {
public:
    CameraTexture();
    ~CameraTexture();

protected:
     void initialize();
     void finalize();
     void update(float elapsedTime);
     void render(float elapsedTime);
private:

    Model *model;
    Matrix _worldViewProjectionMatrix;
};

#endif //GAMEPLAY_CAMERATEXTURE_H
