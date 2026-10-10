package com.example.virtualcamera.hooking

import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

data class CameraCapability(
    val cameraId: String,
    val camera2HardwareLevel: String,
    val backwardCompatible: Boolean,
    val camera1Accessible: Boolean
)

object CameraCapabilityReporter {

    fun inspect(context: Context): List<CameraCapability> {
        val results = mutableListOf<CameraCapability>()
        val manager = context.getSystemService(
            Context.CAMERA_SERVICE
        ) as CameraManager

        for (id in manager.cameraIdList) {
            val characteristics =
                manager.getCameraCharacteristics(id)

            val level = characteristics.get(
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
            )

            val capabilities = characteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
            )

            @Suppress("DEPRECATION")
            val camera1Accessible = try {
                Camera.getNumberOfCameras() > 0
            } catch (_: Exception) {
                false
            }

            val levelName = when (level) {
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
                else -> "UNKNOWN"
            }

            results.add(
                CameraCapability(
                    cameraId = id,
                    camera2HardwareLevel = levelName,
                    backwardCompatible = capabilities?.contains(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE
                    ) == true,
                    camera1Accessible = camera1Accessible
                )
            )
        }

        return results
    }
}
