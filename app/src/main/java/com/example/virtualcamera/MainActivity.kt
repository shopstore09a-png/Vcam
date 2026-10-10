package com.example.virtualcamera

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 16, 18)
    private val card = Color.rgb(17, 30, 32)
    private val card2 = Color.rgb(26, 41, 43)
    private val border = Color.rgb(41, 59, 61)
    private val green = Color.rgb(70, 219, 155)
    private val white = Color.rgb(240, 247, 246)
    private val muted = Color.rgb(153, 173, 172)

    private val prefs by lazy { getSharedPreferences("targets", MODE_PRIVATE) }
    private var currentPage = "Home"
    private var selectedMedia: Uri? = null
    private var mediaType = "No source"
    private var libraryTab = "MEDIA"
    private var searchQuery = ""
    private val selected = mutableSetOf<String>()
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout

    companion object {
        private const val PICK_MEDIA = 101
        private const val PICK_IMAGE = 102
        private const val PICK_VIDEO = 103
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selected.addAll(prefs.getStringSet("selected_packages", emptySet()) ?: emptySet())
        selectedMedia = prefs.getString("media_uri", null)?.let { Uri.parse(it) }
        mediaType = prefs.getString("media_type", "No source") ?: "No source"
        showHome()
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun shape(color: Int, radius: Int = 22, stroke: Int = 0) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != 0) setStroke(dp(1), stroke)
        }

    private fun params(w: Int = -1, h: Int = -2, weight: Float = 0f) =
        LinearLayout.LayoutParams(w, h, weight)

    private fun label(value: String, size: Float = 15f, color: Int = white,
                      bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun gap(parent: LinearLayout, height: Int = 12) {
        parent.addView(View(this), params(h = dp(height)))
    }

    private fun panel(padding: Int = 14): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
            background = shape(card, 24, border)
        }

    private fun horizontal(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

    private fun pill(value: String, color: Int = green): TextView =
        label(value, 12f, color, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = shape(card2, 30)
        }

    private fun button(value: String, action: () -> Unit,
                       primary: Boolean = false): TextView =
        label(value, 14f, if (primary) Color.rgb(5, 35, 26) else green, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(15), dp(12), dp(15))
            background = shape(
                if (primary) green else card2, 17,
                if (primary) green else border
            )
            setOnClickListener { action() }
        }

    private fun saveTargets() {
        prefs.edit().putStringSet("selected_packages", selected.toSet()).apply()
    }

    private fun screen(title: String, subtitle: String = "") {
        currentPage = title
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this).apply {
            clipToPadding = false
            isFillViewport = false
        }
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(10), dp(18), dp(18))
        }
        scroll.addView(body)
        root.addView(scroll, params(h = 0, weight = 1f))

        val header = horizontal().apply {
            setPadding(dp(4), dp(8), dp(4), dp(18))
        }
        header.addView(label("♧", 28f, green, true).apply {
            gravity = Gravity.CENTER
            background = shape(card2, 14, border)
        }, params(dp(48), dp(48)))
        header.addView(label("  DRVCAM", 28f, white, true), params(0, -2, 1f))
        header.addView(label("DR", 14f, green, true).apply {
            gravity = Gravity.CENTER
            background = shape(card2, 40)
        }, params(dp(48), dp(48)))
        body.addView(header)

        if (title != "Home") {
            body.addView(label(title, 28f, white, true))
            if (subtitle.isNotEmpty()) {
                body.addView(label(subtitle, 14f, muted).apply {
                    setPadding(0, dp(4), 0, dp(16))
                })
            } else gap(body, 12)
        }

        val nav = horizontal().apply {
            setPadding(dp(10), dp(7), dp(10), dp(7))
            setBackgroundColor(bg)
        }
        listOf("⌂\nHome", "▧\nLibrary", "⚙\nSettings").forEachIndexed { i, value ->
            val active = when (i) {
                0 -> title == "Home" || title == "Target Apps"
                1 -> title == "Library"
                else -> title == "Settings"
            }
            val item = label(value, 14f, if (active) white else muted, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(8), dp(12), dp(8))
                if (active) background = shape(Color.rgb(13, 49, 38), 30)
                setOnClickListener {
                    when (i) {
                        0 -> showHome()
                        1 -> showLibrary()
                        2 -> showSettings()
                    }
                }
            }
            nav.addView(item, params(0, dp(55), 1f))
        }
        root.addView(nav)
        setContentView(root)
    }

    override fun onBackPressed() {
        when (currentPage) {
            "Target Apps", "Library", "Settings" -> showHome()
            else -> super.onBackPressed()
        }
    }

    private fun showHome() {
        screen("Home")

        val live = panel()
        val liveHead = horizontal()
        liveHead.addView(label("Live Source", 18f, white, true), params(0, -2, 1f))
        liveHead.addView(pill(mediaType))
        live.addView(liveHead)
        gap(live, 12)

        val preview = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = shape(Color.rgb(19, 37, 39), 21)
            minimumHeight = dp(222)
        }
        preview.addView(label("● VIRTUAL", 13f, green, true))
        val previewArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        previewArea.addView(label(
            if (selectedMedia == null) "▧" else "▶",
            54f, muted, true
        ).apply { gravity = Gravity.CENTER })
        previewArea.addView(label(
            selectedMedia?.lastPathSegment ?: "No media selected", 12f, muted
        ).apply { gravity = Gravity.CENTER })
        preview.addView(previewArea, params(h = dp(160)))
        live.addView(preview)
        gap(live, 10)

        val floating = horizontal().apply {
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = shape(card2, 17)
        }
        floating.addView(label("▣  Floating Controller", 14f, white, true),
            params(0, -2, 1f))
        floating.addView(pill("🔒 PRO", Color.rgb(255, 206, 95)))
        floating.addView(label("   ○", 22f, muted))
        live.addView(floating)
        body.addView(live)
        gap(body, 16)

        // Target picker: chosen applications appear as compact chips here.
        val target = panel()
        val targetHead = horizontal()
        targetHead.addView(label("Target App", 18f, white, true), params(0, -2, 1f))
        targetHead.addView(label("${selected.size} selected  ›", 13f, muted))
        target.addView(targetHead)
        target.setOnClickListener { showTargets() }

        if (selected.isEmpty()) {
            target.addView(label("Choose camera-using apps", 14f, muted).apply {
                setPadding(0, dp(12), 0, dp(2))
            })
        } else {
            gap(target, 10)
            val chips = horizontal().apply { gravity = Gravity.TOP }
            var count = 0
            selected.toList().forEach { pkg ->
                val app = try {
                    packageManager.getApplicationInfo(pkg, 0)
                } catch (_: Exception) { null }
                val appName = if (app != null)
                    packageManager.getApplicationLabel(app).toString() else pkg
                val chip = label("● $appName  ×", 12f, green, true).apply {
                    setPadding(dp(10), dp(9), dp(10), dp(9))
                    background = shape(Color.rgb(13, 49, 38), 18)
                    setOnClickListener {
                        selected.remove(pkg)
                        saveTargets()
                        showHome()
                    }
                }
                chips.addView(chip, params(h = -2))
                chips.addView(View(this), params(dp(6), 1))
                count++
                if (count >= 3) return@forEach
            }
            target.addView(chips)
            if (selected.size > 3) {
                target.addView(label("+ ${selected.size - 3} more apps", 12f, muted)
                    .apply { setPadding(0, dp(6), 0, 0) })
            }
        }
        body.addView(target)
        gap(body, 16)

        val media = horizontal().apply {
            setPadding(dp(14), dp(15), dp(14), dp(15))
            background = shape(card, 23, border)
            setOnClickListener { showLibrary() }
        }
        media.addView(label("▤", 30f, muted, true).apply {
            gravity = Gravity.CENTER
            background = shape(card2, 14)
        }, params(dp(66), dp(66)))
        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(8), 0)
        }
        info.addView(label("Media Source", 17f, white, true))
        info.addView(label("Choose a photo or MP4", 13f, muted))
        media.addView(info, params(0, -2, 1f))
        media.addView(label("Import", 15f, green, true))
        body.addView(media)
        gap(body, 16)

        val qh = horizontal()
        qh.addView(label("Quick Controls", 17f, white, true), params(0, -2, 1f))
        qh.addView(label("0° · Fit · Loop", 12f, muted))
        body.addView(qh)
        gap(body, 10)

        val controls = horizontal()
        listOf("⟳\nRotate", "◫\nMirror", "▣\nCamera Source", "⇄\nLoop")
            .forEachIndexed { i, value ->
                val cell = button(value, {
                    when (i) {
                        0 -> prefs.edit().putInt("rotation",
                            (prefs.getInt("rotation", 0) + 90) % 360).apply()
                        1 -> prefs.edit().putBoolean("mirror",
                            !prefs.getBoolean("mirror", false)).apply()
                        2 -> Toast.makeText(this,
                            "Camera source configuration is not connected yet",
                            Toast.LENGTH_SHORT).show()
                        3 -> prefs.edit().putBoolean("loop",
                            !prefs.getBoolean("loop", true)).apply()
                    }
                    if (i == 0 || i == 1 || i == 3) {
                        Toast.makeText(this, "$value preference saved",
                            Toast.LENGTH_SHORT).show()
                    }
                }, i == 3)
                controls.addView(cell, params(0, dp(82), 1f))
                if (i < 3) controls.addView(View(this), params(dp(5), 1))
            }
        body.addView(controls)
        gap(body, 16)

        val actions = horizontal()
        actions.addView(button("▶  Enable", {
            Toast.makeText(this,
                "Hook engine must be verified before enabling camera replacement",
                Toast.LENGTH_LONG).show()
        }), params(0, dp(66), 1f))
        actions.addView(View(this), params(dp(8), 1))
        actions.addView(button("■  Disable", {
            Toast.makeText(this, "No active replacement session confirmed",
                Toast.LENGTH_SHORT).show()
        }), params(0, dp(66), 1f))
        body.addView(actions)
        gap(body, 10)
        body.addView(button("▣   Prepare Target", { showTargets() }))
        gap(body, 14)

        val status = panel(12)
        val sr = horizontal()
        listOf("● Root\nCheck", "● Module\nCheck", "● Overlay\nOff", "● Mic\nReady")
            .forEachIndexed { i, value ->
                sr.addView(label(value, 11f,
                    if (i == 2) muted else green, i == 0 || i == 1).apply {
                    gravity = Gravity.CENTER
                }, params(0, dp(44), 1f))
            }
        status.addView(sr)
        body.addView(status)
    }

    private fun showTargets() {
        screen("Target Apps", "Select one or more apps, then tap Apply.")
        val apps = packageManager.getInstalledApplications(0)
            .filter { it.packageName != packageName }
            .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }

        val search = EditText(this).apply {
            hint = "⌕   Search apps"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 16f
            isSingleLine = true
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = shape(card2, 16, border)
        }
        body.addView(search)
        gap(body, 12)

        val counter = label("", 14f, muted, true)
        body.addView(counter)
        gap(body, 8)

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(list)

        fun render(query: String) {
            list.removeAllViews()
            val filtered = apps.filter {
                packageManager.getApplicationLabel(it).toString()
                    .contains(query, true) || it.packageName.contains(query, true)
            }
            counter.text = "${selected.size} selected  ·  ${filtered.size} apps found"

            filtered.forEach { app ->
                val pkg = app.packageName
                val name = packageManager.getApplicationLabel(app).toString()
                val item = horizontal().apply {
                    setPadding(dp(10), dp(10), dp(8), dp(10))
                    background = shape(card, 16, border)
                }
                val icon = try { packageManager.getApplicationIcon(app) } catch (_: Exception) { null }
                val appIcon = ImageView(this@MainActivity).apply {
                    if (icon != null) setImageDrawable(icon)
                }
                item.addView(appIcon, params(dp(42), dp(42)))
                val texts = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(10), 0, dp(4), 0)
                }
                texts.addView(label(name, 15f, white, true))
                texts.addView(label(pkg, 11f, muted))
                item.addView(texts, params(0, -2, 1f))
                val cb = CheckBox(this).apply {
                    buttonTintList = ColorStateList.valueOf(green)
                    isChecked = selected.contains(pkg)
                    setOnCheckedChangeListener { _, checked ->
                        if (checked) selected.add(pkg) else selected.remove(pkg)
                        saveTargets()
                        counter.text = "${selected.size} selected  ·  ${filtered.size} apps found"
                    }
                }
                item.addView(cb, params(dp(44), dp(48)))
                list.addView(item)
                gap(list, 6)
            }
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                render(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        render("")

        gap(body, 12)
        body.addView(button("✓   Apply · ${selected.size} selected", {
            saveTargets()
            showHome()
        }, true))
        body.addView(label("Selections save immediately and remain selected when you return.",
            12f, muted).apply { setPadding(dp(4), dp(10), dp(4), dp(8)) })
    }

    private fun importDocument(type: String, request: Int) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            this.type = type
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(i, request)
    }

    private fun showLibrary() {
        screen("Library", "Your imported media")
        val tabs = horizontal().apply {
            background = shape(card, 18)
            setPadding(dp(6), dp(6), dp(6), dp(6))
        }
        listOf("MEDIA", "PRESETS").forEach { tab ->
            tabs.addView(label(tab, 15f,
                if (libraryTab == tab) green else muted, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(10), dp(14), dp(10), dp(14))
                setOnClickListener {
                    libraryTab = tab
                    showLibrary()
                }
            }, params(0, -2, 1f))
        }
        body.addView(tabs)
        gap(body, 14)

        if (libraryTab == "MEDIA") {
            val importRow = horizontal()
            importRow.addView(button("▣  Import MP4", {
                importDocument("video/*", PICK_VIDEO)
            }, true), params(0, dp(58), 1f))
            importRow.addView(View(this), params(dp(8), 1))
            importRow.addView(button("▧  Import Image", {
                importDocument("image/*", PICK_IMAGE)
            }, true), params(0, dp(58), 1f))
            body.addView(importRow)
            gap(body, 14)

            val media = panel()
            media.gravity = Gravity.CENTER
            if (selectedMedia == null) {
                media.addView(label("No media imported", 17f, white))
            } else {
                media.addView(label("✓  $mediaType", 16f, green, true))
                media.addView(label(selectedMedia?.lastPathSegment ?: "Imported file",
                    13f, muted).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, dp(8), 0, dp(8))
                })
                media.addView(button("Remove selection", {
                    selectedMedia = null
                    mediaType = "No source"
                    prefs.edit().remove("media_uri").putString("media_type", mediaType).apply()
                    showLibrary()
                }))
            }
            body.addView(media)
        } else {
            body.addView(panel().apply {
                addView(label("No presets yet", 17f, white, true))
                addView(label("Presets will store reusable rotation, mirror and loop settings.",
                    13f, muted).apply { setPadding(0, dp(8), 0, 0) })
            })
        }
    }

    private fun showSettings() {
        screen("Settings", "System & controller")

        fun section(title: String): LinearLayout {
            val box = panel(14)
            box.addView(label(title, 14f, green, true))
            gap(box, 8)
            body.addView(box)
            gap(body, 12)
            return box
        }

        fun settingRow(box: LinearLayout, title: String, desc: String,
                       key: String, default: Boolean = false) {
            val r = horizontal()
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            info.addView(label(title, 16f, white))
            info.addView(label(desc, 13f, muted).apply {
                setPadding(0, dp(4), 0, 0)
            })
            r.addView(info, params(0, -2, 1f))
            val toggle = Switch(this).apply {
                buttonTintList = ColorStateList.valueOf(green)
                isChecked = prefs.getBoolean(key, default)
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean(key, checked).apply()
                }
            }
            r.addView(toggle)
            box.addView(r)
            gap(box, 14)
        }

        val general = section("GENERAL")
        settingRow(general, "Keep Alive", "Keep the app ready in background",
            "keep_alive")
        settingRow(general, "Start on Boot", "Restore Keep Alive after restart",
            "start_boot")
        settingRow(general, "Floating Controller", "Controller overlay preference",
            "overlay_enabled")

        val diagnostics = section("DIAGNOSTICS")
        listOf(
            "Check Environment" to "Root, module, overlay and scopes",
            "Module Status" to "Vector / LSPosed API 102",
            "Virtual Camera" to "Replacement engine status"
        ).forEach { (title, desc) ->
            diagnostics.addView(label(title, 16f, white, true))
            diagnostics.addView(label(desc, 13f, muted).apply {
                setPadding(0, dp(4), 0, dp(5))
            })
            diagnostics.addView(label(
                if (title == "Module Status") "Not verified by this screen"
                else "Needs device/framework verification",
                12f, muted
            ))
            gap(diagnostics, 10)
        }

        val prefsSection = section("PREFERENCES")
        prefsSection.addView(label("Language", 16f, white))
        prefsSection.addView(label("System default", 14f, muted).apply {
            setPadding(0, dp(6), 0, dp(12))
        })
        prefsSection.addView(button("Disable all selected targets", {
            selected.clear()
            saveTargets()
            Toast.makeText(this, "Target selection cleared", Toast.LENGTH_SHORT).show()
            showSettings()
        }))
        gap(prefsSection, 10)
        prefsSection.addView(label("About DRVCAM", 16f, white, true))
        prefsSection.addView(label("UI controls and media selection are available. Actual camera frame replacement is not yet verified.",
            13f, muted).apply { setPadding(0, dp(6), 0, 0) })
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }

        selectedMedia = uri
        mediaType = if (requestCode == PICK_IMAGE) "Image selected" else "Video selected"
        prefs.edit()
            .putString("media_uri", uri.toString())
            .putString("media_type", mediaType)
            .apply()
        Toast.makeText(this, "$mediaType", Toast.LENGTH_SHORT).show()
        when (requestCode) {
            PICK_VIDEO, PICK_IMAGE, PICK_MEDIA -> showLibrary()
        }
    }
}
