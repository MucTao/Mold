package org.muc.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureFlashModeAuto
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.base64EncodedStringWithOptions
import platform.UIKit.UIView
import platform.darwin.NSObject
import kotlin.time.Clock

actual fun createCameraManager(): CameraManager = IosCameraManager()

@OptIn(ExperimentalForeignApi::class)
private class IosCameraManager : CameraManager {
    private val session by lazy { AVCaptureSession() }
    private val photoOutput by lazy { AVCapturePhotoOutput() }
    private val movieOutput by lazy { AVCaptureMovieFileOutput() }
    private var previewLayer: AVCaptureVideoPreviewLayer? = null

    @Composable
    override fun Preview(modifier: Modifier) {
        DisposableEffect(Unit) {
            configure()
            session.startRunning()
            onDispose { release() }
        }
        UIKitView(
            factory = {
                // CGRectZero
                UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)).also { view ->
                    previewLayer = AVCaptureVideoPreviewLayer(session = session).also { layer ->
                        layer.frame = view.bounds
                        view.layer.addSublayer(layer)
                    }
                }
            },
            modifier = modifier,
            update = { view -> previewLayer?.frame = view.bounds }
        )
    }

    private fun configure() {
        if (session.inputs.isNotEmpty()) return
        session.beginConfiguration()
        session.sessionPreset = AVCaptureSessionPresetHigh
        AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)?.let { device ->
            AVCaptureDeviceInput.deviceInputWithDevice(device, null)?.let { session.addInput(it) }
        }
        if (session.canAddOutput(photoOutput)) session.addOutput(photoOutput)
        if (session.canAddOutput(movieOutput)) session.addOutput(movieOutput)
        session.commitConfiguration()
    }

    override suspend fun takePhoto(onFail: (String) -> Unit): String? = suspendCancellableCoroutine { cont ->
        if (session.inputs.isEmpty()) {
            onFail("相机未初始化，请先调用Preview")
            cont.resume(null) { _, _, _ -> }
        }
        if (!session.canAddOutput(photoOutput)) {
            onFail("无法初始化拍照输出")
            cont.resume(null) { _, _, _ -> }
        }
        val settings = AVCapturePhotoSettings()
        settings.flashMode = AVCaptureFlashModeAuto
        photoOutput.capturePhotoWithSettings(settings, object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
            override fun captureOutput(output: AVCapturePhotoOutput, didFinishProcessingPhoto: AVCapturePhoto, error: NSError?) {
                if (error != null) {
                    onFail(error.toString())
                    cont.resume(null) { _, _, _ -> }
                    return
                }
                val nsData = didFinishProcessingPhoto.fileDataRepresentation()
                if (nsData == null) {
                    onFail("图片数据为空")
                    cont.resume(null) { _, _, _ -> }
                    return
                }
                cont.resume(nsData.base64EncodedStringWithOptions(0u)) { _, _, _ -> }
            }
        })
    }

    override suspend fun startRecording(onFail: (String) -> Unit, onSuccess: (suspend () -> String) -> Unit) {
        if (!session.canAddOutput(movieOutput)) {
            onFail("无法初始化录像输出"); return
        }
        val path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String + "/video_${Clock.System.now().toEpochMilliseconds()}.mov"
        movieOutput.startRecordingToOutputFileURL(platform.Foundation.NSURL.fileURLWithPath(path), recordingDelegate = RecordingDelegate { onSuccess { path } })
    }

    override fun stopRecording() {
        if (movieOutput.recording) movieOutput.stopRecording()
    }

    override fun release() {
        stopRecording(); if (session.running) session.stopRunning()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class RecordingDelegate(private val done: () -> Unit) : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {
    override fun captureOutput(output: AVCaptureFileOutput, didFinishRecordingToOutputFileAtURL: platform.Foundation.NSURL, fromConnections: List<*>, error: NSError?) {
        if (error == null) done()
    }
}
