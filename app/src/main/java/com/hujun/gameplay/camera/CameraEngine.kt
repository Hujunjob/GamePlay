package com.hujun.gameplay.camera
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View

/**
 * Created by junhu on 2019-11-14
 */
class CameraEngine private constructor(var context: Context) : ICameraEngine {
    private val surfaceTexture = SurfaceTexture(-1)

    override fun getPreviewWidth(): Int {
        return previewWidth
    }

    override fun getPreviewHeight(): Int {
        return previewHeight
    }

    private var frameListener: ICameraEngine.OnNewFrameListener? = null

    override fun onStop() {

    }

    override fun onStart() {
    }

    override fun addSurfaceView(surfaceTexture: SurfaceTexture) {
        Log.d(TAG, "addSurfaceView: ")
//        mCamera?.setPreviewTexture(surfaceTexture) //如果这个texture并不是在屏幕上的，则是离屏渲染
    }

    override fun removeSurfaceView(surfaceTexture: SurfaceTexture) {
        Log.d(TAG, "removeSurfaceView: ")
        closeCamera()
    }

    override fun setSurfaceHolder(surfaceHolder: SurfaceHolder) {
        mCamera?.setPreviewDisplay(surfaceHolder)
    }

    private var mCamera: Camera? = null
    private var frontId = -1
    private var backId = -1

    private var previewWidth = 1280
    private var previewHeight = 720

    companion object {
        var CAMERA_OPEN = 1
        var CAMERA_OPEN_FAIL = 2
        var CAMERA_CLOSE = 3
        var MESSAGE = 4
        private val TAG = this::class.java.name.replace("${'$'}Companion", "").split(".").last()

        private var instance: CameraEngine? = null
        fun getInstance(context: Context): CameraEngine {
            if (instance == null) {
                instance = CameraEngine(context)
            }
            return instance!!
        }
    }

    init {
        getCameraId()
        mCamera = Camera.open(0)
    }

    override fun openCamera(isFront: Boolean): Boolean {
        Log.d(TAG, "openCamera: ")

        mCamera?.parameters!!.supportedPreviewSizes.forEach {
            Log.d(TAG, "openCamera: supported preview size : w=${it.width},h=${it.height}")
        }
        var params = mCamera?.parameters!!

        params.setPreviewSize(previewWidth, previewHeight)
        params.previewFormat = ImageFormat.NV21
        if (params.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
        }
//        var bytes = Array<Byte>((1280*720*1.5).toInt(),)
//        var surfaceTexture = SurfaceTexture(-1)
//        camera.setPreviewTexture(surfaceTexture)

        val pixelformat = params.getPreviewFormat()
        val pixelinfo = PixelFormat()
        PixelFormat.getPixelFormatInfo(pixelformat, pixelinfo)
        val bufSize = previewWidth * previewHeight * pixelinfo.bitsPerPixel / 8
        for (i in 0..4) {
            mCamera?.addCallbackBuffer(ByteArray(bufSize))
        }

//        for (i in 0..2) {
//            var byteArray = ByteArray((previewWidth * previewHeight * 1.5).toInt())
//            camera.addCallbackBuffer(byteArray)
//        }
        CameraConfigurationUtils.setBestPreviewFPS(params, 10, 20)

        mCamera?.parameters = params
        setCameraDisplayOrientation()
        mCamera?.setPreviewCallbackWithBuffer { data, camera ->
            if (mCamera == null) {
                return@setPreviewCallbackWithBuffer
            }
            var size = camera.parameters.previewSize
            frameListener?.onNewFrame(data, previewWidth, previewHeight)
//            Log.d(TAG, "preview: w=${size.width},h=${size.height},len=${data.size}")
            camera.addCallbackBuffer(data)
        }

        mCamera?.setPreviewTexture(surfaceTexture) //如果这个texture并不是在屏幕上的，则是离屏渲染

        var cameraInfo = Camera.CameraInfo()
        var orientation = cameraInfo.orientation
        Log.d(TAG, "openCamera: orientation=$orientation")
        try {
            mCamera?.startPreview()
        } catch (error: Throwable) {
            error.printStackTrace()
        }

        return true
    }

    override fun setPreviewView(view: View) {

    }

    fun setCameraDisplayOrientation() {
        var rotation = (context as Activity).windowManager.defaultDisplay.rotation
        var degree = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        var result = 0
        var info = Camera.CameraInfo()
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360
            result = (360 - result) % 360
        } else {
            result = (90 - degree + 360) % 360
        }

        Log.d(
            TAG,
            "setCameraDisplayOrientation: camera orientation=${info.orientation},result= $result"
        )
        mCamera?.setDisplayOrientation(result)
    }

    override fun setResolution(width: Int, height: Int) {

    }

    private fun getCameraId() {
        var cameraInfo = Camera.CameraInfo()
        var num = Camera.getNumberOfCameras()
        for (i in 0 until num) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontId = i
            }
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backId = i
            }
        }
    }

    override fun closeCamera(): Boolean {
        Log.d(TAG, "closeCamera: ")
        mCamera?.stopPreview()
//        camera.release()
        return true
    }

    override fun switchCamera(): Boolean {
        Log.d(TAG, "switchCamera: ")
        return true
    }

    override fun setOnNewFrameListener(onNewFrame: ICameraEngine.OnNewFrameListener) {
        frameListener = onNewFrame
    }

    override fun release() {
        Log.d(TAG, "release: ")
        closeCamera()
        mCamera?.release()
        mCamera = null
    }
}