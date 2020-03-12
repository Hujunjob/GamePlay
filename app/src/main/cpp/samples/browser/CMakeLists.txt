set(GAME_NAME sample-browser)

set(GAME_SRC
        src/android_native_app_glue.cpp
        src/android_native_app_glue.h
        src/Audio3DSample.cpp
        src/Audio3DSample.h
        src/AudioSample.cpp
        src/AudioSample.h
        src/BillboardSample.cpp
        src/BillboardSample.h
        src/FirstPersonCamera.cpp
        src/FirstPersonCamera.h
        src/FontSample.cpp
        src/FontSample.h
        src/FormsSample.cpp
        src/FormsSample.h
        src/GamepadSample.cpp
        src/GamepadSample.h
        src/GestureSample.cpp
        src/GestureSample.h
        src/Grid.cpp
        src/Grid.h
        src/InputSample.cpp
        src/InputSample.h
        src/LightSample.cpp
        src/LightSample.h
        src/MeshBatchSample.cpp
        src/MeshBatchSample.h
        src/MeshPrimitiveSample.cpp
        src/MeshPrimitiveSample.h
        src/ParticlesSample.cpp
        src/ParticlesSample.h
        src/PhysicsCollisionObjectSample.cpp
        src/PhysicsCollisionObjectSample.h
        src/PostProcessSample.cpp
        src/PostProcessSample.h
        src/Sample.cpp
        src/Sample.h
        src/SamplesGame.cpp
        src/SamplesGame.h
        src/MyGame.h
        src/MyGame.cpp
        src/CameraTexture.cpp
        src/CameraTexture.h
        src/SceneCreateSample.cpp
        src/SceneCreateSample.h
        src/SceneLoadSample.cpp
        src/SceneLoadSample.h
        src/SpriteBatchSample.cpp
        src/SpriteBatchSample.h
        src/SpriteSample.cpp
        src/SpriteSample.h
        src/TerrainSample.cpp
        src/TerrainSample.h
        src/TextureSample.cpp
        src/TextureSample.h
        src/TriangleSample.cpp
        src/TriangleSample.h
        src/WaterSample.cpp
        src/WaterSample.h
        )

add_library( # Sets the name of the library.
        ${GAME_NAME}

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${GAME_SRC} )

target_link_libraries(${GAME_NAME} ${GAMEPLAY_LIBRARIES})


set_target_properties(${GAME_NAME} PROPERTIES
        OUTPUT_NAME "${GAME_NAME}"
        CLEAN_DIRECT_OUTPUT 1
        )
