package com.example.virtualcamera.hooking

import android.hardware.Camera
import android.hardware.camera2.CameraManager

/**
 * Camera API diagnostics only.
 * Does not intercept or replace camera frames.
 */
object CameraApiDiagnostics {

    fun legacyCameraClassName(): String {
        return Camera::class.java.name
    }

    fun modernCameraManagerClassName(): String {
        return CameraManager::class.java.name
    }

    fun supportedApiNames(): List<String> {
        return listOf("Camera1", "Camera2")
    }
}
