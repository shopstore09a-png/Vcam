package com.example.virtualcamera

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val bg = Color.rgb(17, 19, 27)
    private val panel = Color.rgb(29, 32, 43)
    private val accent = Color.rgb(115, 104, 255)
    private var selectedVideo: Uri? = null
    private lateinit var content: LinearLayout

    private val pickVideo = 101
    private val pickImage = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun base(title: String) {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(18, 16, 18, 12)
        }

        val heading = TextView(this).apply {
            text = title
            textSize = 26f
            setTextColor(Color.WHITE)
            setPadding(4, 12, 4, 18)
        }
        outer.addView(heading)

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(content)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf("Home", "Library", "Overlay", "Presets", "Settings").forEach { item ->
            val b = Button(this).apply {
                text = item
                textSize = 10f
                setOnClickListener {
                    when (item) {
                        "Home" -> showHome()
                        "Library" -> showLibrary()
                        "Overlay" -> showOverlay()
                        "Presets" -> showPresets()
                        "Settings" -> showSettings()
                    }
                }
            }
            nav.addView(b, LinearLayout.LayoutParams(0, -2, 1f))
        }
        outer.addView(nav)
        setContentView(outer)
    }

    private fun label(text: String, size: Float = 16f) {
        content.addView(TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            setPadding(12, 12, 12, 12)
            setBackgroundColor(panel)
        }, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = 10
        })
    }

    private fun button(text: String, action: () -> Unit) {
        content.addView(Button(this).apply {
            this.text = text
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = 8
        })
    }

    private fun showHome() {
        base("VirtualCamera")
        label("ANDROID SUPPORT TARGET\\nAndroid 14 • 15 • 16 • 17")
        label("ROOT STATUS\\nNot checked yet")
        label("MODULE STATUS\\nHook engine not implemented")
        label("OVERLAY STATUS\\nUse Overlay to preview controls")
        label("MIC STATUS\\nNo audio interception enabled")
        button("Open Video Library") { showLibrary() }
        button("Configure Overlay") { showOverlay() }
        label("Note: Camera hooking is not active in this version.", 13f)
    }

    private fun showLibrary() {
        base("Video Library")
        label("Selected video: ${selectedVideo?.lastPathSegment ?: "None"}")
        button("Import Video") {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "video/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(i, pickVideo)
        }
        button("Import Image") {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(i, pickImage)
        }
        button("Preview Selected Video") {
            val uri = selectedVideo
            if (uri == null) {
                Toast.makeText(this, "Pehle video import karo", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "video/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    })
                } catch (_: Exception) {
                    Toast.makeText(this, "Video preview open nahi hua", Toast.LENGTH_SHORT).show()
                }
            }
        }
        button("Save Current Preset") {
            Toast.makeText(this, "Preset saving next stage mein implement hoga", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOverlay() {
        base("Overlay Controls")
        label("Overlay preview (in-app only)")
        label("Image/video overlay ko floating system window banana abhi implement nahi hua.")
        button("Move Left") { label("Left control selected") }
        button("Move Right") { label("Right control selected") }
        button("Zoom In") { label("Zoom-in control selected") }
        button("Zoom Out") { label("Zoom-out control selected") }
        button("Pause / Replay") {
            Toast.makeText(this, "Playback controls next stage mein add honge", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPresets() {
        base("Presets")
        label("No saved presets yet")
        label("Video selection aur settings ko permanent preset ke roop mein save karna next stage hai.")
        button("Open Library") { showLibrary() }
    }

    private fun showSettings() {
        base("Settings")
        label("Target Android: 14–17")
        label("App version: 0.2.0")
        label("Camera hook engine: Not installed")
        label("Framework adapter: Not configured")
        label("Permissions are requested only when required.")
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data?.data != null) {
            val uri = data.data!!
            if (requestCode == pickVideo) {
                selectedVideo = uri
                contentDescription = uri.toString()
                Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show()
                showLibrary()
            } else if (requestCode == pickImage) {
                Toast.makeText(this, "Image selected: ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
