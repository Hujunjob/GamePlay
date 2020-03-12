//
// Created by JUN HU on 2020/3/9.
//

#include "MyGame.h"
#include "CameraTexture.h"

MyGame game;

MyGame::MyGame() : _font(NULL),_sample(NULL) {

}

void MyGame::initialize() {
    Game::initialize();
    _sample = new CameraTexture();
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
    if(_sample){
        _sample->update(elapsedTime);
    }
}

void MyGame::render(float elapsedTime) {
    Game::render(elapsedTime);

    clear(CLEAR_COLOR_DEPTH,Vector4(0,0,0,1),1.0f,0);

    if(_sample){
        _sample->render(elapsedTime);
    }

    _font->start();

    char text[128];
    sprintf(text,"hello world %s","hujun");
    _font->drawText(text,20,20,Vector4(1,0,0,1),30);
    _font->finish();
}
