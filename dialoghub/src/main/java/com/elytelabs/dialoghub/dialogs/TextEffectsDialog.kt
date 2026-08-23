package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.models.TextEffectConfig
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * Dialog for configuring advanced text effects (styles, drop shadow, letter spacing, line spacing)
 * with live real-time preview.
 */
class TextEffectsDialog(private val context: Context) {

    private var currentConfig = TextEffectConfig()
    private var previewSampleText: String? = null
    private var effectsListener: TextEffectsListener? = null

    /**
     * Traditional interface listener for Java callers.
     */
    fun interface TextEffectsListener {
        fun onEffectsChanged(config: TextEffectConfig)
    }

    fun setTextEffectsListener(listener: TextEffectsListener) {
        this.effectsListener = listener
    }

    /**
     * Sets custom sample text to display in the live preview box.
     */
    fun setPreviewText(text: String?) {
        this.previewSampleText = text
    }

    /**
     * Shows the text effects dialog using Kotlin lambda callbacks.
     *
     * @param initialConfig Current text effects configuration.
     * @param previewText Optional custom sample text for the live preview box.
     * @param onEffectsApplied Callback invoked when effects are updated/applied.
     */
    fun show(
        initialConfig: TextEffectConfig = TextEffectConfig(),
        previewText: String? = null,
        onEffectsApplied: (config: TextEffectConfig) -> Unit
    ) {
        this.currentConfig = initialConfig
        if (previewText != null) {
            this.previewSampleText = previewText
        }
        this.effectsListener = TextEffectsListener { config ->
            onEffectsApplied(config)
        }
        showTextEffectsDialog()
    }

    /**
     * Displays the text effects dialog.
     */
    fun showTextEffectsDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_text_effects, null)
        val dialog = Dialog(themedContext)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setWindowAnimations(R.style.DialogHubAnimation)
        dialog.setContentView(dialogView)

        val btnBack = dialogView.findViewById<ImageButton>(R.id.btnBack)
        val tvLivePreview = dialogView.findViewById<TextView>(R.id.tvLivePreview)
        val toggleGroup = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupStyles)
        val seekBarShadow = dialogView.findViewById<SeekBar>(R.id.seekBarShadow)
        val tvShadowValue = dialogView.findViewById<TextView>(R.id.tvShadowValue)
        val seekBarLetterSpacing = dialogView.findViewById<SeekBar>(R.id.seekBarLetterSpacing)
        val tvLetterSpacingValue = dialogView.findViewById<TextView>(R.id.tvLetterSpacingValue)
        val seekBarLineSpacing = dialogView.findViewById<SeekBar>(R.id.seekBarLineSpacing)
        val tvLineSpacingValue = dialogView.findViewById<TextView>(R.id.tvLineSpacingValue)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyEffects)

        // Shadow color swatches
        val swatchBlack = dialogView.findViewById<CardView>(R.id.swatchShadowBlack)
        val swatchWhite = dialogView.findViewById<CardView>(R.id.swatchShadowWhite)
        val swatchGold = dialogView.findViewById<CardView>(R.id.swatchShadowGold)
        val swatchCyan = dialogView.findViewById<CardView>(R.id.swatchShadowCyan)
        val swatchRed = dialogView.findViewById<CardView>(R.id.swatchShadowRed)

        if (!previewSampleText.isNullOrEmpty()) {
            tvLivePreview.text = previewSampleText
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        fun updatePreviewAndNotify(notify: Boolean = true) {
            currentConfig.applyTo(tvLivePreview)
            tvShadowValue.text = if (currentConfig.shadowRadius > 0f) "${currentConfig.shadowRadius.toInt()}dp" else "None"
            tvLetterSpacingValue.text = String.format("%.2f", currentConfig.letterSpacing)
            tvLineSpacingValue.text = String.format("%.1fx", currentConfig.lineSpacingMultiplier)

            if (notify) {
                effectsListener?.onEffectsChanged(currentConfig)
            }
        }

        // Initialize state
        if (currentConfig.isBold) toggleGroup.check(R.id.btnBold)
        if (currentConfig.isItalic) toggleGroup.check(R.id.btnItalic)
        if (currentConfig.isUnderline) toggleGroup.check(R.id.btnUnderline)
        if (currentConfig.isAllCaps) toggleGroup.check(R.id.btnCaps)

        seekBarShadow.progress = currentConfig.shadowRadius.toInt()
        seekBarLetterSpacing.progress = (currentConfig.letterSpacing * 100).toInt().coerceIn(0, 30)
        seekBarLineSpacing.progress = ((currentConfig.lineSpacingMultiplier - 0.8f) * 10).toInt().coerceIn(0, 20)

        updatePreviewAndNotify(notify = false)

        // Style Toggles
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            currentConfig = when (checkedId) {
                R.id.btnBold -> currentConfig.copy(isBold = isChecked)
                R.id.btnItalic -> currentConfig.copy(isItalic = isChecked)
                R.id.btnUnderline -> currentConfig.copy(isUnderline = isChecked)
                R.id.btnCaps -> currentConfig.copy(isAllCaps = isChecked)
                else -> currentConfig
            }
            updatePreviewAndNotify()
        }

        // Shadow Radius Slider
        seekBarShadow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                currentConfig = currentConfig.copy(shadowRadius = progress.toFloat())
                updatePreviewAndNotify()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Shadow Colors
        swatchBlack.setOnClickListener {
            currentConfig = currentConfig.copy(shadowColor = Color.BLACK, shadowRadius = currentConfig.shadowRadius.coerceAtLeast(4f))
            if (seekBarShadow.progress == 0) seekBarShadow.progress = 4
            updatePreviewAndNotify()
        }
        swatchWhite.setOnClickListener {
            currentConfig = currentConfig.copy(shadowColor = Color.WHITE, shadowRadius = currentConfig.shadowRadius.coerceAtLeast(6f))
            if (seekBarShadow.progress == 0) seekBarShadow.progress = 6
            updatePreviewAndNotify()
        }
        swatchGold.setOnClickListener {
            currentConfig = currentConfig.copy(shadowColor = "#FFD700".toColorInt(), shadowRadius = currentConfig.shadowRadius.coerceAtLeast(6f))
            if (seekBarShadow.progress == 0) seekBarShadow.progress = 6
            updatePreviewAndNotify()
        }
        swatchCyan.setOnClickListener {
            currentConfig = currentConfig.copy(shadowColor = "#00BCD4".toColorInt(), shadowRadius = currentConfig.shadowRadius.coerceAtLeast(6f))
            if (seekBarShadow.progress == 0) seekBarShadow.progress = 6
            updatePreviewAndNotify()
        }
        swatchRed.setOnClickListener {
            currentConfig = currentConfig.copy(shadowColor = "#FF5722".toColorInt(), shadowRadius = currentConfig.shadowRadius.coerceAtLeast(6f))
            if (seekBarShadow.progress == 0) seekBarShadow.progress = 6
            updatePreviewAndNotify()
        }

        // Letter Spacing Slider (0.0 to 0.30)
        seekBarLetterSpacing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                currentConfig = currentConfig.copy(letterSpacing = progress / 100f)
                updatePreviewAndNotify()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Line Spacing Multiplier (0.8x to 2.0x)
        seekBarLineSpacing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val multiplier = 0.8f + (progress / 10f)
                currentConfig = currentConfig.copy(lineSpacingMultiplier = multiplier)
                updatePreviewAndNotify()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnApply.setOnClickListener {
            effectsListener?.onEffectsChanged(currentConfig)
            dialog.dismiss()
        }

        dialog.show()
    }
}
