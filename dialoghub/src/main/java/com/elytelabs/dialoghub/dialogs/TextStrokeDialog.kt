package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.ColorSwatchAdapter
import com.elytelabs.dialoghub.models.PresentationStyle
import com.elytelabs.dialoghub.models.TextStrokeConfig
import com.elytelabs.dialoghub.utils.ColorPalettes
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Dialog for configuring text stroke / outline thickness and color with rich curated palettes
 * and active ring selection indicators.
 */
class TextStrokeDialog(private val context: Context) {

    private var currentConfig: TextStrokeConfig = TextStrokeConfig()
    private var previewSampleText: String? = null
    private var presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
    private var strokeListener: TextStrokeListener? = null
    private var dismissListener: (() -> Unit)? = null

    fun interface TextStrokeListener {
        fun onStrokeChanged(config: TextStrokeConfig)
    }

    fun setConfig(config: TextStrokeConfig) = apply { this.currentConfig = config }
    fun setPreviewText(text: String?) = apply { this.previewSampleText = text }
    fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
    fun setStrokeListener(listener: TextStrokeListener) = apply { this.strokeListener = listener }
    fun setStrokeListener(listener: (TextStrokeConfig) -> Unit) = apply {
        this.strokeListener = TextStrokeListener { listener(it) }
    }
    fun setOnDismissListener(listener: () -> Unit) = apply { this.dismissListener = listener }

    fun show(
        initialConfig: TextStrokeConfig = this.currentConfig,
        previewText: String? = null,
        presentationStyle: PresentationStyle = this.presentationStyle,
        onStrokeChanged: (config: TextStrokeConfig) -> Unit
    ) {
        this.currentConfig = initialConfig
        if (previewText != null) this.previewSampleText = previewText
        this.presentationStyle = presentationStyle
        this.strokeListener = TextStrokeListener { onStrokeChanged(it) }
        showTextStrokeDialog()
    }

    fun showTextStrokeDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_text_stroke, null)

        val bottomSheet = BottomSheetDialog(themedContext)
        bottomSheet.setContentView(dialogView)
        dialogView.setBackgroundResource(R.drawable.bg_bottom_sheet)
        bottomSheet.behavior.apply {
            isFitToContents = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheet.setOnDismissListener {
            dismissListener?.invoke()
        }

        val dragHandle = dialogView.findViewById<View>(R.id.dragHandle)
        dragHandle?.visibility = View.VISIBLE

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
        val tvLivePreview = dialogView.findViewById<TextView>(R.id.tvLivePreview)
        val tvStrokeWidthValue = dialogView.findViewById<TextView>(R.id.tvStrokeWidthValue)
        val seekBarStrokeWidth = dialogView.findViewById<SeekBar>(R.id.seekBarStrokeWidth)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyStroke)
        val rvStrokeColors = dialogView.findViewById<RecyclerView>(R.id.rvStrokeColors)

        if (!previewSampleText.isNullOrEmpty()) {
            tvLivePreview.text = previewSampleText
        }

        btnClose?.setOnClickListener { bottomSheet.dismiss() }

        fun updatePreviewAndNotify(notify: Boolean = true) {
            currentConfig.applyTo(tvLivePreview)
            tvStrokeWidthValue.text = "${currentConfig.strokeWidthDp.toInt()}dp"

            if (notify) {
                strokeListener?.onStrokeChanged(currentConfig)
            }
        }

        seekBarStrokeWidth.progress = currentConfig.strokeWidthDp.toInt()
        tvStrokeWidthValue.text = "${currentConfig.strokeWidthDp.toInt()}dp"
        updatePreviewAndNotify(notify = false)

        seekBarStrokeWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                currentConfig = currentConfig.copy(
                    strokeWidthDp = progress.toFloat(),
                    isEnabled = progress > 0
                )
                tvStrokeWidthValue.text = "${progress}dp"
                updatePreviewAndNotify()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Setup Swatches with Active Ring Selection
        rvStrokeColors.layoutManager = LinearLayoutManager(themedContext, LinearLayoutManager.HORIZONTAL, false)
        val colorSwatchAdapter = ColorSwatchAdapter(includeNoneOption = false)
        rvStrokeColors.adapter = colorSwatchAdapter
        colorSwatchAdapter.setColors(ColorPalettes.ALL_CURATED)
        colorSwatchAdapter.setSelectedColor(currentConfig.strokeColor)

        colorSwatchAdapter.setOnSwatchClickListener { color, _ ->
            currentConfig = currentConfig.copy(
                strokeColor = color,
                isEnabled = currentConfig.strokeWidthDp > 0
            )
            updatePreviewAndNotify()
        }

        // Category Chips
        val chipAll = dialogView.findViewById<TextView>(R.id.chipStrokeAll)
        val chipBold = dialogView.findViewById<TextView>(R.id.chipStrokeBold)
        val chipNeon = dialogView.findViewById<TextView>(R.id.chipStrokeNeon)
        val chipCalm = dialogView.findViewById<TextView>(R.id.chipStrokeCalm)
        val chipPastel = dialogView.findViewById<TextView>(R.id.chipStrokePastel)
        val chipDark = dialogView.findViewById<TextView>(R.id.chipStrokeDark)
        val chipVintage = dialogView.findViewById<TextView>(R.id.chipStrokeVintage)

        val chips = listOfNotNull(chipAll, chipBold, chipNeon, chipCalm, chipPastel, chipDark, chipVintage)

        fun selectChip(selectedChip: TextView, colors: List<Int>) {
            chips.forEach { chip ->
                if (chip == selectedChip) {
                    chip.backgroundTintList = android.content.res.ColorStateList.valueOf("#1F2937".toColorInt())
                    chip.setTextColor("#FFFFFF".toColorInt())
                    chip.setTypeface(null, Typeface.BOLD)
                } else {
                    chip.backgroundTintList = android.content.res.ColorStateList.valueOf("#F3F4F6".toColorInt())
                    chip.setTextColor("#4B5563".toColorInt())
                    chip.setTypeface(null, Typeface.NORMAL)
                }
            }
            colorSwatchAdapter.setColors(colors)
        }

        chipAll?.setOnClickListener { selectChip(chipAll, ColorPalettes.ALL_CURATED) }
        chipBold?.setOnClickListener { selectChip(chipBold, ColorPalettes.MOTIVATIONAL_BOLD) }
        chipNeon?.setOnClickListener { selectChip(chipNeon, ColorPalettes.AESTHETIC_NEON) }
        chipCalm?.setOnClickListener { selectChip(chipCalm, ColorPalettes.NATURE_SUFI_CALM) }
        chipPastel?.setOnClickListener { selectChip(chipPastel, ColorPalettes.PASTEL_SOFT) }
        chipDark?.setOnClickListener { selectChip(chipDark, ColorPalettes.MELANCHOLY_DARK) }
        chipVintage?.setOnClickListener { selectChip(chipVintage, ColorPalettes.VINTAGE_EARTHY) }

        btnApply.setOnClickListener {
            strokeListener?.onStrokeChanged(currentConfig)
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    class Builder(private val context: Context) {
        private var config = TextStrokeConfig()
        private var previewText: String? = null
        private var presentationStyle = PresentationStyle.BOTTOM_SHEET
        private var listener: TextStrokeListener? = null
        private var dismissListener: (() -> Unit)? = null

        fun setConfig(config: TextStrokeConfig) = apply { this.config = config }
        fun setPreviewText(text: String?) = apply { this.previewText = text }
        fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
        fun setOnStrokeChanged(listener: (TextStrokeConfig) -> Unit) = apply {
            this.listener = TextStrokeListener { listener(it) }
        }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): TextStrokeDialog {
            val dialog = TextStrokeDialog(context)
            dialog.setConfig(config)
            dialog.setPreviewText(previewText)
            dialog.setPresentationStyle(presentationStyle)
            listener?.let { dialog.setStrokeListener(it) }
            dismissListener?.let { dialog.setOnDismissListener(it) }
            return dialog
        }

        fun show(onStrokeChanged: ((TextStrokeConfig) -> Unit)? = null): TextStrokeDialog {
            val dialog = build()
            if (onStrokeChanged != null) {
                dialog.setStrokeListener { onStrokeChanged(it) }
            }
            dialog.showTextStrokeDialog()
            return dialog
        }
    }
}
