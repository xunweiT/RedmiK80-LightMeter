package com.redmik80.lightmeter.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraMeteringManager(private val context: Context) {

    private var cameraManager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private val _averageLuminance = MutableStateFlow(0.0)
    val averageLuminance: StateFlow<Double> = _averageLuminance.asStateFlow()

    private val _cameraEV = MutableStateFlow(0f)
    val cameraEV: StateFlow<Float> = _cameraEV.asStateFlow()

    init {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun openCamera(surface: Surface, onOpened: () -> Unit, onError: (String) -> Unit) {
        val cameraId = getBackCameraId()
        if (cameraId == null) {
            onError("No back camera found")
            return
        }

        try {
            val characteristics = cameraManager?.getCameraCharacteristics(cameraId)
            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val previewSize = map?.getOutputSizes(Surface::class.java)?.maxByOrNull {
                it.width * it.height
            } ?: Size(1920, 1080)

            imageReader = ImageReader.newInstance(
                previewSize.width,
                previewSize.height,
                ImageFormat.YUV_420_888,
                2
            )

            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                image?.let {
                    val luminance = calculateAverageLuminance(it)
                    _averageLuminance.value = luminance
                    _cameraEV.value = calculateEVFromLuminance(luminance)
                    it.close()
                }
            }, backgroundHandler)

            cameraManager?.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraPreviewSession(surface, onOpened)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    onError("Camera error: $error")
                }
            }, backgroundHandler)

        } catch (e: CameraAccessException) {
            onError("Camera access error: ${e.message}")
        } catch (e: SecurityException) {
            onError("Camera permission not granted")
        }
    }

    private fun createCameraPreviewSession(surface: Surface, onConfigured: () -> Unit) {
        val camera = cameraDevice ?: return
        val readerSurface = imageReader?.surface ?: return

        try {
            val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)
            previewRequestBuilder.addTarget(readerSurface)

            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )

            camera.createCaptureSession(
                listOf(surface, readerSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            session.setRepeatingRequest(
                                previewRequestBuilder.build(),
                                null,
                                backgroundHandler
                            )
                            onConfigured()
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                    }
                },
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }

    private fun getBackCameraId(): String? {
        return cameraManager?.cameraIdList?.find { id ->
            val characteristics = cameraManager?.getCameraCharacteristics(id)
            val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
            facing == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    private fun calculateAverageLuminance(image: android.media.Image): Double {
        val yPlane = image.planes[0]
        val buffer = yPlane.buffer
        val size = buffer.remaining()
        var sum = 0L

        val data = ByteArray(size)
        buffer.get(data)

        for (byte in data) {
            sum += (byte.toInt() and 0xFF)
        }

        return sum.toDouble() / size
    }

    private fun calculateEVFromLuminance(luminance: Double): Float {
        return (Math.log(luminance / 2.5) / Math.log(2.0)).toFloat()
    }

    fun getCameraInfo(): String {
        val cameraId = getBackCameraId() ?: return "No camera found"
        return try {
            val characteristics = cameraManager?.getCameraCharacteristics(cameraId)
            val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
            val focalLengths = characteristics?.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val apertures = characteristics?.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
            val isoRange = characteristics?.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val exposureRange = characteristics?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)

            buildString {
                appendLine("Camera ID: $cameraId")
                appendLine("Facing: ${if (facing == CameraCharacteristics.LENS_FACING_BACK) "Back" else "Front"}")
                focalLengths?.forEach { appendLine("Focal Length: ${it}mm") }
                apertures?.forEach { appendLine("Aperture: f/${it}") }
                isoRange?.let { appendLine("ISO Range: ${it.lower} - ${it.upper}") }
                exposureRange?.let {
                    appendLine("Exposure Range: ${it.lower}ns - ${it.upper}ns")
                }
            }
        } catch (e: CameraAccessException) {
            "Error getting camera info: ${e.message}"
        }
    }
}
