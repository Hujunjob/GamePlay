# Copies files for the given game into the target res directory
#   GAME_NAME name of the game
#   REL_DIR to which directory these files are relative
#   SRC_FILES which files from the REL_DIR to copy (GLOB)
macro(COPY_RES_FILES GAME_NAME GAME_RES_TARGET REL_DIR SRC_FILES)
    file( GLOB_RECURSE RES_FILES RELATIVE ${REL_DIR} ${SRC_FILES} )

    set(ALL_FILES)
    foreach(SRC_FILE ${RES_FILES})
        add_custom_command(
                OUTPUT "${CMAKE_CURRENT_BINARY_DIR}/${SRC_FILE}"
                COMMAND ${CMAKE_COMMAND} -E copy_if_different
                "${REL_DIR}/${SRC_FILE}"
                "${CMAKE_CURRENT_BINARY_DIR}/${SRC_FILE}"
                COMMENT "Copy ${SRC_FILE}"
        )
        list(APPEND ALL_FILES "${CMAKE_CURRENT_BINARY_DIR}/${SRC_FILE}" )
    endforeach()
    # create target for copying these files
    add_custom_target( ${GAME_RES_TARGET} DEPENDS ${ALL_FILES} )
endmacro()

# convenience to call above with current directory and everything in "res"
macro(COPY_RES GAME_NAME)
    # a target for all addition asserts (will be done in default compile, but if you target the executable
    # it won't be done -- good for testing)
    add_custom_target( ${GAME_NAME}_ASSETS ALL )

    # copy entire "res" directory and "game.config" if there is one
    set(CRG_PATTERN "res/*" "game.config")
    COPY_RES_FILES( ${GAME_NAME} ${GAME_NAME}_CORE_RES
            ${CMAKE_CURRENT_SOURCE_DIR}
            "${CRG_PATTERN}"
            )
    add_dependencies( ${GAME_NAME}_ASSETS ${GAME_NAME}_CORE_RES )
endmacro()

# Copies resources from an additional directory
#   GAME_NAME name of the game
#   REL_DIR from which directory
#   ARGN which patterns to copy (should include res/ in name if to be placed in the res/ output)
macro(COPY_RES_EXTRA GAME_NAME REL_DIR)
    # convert src's to full paths (based on rel_dir)
    set(SRC_FILES)
    foreach(SRC_FILE ${ARGN} )
        list(APPEND SRC_FILES "${REL_DIR}/${SRC_FILE}")
    endforeach()

    COPY_RES_FILES( ${GAME_NAME} ${GAME_NAME}_EXTRA_RES ${REL_DIR} "${SRC_FILES}" )
    add_dependencies( ${GAME_NAME}_ASSETS ${GAME_NAME}_EXTRA_RES )
endmacro()

# Build a list of file matching a list of regular expression within a given path
#   RESULT name of the global variable into which the result is built
#   REL_PATH the path under which the files are to be found
#   ARGN list of patterns
macro(MAKE_ABSOLUTE RESULT REL_PATH)
    set(${RESULT})
    foreach(SRC_FILE ${ARGN} )
        file(GLOB XX ${REL_PATH}/${SRC_FILE})
        list(APPEND ${RESULT} ${XX})
    endforeach()
endmacro()

# Build a list of resource files matching a list of regular expression within a given path
# and mark their correct Mac OS X destination, keeping the relative position to a base path
#   RESULT name of the global variable into which the result is built
#   BASE_SRC_PATH the base path under which the structure is replicated in the bundle
#   REL_PATH a pattern to be matched and searched recursively.
macro(COPY_RES_MAC RESULT BASE_SRC_PATH)
    set(${RESULT})
    foreach(PATTERN ${ARGN} )
        file(GLOB_RECURSE XX RELATIVE ${BASE_SRC_PATH} ${BASE_SRC_PATH}/${PATTERN})
        foreach(FNAME ${XX})
            list(APPEND ${RESULT} ${BASE_SRC_PATH}/${FNAME})
            get_filename_component(DIR ${FNAME} DIRECTORY )
            set_source_files_properties(
                    ${BASE_SRC_PATH}/${FNAME}
                    PROPERTIES
                    MACOSX_PACKAGE_LOCATION Resources/${DIR}
            )
        endforeach()
    endforeach()
endmacro()

include_directories(
        ${CMAKE_SOURCE_DIR}/gameplay/src
        ${CMAKE_SOURCE_DIR}/external-deps/include
)

if(${CMAKE_SYSTEM_NAME} MATCHES "Darwin")
    find_package(OpenGL REQUIRED)
    FIND_LIBRARY(AGL_LIBRARY AGL)
    FIND_LIBRARY(APP_SERVICES_LIBRARY ApplicationServices )
    FIND_LIBRARY(ATBOX_LIBRARY AudioToolbox)
    FIND_LIBRARY(CARBON_LIBRARY Carbon)
    FIND_LIBRARY(CAUDIO_LIBRARY CoreAudio)
    FIND_LIBRARY(COREVIDEO_LIBRARY CoreVideo)
    FIND_LIBRARY(CFOUNDATION_LIBRARY CoreFoundation)
    FIND_LIBRARY(CSERVICES_LIBRARY CoreServices)
    FIND_LIBRARY(OPENGL_LIBRARY OpenGL)
    FIND_LIBRARY(QUICKTIME_LIBRARY QuickTime )
    FIND_LIBRARY(IOKIT_LIBRARY IOKit )
    FIND_LIBRARY(AVF_LIBRARY AVFoundation)
    FIND_LIBRARY(OAL_LIBRARY OpenAL)
    FIND_LIBRARY(GKIT_LIBRARY GameKit)
    SET(FRAMEWORK_LIBRS
            ${AGL_LIBRARY}
            ${APP_SERVICES_LIBRARY}
            ${ATBOX_LIBRARY}
            ${CARBON_LIBRARY}
            ${CAUDIO_LIBRARY}
            ${COREVIDEO_LIBRARY}
            ${CFOUNDATION_LIBRARY}
            ${CSERVICES_LIBRARY}
            ${OAL_LIBRARY}
            ${OPENGL_LIBRARIES}
            ${GKIT_LIBRARY}
            ${IOKIT_LIBRARY}
            "-framework Foundation"
            "-framework Cocoa")
    link_directories(${CMAKE_SOURCE_DIR}/external-deps/lib/macosx/x86_64)
    set(GAMEPLAY_LIBRARIES
            stdc++
            gameplay
            android
            gameplay-deps
            m
            dl
            pthread
            ${FRAMEWORK_LIBRS}
            )
ELSE(${CMAKE_SYSTEM_NAME} MATCHES "Darwin")
    add_definitions(-D__linux__)

    IF(ARCH_DIR STREQUAL "x64")
        link_directories(${CMAKE_SOURCE_DIR}/external-deps/lib/linux/x86_64)
    ELSE()
        link_directories(${CMAKE_SOURCE_DIR}/external-deps/lib/linux/x86)
    ENDIF(ARCH_DIR STREQUAL "x64")


    set(GAMEPLAY_LIBRARIES
            stdc++
            gameplay
            android
            gameplay-deps
            m
            GL
            rt
            dl
            X11
            pthread
            gtk-x11-2.0
            glib-2.0
            gobject-2.0
            )
ENDIF(${CMAKE_SYSTEM_NAME} MATCHES "Darwin")

link_directories(${CMAKE_SOURCE_DIR}/external-deps/lib/android/armeabi-v7a)

set(GAMEPLAY_LIBRARIES
        stdc++
        gameplay
        gameplay-deps
        GLESv3
        OpenSLES
        EGL
        log
        android
        m
        dl
        )

add_definitions(-std=c++11)

add_subdirectory(browser)
#add_subdirectory(demo)
