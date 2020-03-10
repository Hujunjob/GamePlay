set(GAME_NAME sample-browser)

set(GAME_SRC
        src/android_native_app_glue.cpp
        src/android_native_app_glue.h
        src/MyGame.cpp
        src/MyGame.h
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
