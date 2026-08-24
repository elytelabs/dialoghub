package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.content.Context
import android.graphics.Color
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
import com.elytelabs.dialoghub.models.TextHighlightConfig
import com.elytelabs.dialoghub.utils.ColorPalettes
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Dialog for configuring text background ribbons/highlight cards with rich curated palettes,
 * None/Transparent support, and active ring selection indicators.
 */
class TextHighlightDialog(private val context: Context) {

    private var currentConfig: TextHighlightConfig = TextHighlightConfig()
    private var previewSampleText: String? = null
    private var presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
    private var highlightListener: TextHighlightListener? = null
    private var dismissListener: (() -> Unit)? = null

    fun interface TextHighlightListener {
        fun onHighlightChanged(config: TextHighlightConfig)
    }

    fun setConfig(config: TextHighlightConfig) = apply { this.currentConfig = config }
    fun setPreviewText(text: String?) = apply { this.previewSampleText = text }
    fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
    fun setHighlightListener(listener: TextHighlightListener) = apply { this.highlightListener = listener }
    fun setHighlightListener(listener: (TextHighlightConfig) -> Unit) = apply {
        this.highlightListener = TextHighlightListener { listener(it) }
    }
    fun setOnDismissListener(listener: () -> Unit) = apply { this.dismissListener = listener }

    fun show(
        initialConfig: TextHighlightConfig = this.currentConfig,
        previewText: String? = null,
        presentationStyle: PresentationStyle = this.presentationStyle,
        onHighlightChanged: (config: TextHighlightConfig) -> Unit
    ) {
        this.currentConfig = initialConfig
        if (previewText != null) this.previewSampleText = previewText
        this.presentationStyle = presentationStyle
        this.highlightListener = TextHighlightListener { onHighlightChanged(it) }
        showTextHighlightDialog()
    }

    fun showTextHighlightDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_text_highlight, null)

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
        val tvRadiusValue = dialogView.findViewById<TextView>(R.id.tvRadiusValue)
        val seekBarRadius = dialogView.findViewById<SeekBar>(R.id.seekBarRadius)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyHighlight)
        val rvRibbonColors = dialogView.findViewById<RecyclerView>(R.id.rvRibbonColors)

        if (!previewSampleText.isNullOrEmpty()) {
            tvLivePreview.text = previewSampleText
        }

        btnClose?.setOnClickListener { bottomSheet.dismiss() }

        fun updatePreviewAndNotify(notify: Boolean = true) {
            currentConfig.applyTo(tvLivePreview)
            tvRadiusValue.text = "${currentConfig.cornerRadiusDp.toInt()}dp"

            if (notify) {
                highlightListener?.onHighlightChanged(currentConfig)
            }
        }

        seekBarRadius.progress = currentConfig.cornerRadiusDp.toInt()
        tvRadiusValue.text = "${currentConfig.cornerRadiusDp.toInt()}dp"
        updatePreviewAndNotify(notify = false)

        seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                currentConfig = currentConfig.copy(cornerRadiusDp = progress.toFloat())
                tvRadiusValue.text = "${progress}dp"
                updatePreviewAndNotify()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Setup Swatches with Active Ring Selection & None Option
        rvRibbonColors.layoutManager = LinearLayoutManager(themedContext, LinearLayoutManager.HORIZONTAL, false)
        val colorSwatchAdapter = ColorSwatchAdapter(includeNoneOption = true)
        rvRibbonColors.adapter = colorSwatchAdapter
        colorSwatchAdapter.setColors(ColorPalettes.ALL_CURATED)
        colorSwatchAdapter.setSelectedColor(
            if (currentConfig.isEnabled) currentConfig.backgroundColor else null,
            isNone = !currentConfig.isEnabled
        )

        colorSwatchAdapter.setOnSwatchClickListener { color, isNone ->
            currentConfig = currentConfig.copy(
                backgroundColor = if (isNone) Color.TRANSPARENT else color,
                isEnabled = !isNone
            )
            updatePreviewAndNotify()
        }

        // Category Chips
        val chipAll = dialogView.findViewById<TextView>(R.id.chipRibbonAll)
        val chipBold = dialogView.findViewById<TextView>(R.id.chipRibbonBold)
        val chipNeon = dialogView.findViewById<TextView>(R.id.chipRibbonNeon)
        val chipCalm = dialogView.findViewById<TextView>(R.id.chipRibbonCalm)
        val chipPastel = dialogView.findViewById<TextView>(R.id.chipRibbonPastel)
        val chipDark = dialogView.findViewById<TextView>(R.id.chipRibbonDark)
        val chipVintage = dialogView.findViewById<TextView>(R.id.chipRibbonVintage)

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
            highlightListener?.onHighlightChanged(currentConfig)
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    class Builder(private val context: Context) {
        private var config = TextHighlightConfig()
        private var previewText: String? = null
        private var presentationStyle = PresentationStyle.BOTTOM_SHEET
        private var listener: TextHighlightListener? = null
        private var dismissListener: (() -> Unit)? = null

        fun setConfig(config: TextHighlightConfig) = apply { this.config = config }
        fun setPreviewText(text: String?) = apply { this.previewText = text }
        fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
        fun setOnHighlightChanged(listener: (TextHighlightConfig) -> Unit) = apply {
            this.listener = TextHighlightListener { listener(it) }
        }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): TextHighlightDialog {
            val dialog = TextHighlightDialog(context)
            dialog.setConfig(config)
            dialog.setPreviewText(previewText)
            dialog.setPresentationStyle(presentationStyle)
            listener?.let { dialog.setHighlightListener(it) }
            dismissListener?.let { dialog.setOnDismissListener(it) }
            return dialog
        }

        fun show(onHighlightChanged: ((TextHighlightConfig) -> Unit)? = null): TextHighlightDialog {
            val dialog = build()
            if (onHighlightChanged != null) {
                dialog.setHighlightListener { onHighlightChanged(it) }
            }
            dialog.showTextHighlightDialog()
            return dialog
        }
    }
}
