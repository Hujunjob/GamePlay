package com.hujun.gameplay.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.annotation.WorkerThread
import java.lang.Exception
import java.lang.RuntimeException

/**
 * Created by junhu on 2019-11-13
 */
class Camera2Engine private constructor(val context: Context) : ICameraEngine {
    override fun getPreviewWidth(): Int {
        return previewWidth
    }

    override fun getPreviewHeight(): Int {
        return previewHeight
    }

    private val isG200 = false

    private var cameraManager: CameraManager
    private var frontCameraId: String? = null
    private var backCameraId: String? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    private var mainHandler: CameraHandler? = null

    private var cameraDevice: CameraDevice? = null
    private var cameraSession: CameraCaptureSession? = null

    private lateinit var frontCameraCharacteristics: CameraCharacteristics
    private lateinit var backCameraCharacteristics: CameraCharacteristics

    private var previewWidth = 640
    private var previewHeight = 480

    private var sensorOrientation = 0

    private var mContext: Context = context

    private lateinit var imageReader: ImageReader

    private lateinit var frameListener: ICameraEngine.OnNewFrameListener

    private var previewView: View? = null

    companion object {
        private val TAG = this::class.java.name.replace("${'$'}Companion", "").split(".").last()


        var CAMERA_OPEN = 1
        var CAMERA_OPEN_FAIL = 2
        var CAMERA_CLOSE = 3
        var MESSAGE = 4

        private var instance: Camera2Engine? = null
        fun getInstance(context: Context): Camera2Engine {
            if (instance == null) {
                instance = Camera2Engine(context)
            }
            return instance!!
        }
    }

    init {
        mainHandler = CameraHandler()
    }

    /**
     * 判断相机的 Hardware Level 是否大于等于指定的 Level。
     */
    fun CameraCharacteristics.isHardwareLevelSupported(requiredLevel: Int): Boolean {
        val sortedLevels = intArrayOf(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
        )
        val deviceLevel = this[CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL]
        if (requiredLevel == deviceLevel) {
            return true
        }
        for (sortedLevel in sortedLevels) {
            if (requiredLevel == sortedLevel) {
                return true
            } else if (deviceLevel == sortedLevel) {
                return false
            }
        }
        return false
    }

    init {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var cameraList = cameraManager.cameraIdList
        cameraList.forEach {
            var front = false
            var cameraCharacter = cameraManager.getCameraCharacteristics(it)
            var facing = cameraCharacter[CameraCharacteristics.LENS_FACING]
            if (isG200) {
                if (it.equals("1")) {
                    facing = CameraCharacteristics.LENS_FACING_FRONT
                } else {
                    facing = CameraCharacteristics.LENS_FACING_BACK
                }
            }
            if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                front = true
                frontCameraId = it
                frontCameraCharacteristics = cameraCharacter
            }
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                backCameraId = it
                backCameraCharacteristics = cameraCharacter
            }
            if (it.equals("1")){
                backCameraId = it
                backCameraCharacteristics = cameraCharacter
            }

            var support =
                cameraCharacter.isHardwareLevelSupported(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)
            println("isHardwareLevelSupported id=${it},front=$front,support = $support")
        }
    }

    override fun onStart() {

    }

    override fun onStop() {

    }

    override fun setPreviewView(view: View) {
        previewView = view
    }

    private fun startBackgoundThread() {
        backgroundThread = HandlerThread("CameraThread").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {
        try {
            backgroundThread?.quitSafely()
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*************************************** 在子线程里操作相机  **************************************/
    override fun setResolution(width: Int, height: Int) {
        previewHeight = height
        previewWidth = width
    }

    @SuppressLint("MissingPermission")
    override fun openCamera(isFront: Boolean): Boolean {
        if (cameraDevice != null) {
            toast("相机已经开启")
            return false
        }
        startBackgoundThread()
        var cameraId = if (isFront.not()) backCameraId else frontCameraId
        cameraId = "1"
        Thread(Runnable {
            if (cameraId != null) {
                cameraManager.openCamera(cameraId, CameraCallback(), backgroundHandler)
            } else {
                throw RuntimeException("camera can not open")
            }
        }).start()
        return true
    }

    override fun switchCamera(): Boolean {
        return true
    }

    override fun setOnNewFrameListener(onNewFrame: ICameraEngine.OnNewFrameListener) {
        frameListener = onNewFrame
    }

    override fun release() {
        closeCamera()
    }


    override fun closeCamera(): Boolean {
        if (cameraDevice == null) {
            toast("相机未开启")
            return false
        }
        Thread(Runnable {
            cameraSession?.stopRepeating()
            cameraDevice?.close()
            stopBackgroundThread()
        }).start()
        return true
    }

    inner class CameraHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                CAMERA_OPEN -> {
                    toast("相机开启成功")
                }
                CAMERA_OPEN_FAIL -> {
                    toast("相机开启失败")
                }
                MESSAGE -> {
                    val str = msg.obj as String
                    Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show()
                }
                CAMERA_CLOSE -> {
                    toast("相机关闭成功")
                }
            }
        }
    }

    inner class CameraCallback : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            mainHandler?.sendEmptyMessage(CAMERA_OPEN)
            println("open success")
            startPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
            mainHandler?.sendEmptyMessage(CAMERA_OPEN_FAIL)
        }

        override fun onClosed(camera: CameraDevice) {
            super.onClosed(camera)
            cameraDevice = null
            mainHandler?.sendEmptyMessage(CAMERA_CLOSE)
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
            mainHandler?.sendEmptyMessage(CAMERA_OPEN_FAIL)
        }
    }

    inner class SesstionStateCallback : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {

        }

        override fun onConfigured(session: CameraCaptureSession) {
            cameraSession = session
            startCapture()
        }

    }

    var surfaceViews = mutableListOf<SurfaceTexture>()
    var surfaces = mutableListOf<Surface>()

    override fun addSurfaceView(surfaceTexture: SurfaceTexture) {
        surfaceViews.add(surfaceTexture)
        surfaces.add(Surface(surfaceTexture))
    }

    override fun removeSurfaceView(surfaceTexture: SurfaceTexture) {
//        surfaceViews.remove(surfaceTexture)
        closeCamera()
    }

    override fun setSurfaceHolder(surfaceHolder: SurfaceHolder) {

    }


    @WorkerThread
    private fun startPreview() {
        val configMap =
            backCameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]!!
        val sizes = configMap.getOutputSizes(ImageFormat.YUV_420_888)
        sizes?.forEach {
            Log.d(TAG, "preview size: w=${it.width},h= ${it.height}")
        }

        sensorOrientation = backCameraCharacteristics[CameraCharacteristics.SENSOR_ORIENTATION]!!
        Log.d(TAG, "startPreview: sensorOrientation $sensorOrientation")

        surfaceViews.forEach {
            it.setDefaultBufferSize(previewWidth, previewHeight)
        }

//        configureTransform(previewView?.width!!,previewView?.height!!)
        val supportSize = getSupportedSize(sizes)
        Log.d(TAG, "startPreview: support size w=${supportSize.width},h=${supportSize.height}")
        val imageFormat = ImageFormat.YUV_420_888
        if (configMap.isOutputSupportedFor(imageFormat)) {
            imageReader =
                ImageReader.newInstance(supportSize.width, supportSize.height, imageFormat, 3)
                    .apply {
                        setOnImageAvailableListener(
                            OnPreviewDataAvailableListener(),
                            backgroundHandler
                        )
                    }
            surfaces.add(imageReader.surface)
        } else {
            println("不支持该格式：${imageFormat}")
        }

        cameraDevice?.createCaptureSession(
            surfaces,
            SesstionStateCallback(),
            backgroundHandler
        )
    }

    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `textureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `textureView` is fixed.
     *
     * @param viewWidth  The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        Log.d(TAG, "configureTransform: w=$viewWidth,h=$viewHeight")
        val rotation = (context as Activity).windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewHeight.toFloat(), previewWidth.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        var rotate = 0f
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = Math.max(
                viewHeight.toFloat() / previewHeight,
                viewWidth.toFloat() / previewWidth
            )

            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                rotate = (90 * (rotation - 2)).toFloat()
                postRotate(rotate, centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            rotate = 180f
            matrix.postRotate(rotate, centerX, centerY)
        }
        Log.d(TAG, "configureTransform: rotate=$rotate")
        (previewView as TextureView).setTransform(matrix)
    }


    //默认输出720p的，1280*720
    private fun getSupportedSize(sizes: Array<Size>): Size {
        sizes.forEach {
            it.apply {
                if (width > height) {
                    if (width == previewWidth && height == previewHeight) {
                        return it
                    }
                } else {
                    if (height == previewWidth && width == previewHeight) {
                        return it
                    }
                }
            }
        }
        return sizes[0]
    }

    private fun startCapture() {
        val requstBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)!!
        surfaces.forEach {
            requstBuilder.addTarget(it)
        }

        val request = requstBuilder.build()
        cameraSession?.setRepeatingRequest(
            request,
            RepeatingCaptureStateCallback(),
            backgroundHandler
        )
    }

    inner class RepeatingCaptureStateCallback : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
        }

        override fun onCaptureFailed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            failure: CaptureFailure
        ) {
            super.onCaptureFailed(session, request, failure)
        }

        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) {
            super.onCaptureStarted(session, request, timestamp, frameNumber)
        }
    }

    private fun toast(msg: String) {
        var message = mainHandler?.obtainMessage()
        message?.what = MESSAGE
        message?.obj = msg
        mainHandler?.sendMessage(message)
    }

    private inner class OnPreviewDataAvailableListener : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader?) {
            var image = reader?.acquireNextImage()!!
            val planes = image.planes!!
            planes[0].buffer
            frameListener.onBuffer(
                planes[0].buffer,
                planes[1].buffer,
                planes[2].buffer,
                reader.width,
                reader.height
            )
//            println("onImageAvailable")
//            Log.d(TAG, "onImageAvailable: w=${image.width},h=${image.height}")
            image.close()
        }
    }
}