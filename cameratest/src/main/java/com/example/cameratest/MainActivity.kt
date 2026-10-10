package com.example.cameratest

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var output: TextView
    private var thread: HandlerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        output = TextView(this).apply {
            textSize = 17f
            setPadding(24, 24, 24, 24)
            text = "Camera2 Test Ready"
        }

        val button = Button(this).apply {
            text = "Test Camera2"
            setOnClickListener { testCamera() }
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(output, ViewGroup.LayoutParams(-1, 0).apply {
                height = 0
            })
            addView(button)
        })

        output.text = "Package: $packageName\nPress Test Camera2"
    }

    private fun testCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 10)
            return
        }

        try {
            val manager = getSystemService(CameraManager::class.java)
            val ids = manager.cameraIdList
            output.text = "Camera IDs: ${ids.joinToString()}\nOpening first camera..."

            if (ids.isEmpty()) {
                output.text = "No cameras found"
                return
            }

            val id = ids.first()
            thread?.quitSafely()
            thread = HandlerThread("Camera2Test").also { it.start() }
            manager.openCamera(id, object : android.hardware.camera2.CameraDevice.StateCallback() {
                override fun onOpened(camera: android.hardware.camera2.CameraDevice) {
                    runOnUiThread {
                        output.text = "CAMERA2 OPEN SUCCESS\nID: $id\nPackage: $packageName"
                        Toast.makeText(this@MainActivity, "Camera2 open success", Toast.LENGTH_SHORT).show()
                    }
                    camera.close()
                }

                override fun onDisconnected(camera: android.hardware.camera2.CameraDevice) {
                    camera.close()
                    runOnUiThread { output.text = "Camera disconnected: $id" }
                }

                override fun onError(camera: android.hardware.camera2.CameraDevice, error: Int) {
                    camera.close()
                    runOnUiThread { output.text = "Camera2 error: $error" }
                }
            }, Handler(thread!!.looper))
        } catch (e: Exception) {
            output.text = "Camera2 test failed: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            testCamera()
        } else {
            output.text = "Camera permission denied"
        }
    }

    override fun onDestroy() {
        thread?.quitSafely()
        super.onDestroy()
    }
}
