package com.example.virtualcamera.hooking

import android.content.Context
import android.util.Log

object CameraCapabilityRuntimeTest {
    private const val TAG = "VirtualCameraCamera"

    fun run(context: Context) {
        try {
            val cameras = CameraCapabilityReporter.inspect(context)
            Log.i(TAG, "Camera count: ${cameras.size}")

            cameras.forEach { camera ->
                Log.i(
                    TAG,
                    "id=${camera.cameraId}, level=${camera.camera2HardwareLevel}, " +
                        "backwardCompatible=${camera.backwardCompatible}, " +
                        "camera1Accessible=${camera.camera1Accessible}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Capability test failed: ${e.javaClass.simpleName}", e)
        }
    }
}
