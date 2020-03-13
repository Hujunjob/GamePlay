//
// Created by JUN HU on 2020/3/9.
//

#include <jni.h>
#include "MyGame.h"
#include "CameraTexture.h"

MyGame game;
int num;

MyGame::MyGame() : _font(NULL), _sample(NULL) {

}

void MyGame::initialize() {
    Game::initialize();
    _sample = new CameraSample();
    _sample->initialize();
    _font = Font::create("res/ui/arial.gpb");
}

void MyGame::finalize() {
    Game::finalize();
    SAFE_DELETE(_sample);
//    SAFE_DELETE(_font);
}

void MyGame::update(float elapsedTime) {
    Game::update(elapsedTime);
    if (_sample) {
        _sample->update(elapsedTime);
    }
}

void MyGame::render(float elapsedTime) {
    Game::render(elapsedTime);

    clear(CLEAR_COLOR_DEPTH, Vector4(0, 0, 0, 1), 1.0f, 0);

    if (_sample) {
        _sample->render(elapsedTime);
    }

    _font->start();

    char text[128];
    sprintf(text, "hello world %d", num);
    _font->drawText(text, 20, 20, Vector4(1, 0, 0, 1), 100);
    _font->finish();
}

extern "C"
{

//byte[] data, int width, int height
JNIEXPORT void JNICALL
Java_com_hujun_gameplay_GamePlayNativeActivity_onCameraFrame(JNIEnv *env, jclass clazz, jbyteArray
jdata, jint _width, jint
                                                             _height) {
    int width = _width;
    int height = _height;
    num++;

    if (game._sample != NULL) {
        jbyte *data = env->GetByteArrayElements(jdata, 0);
        game._sample->offerData(data, env->GetArrayLength(jdata), width, height);
        env->ReleaseByteArrayElements(jdata, data, 0);
    }
}
}