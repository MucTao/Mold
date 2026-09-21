@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.Parameters.FLASH_MODE_OFF
import android.hardware.Camera.Parameters.FLASH_MODE_TORCH
import android.util.Log

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2018/04/27
 * desc  : utils about flashlight
 * </pre> *
 */
object FlashlightUtils {
    private lateinit var mCamera: Camera
    private var mSurfaceTexture: SurfaceTexture? = null

    val isFlashlightEnable: Boolean
        /**
         * Return whether the device supports flashlight.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = Utils.app.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    val isFlashlightOn: Boolean
        /**
         * Return whether the flashlight is working.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            if (!init()) return false
            val parameters: Camera.Parameters = mCamera.parameters
            return FLASH_MODE_TORCH == parameters.flashMode
        }

    /**
     * Turn on or turn off the flashlight.
     *
     * @param isOn True to turn on the flashlight, false otherwise.
     */
    fun setFlashlightStatus(isOn: Boolean) {
        if (!init()) return
        val parameters: Camera.Parameters =
            mCamera.getParameters()
        if (isOn) {
            if (FLASH_MODE_TORCH != parameters.flashMode) {
                runCatching {
                    mCamera.setPreviewTexture(
                        mSurfaceTexture
                    )
                    mCamera.startPreview()
                    parameters.flashMode = FLASH_MODE_TORCH
                    mCamera.setParameters(parameters)
                }.onFailure { e ->
                    e.printStackTrace()
                }
            }
        } else {
            if (FLASH_MODE_OFF != parameters.flashMode) {
                parameters.flashMode = FLASH_MODE_OFF
                mCamera.setParameters(parameters)
            }
        }
    }

    /** Destroy the flashlight. */
    fun destroy() {
        if (::mCamera.isInitialized.not()) return
        mCamera.release()
        mSurfaceTexture = null
    }

    private fun init(): Boolean {
        if (::mCamera.isInitialized.not()) {
            runCatching {
                FlashlightUtils.mCamera = Camera.open(0)
                FlashlightUtils.mSurfaceTexture = SurfaceTexture(0)
            }.getOrElse { t ->
                Log.e("FlashlightUtils", "init failed: ", t)
                false
            }
        }
        if (::mCamera.isInitialized.not()) {
            Log.e("FlashlightUtils", "init failed.")
            return false
        }
        return true
    }
}
