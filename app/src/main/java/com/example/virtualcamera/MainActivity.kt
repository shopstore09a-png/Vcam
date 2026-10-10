package com.example.virtualcamera

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.*
import android.widget.LinearLayout.LayoutParams

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 17, 19)
    private val card = Color.rgb(16, 29, 31)
    private val card2 = Color.rgb(25, 40, 42)
    private val green = Color.rgb(73, 218, 157)
    private val white = Color.rgb(239, 247, 246)
    private val muted = Color.rgb(151, 172, 172)
    private val prefs by lazy { getSharedPreferences("targets", MODE_PRIVATE) }
    private var selectedMedia: Uri? = null
    private var mediaType = "No source"
    private lateinit var page: LinearLayout
    private lateinit var body: LinearLayout
    private val pickVideo = 101
    private val pickImage = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedMedia = prefs.getString("media_uri", null)?.let { Uri.parse(it) }
        mediaType = prefs.getString("media_type", "No source") ?: "No source"
        showHome()
    }

    private fun shape(color: Int, radius: Float = 22f, stroke: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius * resources.displayMetrics.density
            if (stroke != 0) setStroke((1 * resources.displayMetrics.density).toInt(), stroke)
        }
    }

    private fun lp(w: Int = -1, h: Int = -2, weight: Float = 0f) =
        LayoutParams(w, h, weight)

    private fun text(
        value: String,
        size: Float = 15f,
        color: Int = white,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
    }

    private fun gap(height: Int) {
        body.addView(View(this), lp(h = height))
    }

    private fun startScreen(title: String, subtitle: String = "") {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this).apply {
            clipToPadding = false
            setPadding(0, 0, 0, 10)
            isFillViewport = false
        }
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
        }
        scroll.addView(page)
        root.addView(scroll, lp(h = 0, weight = 1f))

        body = page
        if (title == "DRVCAM") {
            val header = LinearLayout(this).apply {
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(4), dp(8), dp(4), dp(20))
            }
            val logo = text("♧", 29f, green, true).apply {
                gravity = Gravity.CENTER
                background = shape(card2, 15f, green)
            }
            header.addView(logo, lp(dp(48), dp(48)))
            val name = text("  DRVCAM", 29f, white, true)
            header.addView(name, lp(0, -2, 1f))
            header.addView(text("DR", 15f, green, true).apply {
                gravity = Gravity.CENTER
                background = shape(card2, 40f)
            }, lp(dp(48), dp(48)))
            body.addView(header)
        } else {
            body.addView(text(title, 27f, white, true).apply {
                setPadding(dp(4), dp(14), dp(4), dp(5))
            })
            if (subtitle.isNotEmpty()) {
                body.addView(text(subtitle, 14f, muted).apply {
                    setPadding(dp(4), 0, dp(4), dp(18))
                })
            }
        }

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setBackgroundColor(bg)
        }
        listOf("⌂\nHome", "▧\nLibrary", "⚙\nSettings").forEachIndexed { index, label ->
            val active = when (index) {
                0 -> title == "DRVCAM"
                1 -> title == "Library"
                else -> title == "Settings"
            }
            val item = text(label, 14f, if (active) green else muted, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(8), dp(8))
                if (active) background = shape(Color.rgb(13, 47, 38), 32f)
                setOnClickListener {
                    when (index) {
                        0 -> showHome()
                        1 -> showLibrary()
                        2 -> showSettings()
                    }
                }
            }
            nav.addView(item, lp(0, dp(54), 1f))
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun panel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = shape(card, 25f, Color.rgb(37, 55, 57))
        }
    }

    private fun row(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun pill(label: String, color: Int = green): TextView =
        text(label, 12f, color, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = shape(card2, 25f)
        }

    private fun actionButton(
        label: String,
        action: () -> Unit,
        active: Boolean = false
    ): TextView = text(label, 14f, if (active) green else muted, true).apply {
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(14), dp(8), dp(14))
        background = shape(if (active) Color.rgb(14, 48, 38) else card, 17f,
            if (active) green else Color.rgb(40, 58, 60))
        setOnClickListener { action() }
    }

    private fun showHome() {
        startScreen("DRVCAM")
        val targets = prefs.getStringSet("selected_packages", emptySet()) ?: emptySet()

        val live = panel()
        val liveHead = row()
        liveHead.addView(text("Live Source", 18f, white, true), lp(0, -2, 1f))
        liveHead.addView(pill(mediaType))
        live.addView(liveHead)
        gapIn(live, 12)

        val preview = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = shape(Color.rgb(19, 37, 39), 21f)
            minimumHeight = dp(225)
        }
        preview.addView(text("● VIRTUAL", 13f, green, true).apply {
            gravity = Gravity.LEFT
        }, lp(-1))
        val previewIcon = text(
            if (selectedMedia == null) "▧" else "▶",
            54f, muted, true
        ).apply { gravity = Gravity.CENTER }
        preview.addView(previewIcon, lp(-1, 0, 1f))
        preview.addView(text(
            selectedMedia?.lastPathSegment ?: "No media selected",
            12f, muted
        ).apply { gravity = Gravity.CENTER }, lp(-1))
        live.addView(preview, lp(h = dp(238)))
        gapIn(live, 10)

        val floating = row().apply {
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = shape(card2, 17f)
        }
        floating.addView(text("▣  Floating Controller", 14f, white, true), lp(0, -2, 1f))
        floating.addView(pill("🔒 PRO", Color.rgb(255, 207, 91)))
        floating.addView(text("   ○", 25f, muted), lp())
        live.addView(floating)
        body.addView(live, lp())
        gap(16)

        val targetCard = panel()
        val targetHead = row()
        targetHead.addView(text("Target App", 18f, white, true), lp(0, -2, 1f))
        targetHead.addView(text("${targets.size} selected  ›", 14f, muted))
        targetCard.addView(targetHead)
        targetCard.addView(text(
            if (targets.isEmpty()) "Choose camera-using apps"
            else targets.joinToString(", "),
            14f, muted
        ).apply { setPadding(0, dp(15), 0, dp(2)) })
        targetCard.setOnClickListener { showTargets() }
        body.addView(targetCard)
        gap(16)

        val media = row().apply {
            setPadding(dp(14), dp(15), dp(14), dp(15))
            background = shape(card, 24f, Color.rgb(37, 55, 57))
        }
        media.addView(text("▤", 30f, muted, true).apply {
            gravity = Gravity.CENTER
            background = shape(card2, 15f)
        }, lp(dp(66), dp(66)))
        val mediaInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        mediaInfo.addView(text("Media Source", 17f, white, true))
        mediaInfo.addView(text("Choose a photo or MP4", 13f, muted))
        media.addView(mediaInfo, lp(0, -2, 1f))
        media.addView(text("Import", 16f, green, true).apply {
            setOnClickListener { importMedia() }
        })
        media.setOnClickListener { importMedia() }
        body.addView(media)
        gap(16)

        val quickHead = row()
        quickHead.addView(text("Quick Controls", 17f, white, true), lp(0, -2, 1f))
        quickHead.addView(text("0° · Fit · Loop", 12f, muted))
        body.addView(quickHead)
        gap(10)

        val controls = row()
        listOf("⟳\nRotate", "◫\nMirror", "▣\nCamera Source", "⇄\nLoop").forEachIndexed { i, label ->
            val item = actionButton(label, {
                Toast.makeText(this, when (i) {
                    0 -> "Rotate control selected"
                    1 -> "Mirror control selected"
                    2 -> "Camera Source is not connected yet"
                    else -> "Loop enabled for selected media"
                }, Toast.LENGTH_SHORT).show()
            }, i == 3)
            val wrap = LinearLayout(this).apply {
                setPadding(dp(3), 0, dp(3), 0)
                addView(item, lp(-1, dp(84)))
            }
            controls.addView(wrap, lp(0, -2, 1f))
        }
        body.addView(controls)
        gap(16)

        val mainButtons = row()
        mainButtons.addView(actionButton("▶  Enable", {
            Toast.makeText(this, "Hook engine is not verified active yet", Toast.LENGTH_LONG).show()
        }), lp(0, dp(68), 1f))
        mainButtons.addView(View(this), lp(dp(8), 1))
        mainButtons.addView(actionButton("■  Disable", {
            Toast.makeText(this, "No active session to disable", Toast.LENGTH_SHORT).show()
        }), lp(0, dp(68), 1f))
        body.addView(mainButtons)
        gap(10)
        body.addView(actionButton("▣   Prepare Target", {
            showTargets()
        }), lp(h = dp(64)))
        gap(16)

        val status = panel()
        val statusRow = row()
        listOf(
            "● Root\nCheck",
            "● Module\nCheck",
            "● Overlay\nOff",
            "● Mic\nReady"
        ).forEachIndexed { index, label ->
            val c = when (index) {
                2 -> muted
                1 -> Color.rgb(238, 190, 88)
                else -> green
            }
            statusRow.addView(text(label, 12f, c, false).apply {
                gravity = Gravity.CENTER
            }, lp(0, dp(48), 1f))
        }
        status.addView(statusRow)
        body.addView(status)
        gap(12)
    }

    private fun gapIn(view: LinearLayout, height: Int) {
        view.addView(View(this), lp(h = height))
    }

    private fun importMedia() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*", "image/*"))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, pickVideo)
    }

    private fun showTargets() {
        startScreen("Target Apps", "Choose camera-using apps. Package names are shown below each app.")
        val selected = (prefs.getStringSet("selected_packages", emptySet())
            ?: emptySet()).toMutableSet()
        val apps = packageManager.getInstalledApplications(0)
            .filter { it.packageName != packageName }
            .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }

        body.addView(text("${apps.size} installed apps", 15f, green, true))
        gap(10)
        apps.forEach { app: ApplicationInfo ->
            val item = panel()
            val cb = CheckBox(this).apply {
                text = packageManager.getApplicationLabel(app).toString()
                textSize = 16f
                setTextColor(white)
                buttonTintList = android.content.res.ColorStateList.valueOf(green)
                isChecked = selected.contains(app.packageName)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selected.add(app.packageName)
                    else selected.remove(app.packageName)
                }
            }
            item.addView(cb)
            item.addView(text(app.packageName, 12f, muted).apply {
                setPadding(dp(34), 0, 0, 0)
            })
            body.addView(item)
            gap(8)
        }
        body.addView(actionButton("Save Selected Apps (${selected.size})", {
            prefs.edit().putStringSet("selected_packages", selected.toSet()).apply()
            Toast.makeText(this, "Saved ${selected.size} target apps", Toast.LENGTH_SHORT).show()
            showHome()
        }, true))
    }

    private fun showLibrary() {
        startScreen("Library", "Import and choose your virtual camera media.")
        val selected = panel()
        selected.addView(text("Selected media", 18f, white, true))
        selected.addView(text(
            selectedMedia?.lastPathSegment ?: "No media selected",
            14f, muted
        ).apply { setPadding(0, dp(12), 0, dp(12)) })
        body.addView(selected)
        gap(12)
        body.addView(actionButton("＋  Import Photo / MP4", { importMedia() }, true))
        gap(10)
        body.addView(text("Media preview and actual camera frame injection will be connected in the next stage.", 13f, muted))
    }

    private fun showSettings() {
        startScreen("Settings", "Virtual camera configuration")
        val entries = listOf(
            "App version" to "0.4.0",
            "Framework" to "Vector / libxposed API 102",
            "Hook status" to "Not verified active",
            "Target scope" to "Enable VirtualCamera in Vector for each target app",
            "Overlay" to "Floating Controller is a future feature",
            "Privacy" to "Media access is chosen through Android's document picker"
        )
        entries.forEach { (title, value) ->
            val item = panel()
            item.addView(text(title, 15f, green, true))
            item.addView(text(value, 14f, muted).apply {
                setPadding(0, dp(8), 0, 0)
            })
            body.addView(item)
            gap(10)
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return
        selectedMedia = data.data!!
        val type = contentResolver.getType(selectedMedia!!)
        mediaType = if (type?.startsWith("image/") == true) "Image selected" else "Video selected"
        try {
            contentResolver.takePersistableUriPermission(
                selectedMedia!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }
        prefs.edit()
            .putString("media_uri", selectedMedia.toString())
            .putString("media_type", mediaType)
            .apply()
        Toast.makeText(this, "$mediaType", Toast.LENGTH_SHORT).show()
        when (requestCode) {
            pickVideo, pickImage -> showHome()
        }
    }
}
