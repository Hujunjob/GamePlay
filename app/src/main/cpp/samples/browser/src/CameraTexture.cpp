//
// Created by JUN HU on 2020/3/12.
//

#include "CameraTexture.h"

CameraTexture::CameraTexture() : model(NULL) {

}

CameraTexture::~CameraTexture() {
    finalize();
}

//Mesh *createMesh() {
//    Mesh *mesh = Mesh::createMesh()
//}

//构建一个Material
Material *generateMaterial(Model *model, const char *image, bool mipmap) {
    Material *material = model->setMaterial("res/shaders/textured.vert",
                                            "res/shaders/textured.frag");
    material->setParameterAutoBinding("u_worldViewProjectionMatrix", "WORLD_VIEW_PROJECTION_MATRIX");
    Texture::Sampler *sampler = material->getParameter("u_diffuseTexture")->setValue(image, mipmap);
    if (mipmap)
        sampler->setFilterMode(Texture::LINEAR_MIPMAP_LINEAR, Texture::LINEAR);
    else
        sampler->setFilterMode(Texture::LINEAR, Texture::LINEAR);
    sampler->setWrapMode(Texture::CLAMP, Texture::CLAMP);
    material->getStateBlock()->setCullFace(true);
    material->getStateBlock()->setDepthTest(true);
    material->getStateBlock()->setDepthWrite(true);

    return material;
}

void CameraTexture::initialize() {
    //首先构建一个Mesh
    //构建一个全屏的矩形
    Mesh *mesh = Mesh::createQuadFullscreen();

    //然后根据这个Mesh创建一个Model
    //Model有mesh，mesh上面可以附着Material
    model = Model::create(mesh);

    generateMaterial(model,"res/png/brick.png",true);
    // Create an orthographic projection matrix.
    float width = getWidth() / (float)getHeight();
    float height = 1.0f;
    Matrix::createOrthographic(width, height, -1.0f, 1.0f, &_worldViewProjectionMatrix);
}

void CameraTexture::finalize() {
    model->release();
//    SAFE_DELETE(model);
}

void CameraTexture::update(float elapsedTime) {
    model->getMaterial()->getParameter("u_worldViewProjectionMatrix")->setValue(_worldViewProjectionMatrix);
}

void CameraTexture::render(float elapsedTime) {
    model->draw();
}
