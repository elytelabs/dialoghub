package com.elytelabs.dialoghub

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.elytelabs.dialoghub.coroutines.*
import com.elytelabs.dialoghub.demo.R
import com.elytelabs.dialoghub.dialogs.*
import com.elytelabs.dialoghub.dsl.*
import com.elytelabs.dialoghub.models.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var textView: TextView
    private lateinit var rgThemeMode: RadioGroup
    private lateinit var rgInvocationMode: RadioGroup
    private lateinit var tvThemeStatus: TextView

    private var currentBackgroundRes: Int? = null
    private var currentFontRes: Int? = null
    private var currentColor: Int? = null
    private var currentTextSize: Float = 20f
    private var currentAlignment: TextFormatDialog.TextAlignment = TextFormatDialog.TextAlignment.CENTER
    private var currentEffectsConfig = TextEffectConfig()

    // System gallery picker launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        rootLayout.background = bitmap.toDrawable(resources)
                        currentBackgroundRes = null
                        Toast.makeText(this, "Gallery photo applied as background!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Resolves the active context to demonstrate DialogHub's dual support:
     * - Material Theme Context: Inherits host app brand colors and Material3 styling directly.
     * - Legacy AppCompat Context: Simulates an AppCompat host activity; DialogThemeHelper auto-wraps it safely.
     */
    private fun getActiveContext(): Context {
        return if (rgThemeMode.checkedRadioButtonId == R.id.rbAppCompat) {
            ContextThemeWrapper(this, R.style.Theme_DialogHub_AppCompatTest)
        } else {
            this
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)
        textView = findViewById(R.id.textView)
        rgThemeMode = findViewById(R.id.rgThemeMode)
        rgInvocationMode = findViewById(R.id.rgInvocationMode)
        tvThemeStatus = findViewById(R.id.tvThemeStatus)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rgThemeMode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbAppCompat) {
                tvThemeStatus.text = "Using: Legacy AppCompat Context (Auto-wrapped by DialogThemeHelper without crashing)"
                tvThemeStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
            } else {
                tvThemeStatus.text = "Using: Material3 Theme (Direct inheritance of host brand colors)"
                tvThemeStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            }
        }

        val backgrounds = listOf(
            R.drawable.bg11,
            R.drawable.bg22,
            R.drawable.bg23,
            R.drawable.bg25,
            R.drawable.bg5,
            R.drawable.bg22,
            R.drawable.bg23,
            R.drawable.bg25,
            R.drawable.bg22,
            R.drawable.bg23,
            R.drawable.bg25
        )

        val fonts = listOf(
            R.font.righteous,
            R.font.salsa,
            R.font.schoolbell,
            R.font.sofadi_one,
            R.font.salsa,
            R.font.schoolbell,
            R.font.sofadi_one,
            R.font.salsa,
            R.font.schoolbell,
            R.font.sofadi_one
        )

        val btnImageSelector: Button = findViewById(R.id.btnImageSelector)
        val btnFontSelector: Button = findViewById(R.id.btnFontSelector)
        val btnColorSelector: Button = findViewById(R.id.btnColorSelector)
        val btnFormatSelector: Button = findViewById(R.id.btnFormatSelector)
        val btnEffectsSelector: Button = findViewById(R.id.btnEffectsSelector)
        val btnTextStudio: Button = findViewById(R.id.btnTextStudio)

        //  ALL-IN-ONE TEXT STUDIO (with real-time live preview)
        btnTextStudio.setOnClickListener {
            val ctx = getActiveContext()

            val currentTypography = TextTypographyConfig(
                fontResId = currentFontRes,
                textColor = currentColor ?: Color.WHITE,
                textSizeSp = currentTextSize,
                alignment = currentAlignment,
                effectConfig = currentEffectsConfig
            )

            when (rgInvocationMode.checkedRadioButtonId) {
                R.id.rbModeCoroutine -> {
                    lifecycleScope.launch {
                        val result = ctx.awaitTextStudio(
                            initialConfig = currentTypography,
                            previewText = textView.text.toString(),
                            fonts = fonts
                        )
                        result.applyTo(textView)
                    }
                }
                R.id.rbModeDsl -> {
                    ctx.showTextStudioDialog {
                        setConfig(currentTypography)
                        setPreviewText(textView.text.toString())
                        setFonts(fonts)
                        setOnLivePreviewListener { liveConfig ->
                            liveConfig.applyTo(textView)
                        }
                        setOnTypographyApplied { applied ->
                            applied.applyTo(textView)
                        }
                    }
                }
                else -> {
                    TextStudioDialog(ctx).show(
                        initialConfig = currentTypography,
                        previewText = textView.text.toString(),
                        fonts = fonts,
                        onLivePreview = { liveConfig ->
                            liveConfig.applyTo(textView)
                        }
                    ) { applied ->
                        applied.applyTo(textView)
                    }
                }
            }
        }

        // 1. Background Image / Gallery / Color Selector
        btnImageSelector.setOnClickListener {
            val ctx = getActiveContext()

            when (rgInvocationMode.checkedRadioButtonId) {
                R.id.rbModeCoroutine -> {
                    lifecycleScope.launch {
                        when (val result = ctx.awaitBackground(backgrounds, currentBackgroundRes, true)) {
                            is SelectedBackground.Image -> {
                                currentBackgroundRes = result.drawableResId
                                rootLayout.setBackgroundResource(result.drawableResId)
                            }
                            is SelectedBackground.Color -> {
                                currentColor = result.colorInt
                                rootLayout.setBackgroundColor(result.colorInt)
                            }
                            is SelectedBackground.GalleryRequested -> {
                                galleryLauncher.launch("image/*")
                            }
                            null -> {} // Dismissed
                        }
                    }
                }
                R.id.rbModeDsl -> {
                    ctx.showImageSelectorDialog {
                        setBackgrounds(backgrounds)
                        setSelectedBackground(currentBackgroundRes)
                        setEnableGalleryPick(true) { galleryLauncher.launch("image/*") }
                        setOnImageSelected { resId ->
                            currentBackgroundRes = resId
                            rootLayout.setBackgroundResource(resId)
                        }
                        setOnColorSelected { color ->
                            currentColor = color
                            rootLayout.setBackgroundColor(color)
                        }
                    }
                }
                else -> {
                    ImageSelectorDialog(ctx).show(
                        backgrounds = backgrounds,
                        selectedBackgroundResId = currentBackgroundRes,
                        onPickFromGallery = { galleryLauncher.launch("image/*") },
                        onImageSelected = { resId ->
                            currentBackgroundRes = resId
                            rootLayout.setBackgroundResource(resId)
                        },
                        onColorSelected = { color ->
                            currentColor = color
                            rootLayout.setBackgroundColor(color)
                        }
                    )
                }
            }
        }

        // 2. Font Style Selector with Custom Preview Text
        btnFontSelector.setOnClickListener {
            val ctx = getActiveContext()

            when (rgInvocationMode.checkedRadioButtonId) {
                R.id.rbModeCoroutine -> {
                    lifecycleScope.launch {
                        val fontRes = ctx.awaitFont(fonts, "Sample / اردو", currentFontRes)
                        if (fontRes != null) {
                            currentFontRes = fontRes
                            textView.typeface = ResourcesCompat.getFont(this@MainActivity, fontRes)
                            currentEffectsConfig.applyTo(textView)
                        }
                    }
                }
                R.id.rbModeDsl -> {
                    ctx.showFontStyleDialog {
                        setFonts(fonts)
                        setPreviewText("Sample / اردو")
                        setSelectedFont(currentFontRes)
                        setOnFontSelected { fontResId ->
                            currentFontRes = fontResId
                            textView.typeface = ResourcesCompat.getFont(this@MainActivity, fontResId)
                            currentEffectsConfig.applyTo(textView)
                        }
                    }
                }
                else -> {
                    FontStyleDialog(ctx).show(
                        fonts = fonts,
                        previewText = "Sample / اردو",
                        selectedFontResId = currentFontRes
                    ) { fontResId ->
                        currentFontRes = fontResId
                        textView.typeface = ResourcesCompat.getFont(this, fontResId)
                        currentEffectsConfig.applyTo(textView)
                    }
                }
            }
        }

        // 3. Color Picker with Transparency & Live Preview
        btnColorSelector.setOnClickListener {
            val ctx = getActiveContext()

            when (rgInvocationMode.checkedRadioButtonId) {
                R.id.rbModeCoroutine -> {
                    lifecycleScope.launch {
                        val color = ctx.awaitColor(selectedColor = currentColor)
                        if (color != null) {
                            currentColor = color
                            rootLayout.setBackgroundColor(color)
                        }
                    }
                }
                R.id.rbModeDsl -> {
                    ctx.showColorPickerDialog {
                        setSelectedColor(currentColor)
                        setOnColorSelected { color ->
                            currentColor = color
                            rootLayout.setBackgroundColor(color)
                        }
                    }
                }
                else -> {
                    ColorPickerDialog(ctx).show(
                        selectedColor = currentColor
                    ) { color ->
                        currentColor = color
                        rootLayout.setBackgroundColor(color)
                    }
                }
            }
        }

        // 4. Text Format Dialog (Live Preview, Text Size & Alignment)
        btnFormatSelector.setOnClickListener {
            val ctx = getActiveContext()

            when (rgInvocationMode.checkedRadioButtonId) {
                R.id.rbModeCoroutine -> {
                    lifecycleScope.launch {
                        val result = ctx.awaitTextFormat(
                            initialSizeSp = currentTextSize,
                            initialAlignment = currentAlignment,
                            previewText = textView.text.toString()
                        )
                        currentTextSize = result.textSizeSp
                        currentAlignment = result.alignment
                        textView.textSize = result.textSizeSp
                        textView.gravity = result.alignment.gravity
                    }
                }
                R.id.rbModeDsl -> {
                    ctx.showTextFormatDialog {
                        setTextSize(currentTextSize)
                        setAlignment(currentAlignment)
                        setPreviewText(textView.text.toString())
                        setOnFormatChanged { size, alignment ->
                            currentTextSize = size
                            currentAlignment = alignment
                            textView.textSize = size
                            textView.gravity = alignment.gravity
                        }
                    }
                }
                else -> {
                    TextFormatDialog(ctx).show(
                        initialSizeSp = currentTextSize,
                        initialAlignment = currentAlignment,
                        previewText = textView.text.toString()
                    ) { size, alignment ->
                        currentTextSize = size
                        currentAlignment = alignment
                        textView.textSize = size
                        textView.gravity = alignment.gravity
                    }
                }
            }
        }

        // 5. Text Effects Dialog (Styles, Drop Shadow, Letter Spacing, Line Spacing)
        btnEffectsSelector.setOnClickListener {
            val ctx = getActiveContext()

            when (rgInvocationMode.checkedRadioButtonId) {
                R.id.rbModeCoroutine -> {
                    lifecycleScope.launch {
                        val config = ctx.awaitTextEffects(
                            initialConfig = currentEffectsConfig,
                            previewText = textView.text.toString()
                        )
                        currentEffectsConfig = config
                        currentEffectsConfig.applyTo(textView)
                    }
                }
                R.id.rbModeDsl -> {
                    ctx.showTextEffectsDialog {
                        setConfig(currentEffectsConfig)
                        setPreviewText(textView.text.toString())
                        setOnEffectsChanged { config ->
                            currentEffectsConfig = config
                            currentEffectsConfig.applyTo(textView)
                        }
                    }
                }
                else -> {
                    TextEffectsDialog(ctx).show(
                        initialConfig = currentEffectsConfig,
                        previewText = textView.text.toString()
                    ) { config ->
                        currentEffectsConfig = config
                        currentEffectsConfig.applyTo(textView)
                    }
                }
            }
        }

        // 6. Text Stroke & Outline Dialog
        val btnStrokeSelector = findViewById<Button>(R.id.btnStrokeSelector)
        btnStrokeSelector.setOnClickListener {
            val ctx = getActiveContext()

            when (rgInvocationMode.checkedRadioButtonId) {
                R.id.rbModeCoroutine -> {
                    lifecycleScope.launch {
                        val config = ctx.awaitTextStroke(previewText = textView.text.toString())
                        config.applyTo(textView)
                    }
                }
                R.id.rbModeDsl -> {
                    ctx.showTextStrokeDialog {
                        setPreviewText(textView.text.toString())
                        setOnStrokeChanged { config ->
                            config.applyTo(textView)
                        }
                    }
                }
                else -> {
                    TextStrokeDialog(ctx).show(
                        previewText = textView.text.toString()
                    ) { config ->
                        config.applyTo(textView)
                    }
                }
            }
        }

        // 7. Text Background Ribbon / Highlight Dialog
        val btnHighlightSelector = findViewById<Button>(R.id.btnHighlightSelector)
        btnHighlightSelector.setOnClickListener {
            val ctx = getActiveContext()

            when (rgInvocationMode.checkedRadioButtonId) {
                R.id.rbModeCoroutine -> {
                    lifecycleScope.launch {
                        val config = ctx.awaitTextHighlight(previewText = textView.text.toString())
                        config.applyTo(textView)
                    }
                }
                R.id.rbModeDsl -> {
                    ctx.showTextHighlightDialog {
                        setPreviewText(textView.text.toString())
                        setOnHighlightChanged { config ->
                            config.applyTo(textView)
                        }
                    }
                }
                else -> {
                    TextHighlightDialog(ctx).show(
                        previewText = textView.text.toString()
                    ) { config ->
                        config.applyTo(textView)
                    }
                }
            }
        }
    }
}