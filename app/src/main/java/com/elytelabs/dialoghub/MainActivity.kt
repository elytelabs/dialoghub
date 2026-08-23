package com.elytelabs.dialoghub

import android.content.Context
import android.graphics.BitmapFactory
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elytelabs.dialoghub.demo.R
import com.elytelabs.dialoghub.dialogs.ColorPickerDialog
import com.elytelabs.dialoghub.dialogs.FontStyleDialog
import com.elytelabs.dialoghub.dialogs.ImageSelectorDialog
import com.elytelabs.dialoghub.dialogs.TextEffectsDialog
import com.elytelabs.dialoghub.dialogs.TextFormatDialog
import com.elytelabs.dialoghub.models.TextEffectConfig

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var textView: TextView
    private lateinit var rgThemeMode: RadioGroup
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
            R.drawable.bg5
        )

        val fonts = listOf(
            R.font.righteous,
            R.font.salsa,
            R.font.schoolbell,
            R.font.sofadi_one
        )

        val btnImageSelector: Button = findViewById(R.id.btnImageSelector)
        val btnFontSelector: Button = findViewById(R.id.btnFontSelector)
        val btnColorSelector: Button = findViewById(R.id.btnColorSelector)
        val btnFormatSelector: Button = findViewById(R.id.btnFormatSelector)
        val btnEffectsSelector: Button = findViewById(R.id.btnEffectsSelector)

        // 1. Background Image / Gallery / Color Selector
        btnImageSelector.setOnClickListener {
            ImageSelectorDialog(getActiveContext()).show(
                backgrounds = backgrounds,
                selectedBackgroundResId = currentBackgroundRes,
                onPickFromGallery = {
                    galleryLauncher.launch("image/*")
                },
                onImageSelected = { imageResource ->
                    currentBackgroundRes = imageResource
                    rootLayout.setBackgroundResource(imageResource)
                },
                onColorSelected = { color ->
                    currentColor = color
                    rootLayout.setBackgroundColor(color)
                }
            )
        }

        // 2. Font Style Selector with Custom Preview Text
        btnFontSelector.setOnClickListener {
            FontStyleDialog(getActiveContext()).show(
                fonts = fonts,
                previewText = "Sample / اردو",
                selectedFontResId = currentFontRes
            ) { fontResId ->
                currentFontRes = fontResId
                textView.typeface = ResourcesCompat.getFont(this, fontResId)
                currentEffectsConfig.applyTo(textView)
            }
        }

        // 3. Color Picker with Transparency & Live Preview
        btnColorSelector.setOnClickListener {
            ColorPickerDialog(getActiveContext()).show(selectedColor = currentColor) { color ->
                currentColor = color
                rootLayout.setBackgroundColor(color)
            }
        }

        // 4. Text Format Dialog (Live Preview, Text Size & Alignment)
        btnFormatSelector.setOnClickListener {
            TextFormatDialog(getActiveContext()).show(
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

        // 5. Text Effects Dialog (Styles, Drop Shadow, Letter Spacing, Line Spacing)
        btnEffectsSelector.setOnClickListener {
            TextEffectsDialog(getActiveContext()).show(
                initialConfig = currentEffectsConfig,
                previewText = textView.text.toString()
            ) { config ->
                currentEffectsConfig = config
                currentEffectsConfig.applyTo(textView)
            }
        }
    }
}