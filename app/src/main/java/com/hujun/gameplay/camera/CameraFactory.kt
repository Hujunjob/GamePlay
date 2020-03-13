package com.hujun.gameplay.camera
import android.content.Context

/**
 * Created by junhu on 2019-11-14
 */
object CameraFactory {
    enum class CameraType {
        CAMERA1,
        CAMERA2,
    }

    lateinit var cameraEngine: ICameraEngine

    fun createCameraEngine(cameraType: CameraType, context: Context): ICameraEngine? {
        cameraEngine = when (cameraType) {
            CameraType.CAMERA1 -> {
                CameraEngine.getInstance(context)
            }
            CameraType.CAMERA2 -> {
                Camera2Engine.getInstance(context)
            }
        }
        return cameraEngine
    }
}