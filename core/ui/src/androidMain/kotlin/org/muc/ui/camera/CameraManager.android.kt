package org.muc.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.muc.ui.status.ErrorView
import java.io.File

actual fun createCameraManager(): CameraManager = AndroidCameraController()


class AndroidCameraController : CameraManager {
    private lateinit var appContext: Context
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val mainExecutor by lazy { ContextCompat.getMainExecutor(appContext) }
    private val permissionMap by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        mutableStateMapOf(
            Manifest.permission.CAMERA to (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED),
            Manifest.permission.RECORD_AUDIO to (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED),
        )
    }

    @Composable
    override fun Preview(modifier: Modifier) {
        val context = LocalContext.current
        appContext = context.applicationContext
        val lifecycleOwner = LocalLifecycleOwner.current
        val permissionLaunch = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            map.forEach {
                permissionMap[it.key] = it.value
            }
        }
        DisposableEffect(Unit) {
            permissionMap.keys.forEach {
                permissionMap[it] = ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
            }
            if (permissionMap[Manifest.permission.CAMERA] != true)
                permissionLaunch.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            onDispose {
                release()
            }
        }
        if (permissionMap[Manifest.permission.CAMERA] == true && permissionMap[Manifest.permission.RECORD_AUDIO] == true)
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = modifier,
                update = { previewView ->
                    val future = ProcessCameraProvider.getInstance(context)
                    future.addListener({
                        val provider = future.get()
                        cameraProvider = provider
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        val recorder = Recorder.Builder()
                            .setQualitySelector(QualitySelector.from(Quality.HD))
                            .build()
                        videoCapture = VideoCapture.withOutput(recorder)

                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview, imageCapture, videoCapture
                        )
                    }, mainExecutor)
                }
            )
        else {
            ErrorView("未授予相机权限，无法使用相册", {
                permissionLaunch.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            })
        }
    }

    override suspend fun takePhoto(onFail: (String) -> Unit): String? = suspendCancellableCoroutine { cont ->
        if (::appContext.isInitialized) {
            if (permissionMap[Manifest.permission.CAMERA] == true) {
                val file = File(appContext.filesDir, "photo_${System.currentTimeMillis()}.jpg")
                val output = ImageCapture.OutputFileOptions.Builder(file).build()
                imageCapture?.takePicture(
                    output, mainExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            cont.resume(file.path) { _, _, _ -> }
                        }

                        override fun onError(exc: ImageCaptureException) {
                            onFail(exc.message ?: exc.toString())
                            cont.resume(null) { _, _, _ -> }
                        }
                    }
                )
            } else {
                onFail("没有相机权限")
                cont.resume(null) { _, _, _ -> }
            }
        } else {
            onFail("相机未初始化，请先调用AndroidCameraController.Preview方法")
            cont.resume(null) { _, _, _ -> }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override suspend fun startRecording(onFail: (String) -> Unit, onSuccess: (suspend () -> String) -> Unit) {
        if (::appContext.isInitialized) {
            if (permissionMap[Manifest.permission.CAMERA] == true && permissionMap[Manifest.permission.RECORD_AUDIO] == true) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}")
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/课堂评价智能体")
                }

                val mediaStoreOutput = MediaStoreOutputOptions.Builder(
                    appContext.contentResolver,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                    .setContentValues(contentValues).build()
                recording = videoCapture?.output
                    ?.prepareRecording(appContext, mediaStoreOutput)
                    ?.withAudioEnabled()
                    ?.start(mainExecutor) { event ->
                        // 可选：监听录制事件
                        if (event is VideoRecordEvent.Finalize) {
                            onSuccess {
                                event.outputResults.outputUri.copyToPrivateFile(appContext).absolutePath
                            }
                        }
                    }
            } else {
                if (permissionMap[Manifest.permission.CAMERA] != true)
                    onFail("没有相机权限")
                if (permissionMap[Manifest.permission.RECORD_AUDIO] != true)
                    onFail("没有录音权限")
            }
        } else {
            onFail("相机未初始化，请先调用AndroidCameraController.Preview方法")
        }
    }

    override fun stopRecording() {
        recording?.stop()
        recording = null
    }

    override fun release() {
        recording?.stop()
        cameraProvider?.unbindAll()
    }

    private suspend fun Uri.copyToPrivateFile(context: Context): File = withContext(Dispatchers.IO) {
        val target = File(context.filesDir, "video_${System.currentTimeMillis()}.mp4")
        context.contentResolver.openInputStream(this@copyToPrivateFile)?.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        target
    }
}
