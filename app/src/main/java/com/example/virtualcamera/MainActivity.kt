package com.example.virtualcamera

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val bg = Color.rgb(17, 19, 27)
    private val panel = Color.rgb(29, 32, 43)
    private val prefs by lazy { getSharedPreferences("targets", MODE_PRIVATE) }
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
        outer.addView(TextView(this).apply {
            text = title
            textSize = 26f
            setTextColor(Color.WHITE)
            setPadding(4, 12, 4, 18)
        })
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER }
        listOf("Home", "Target Apps", "Library", "Settings").forEach { item ->
            nav.addView(Button(this).apply {
                text = item
                textSize = 10f
                setOnClickListener {
                    when (item) {
                        "Home" -> showHome()
                        "Target Apps" -> showTargets()
                        "Library" -> showLibrary()
                        "Settings" -> showSettings()
                    }
                }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }
        outer.addView(nav)
        setContentView(outer)
    }

    private fun label(text: String, size: Float = 15f) {
        content.addView(TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            setPadding(12, 12, 12, 12)
            setBackgroundColor(panel)
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 10 })
    }

    private fun button(text: String, action: () -> Unit) {
        content.addView(Button(this).apply {
            this.text = text
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
    }

    private fun showHome() {
        base("VirtualCamera")
        val targets = prefs.getStringSet("selected_packages", emptySet()) ?: emptySet()
        label("TARGET APPS\\n${targets.size} selected")
        label("MODULE STATUS\\nXposed entry loaded only when framework scope includes the target.")
        button("Select Target Apps") { showTargets() }
        button("Open Video Library") { showLibrary() }
        button("Settings") { showSettings() }
        label("Note: App selection is saved here. Enable VirtualCamera in Vector/LSPosed scope for each selected package. Camera API interception is a separate implementation step.", 13f)
    }

    private fun showTargets() {
        base("Select Target Apps")
        label("Choose the apps in which you want to activate the module. Package names are shown under app names.")
        val selected = (prefs.getStringSet("selected_packages", emptySet()) ?: emptySet()).toMutableSet()
        val apps = packageManager.getInstalledApplications(0)
            .filter { it.packageName != packageName }
            .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }
        label("Installed apps found: ${apps.size}")
        apps.forEach { app: ApplicationInfo ->
            val appName = packageManager.getApplicationLabel(app).toString()
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 8, 12, 8)
                setBackgroundColor(panel)
            }
            row.addView(CheckBox(this).apply {
                text = appName
                textSize = 16f
                setTextColor(Color.WHITE)
                isChecked = selected.contains(app.packageName)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selected.add(app.packageName) else selected.remove(app.packageName)
                }
            })
            row.addView(TextView(this).apply {
                text = app.packageName
                textSize = 12f
                setTextColor(Color.LTGRAY)
                setPadding(12, 0, 8, 8)
            })
            content.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
        }
        button("Save Selected Apps (${selected.size})") {
            prefs.edit().putStringSet("selected_packages", selected.toSet()).apply()
            Toast.makeText(this, "Target app list saved: ${selected.size}", Toast.LENGTH_SHORT).show()
            showHome()
        }
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
        label("Video source storage and camera frame injection are not connected yet.")
    }

    private fun showSettings() {
        base("Settings")
        label("App version: 0.3.0")
        label("Framework: Vector / LSPosed with libxposed API 102")
        label("For a target app, enable VirtualCamera in the framework's scope list and restart that app.")
        label("The in-app target list records your choices; it cannot grant framework scope by itself.")
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data?.data != null) {
            val uri = data.data!!
            if (requestCode == pickVideo) {
                selectedVideo = uri
                Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show()
                showLibrary()
            } else if (requestCode == pickImage) {
                Toast.makeText(this, "Image selected: ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
