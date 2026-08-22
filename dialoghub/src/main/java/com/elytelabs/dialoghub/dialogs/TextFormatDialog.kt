package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.elytelabs.dialoghub.R
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * Dialog for adjusting text formatting (size and gravity alignment) with live interactive preview.
 */
class TextFormatDialog(private val context: Context) {

    /**
     * Text alignment options supported by the dialog.
     */
    enum class TextAlignment(val gravity: Int) {
        LEFT(Gravity.START or Gravity.CENTER_VERTICAL),
        CENTER(Gravity.CENTER),
        RIGHT(Gravity.END or Gravity.CENTER_VERTICAL)
    }

    private var formatListener: TextFormatListener? = null
    private var currentSize: Float = DEFAULT_SIZE_SP
    private var currentAlignment: TextAlignment = TextAlignment.CENTER
    private var previewSampleText: String? = null

    companion object {
        const val MIN_SIZE_SP = 12f
        const val MAX_SIZE_SP = 42f
        const val DEFAULT_SIZE_SP = 20f
    }

    /**
     * Traditional interface listener.
     */
    fun interface TextFormatListener {
        fun onFormatChanged(textSizeSp: Float, alignment: TextAlignment)
    }

    fun setTextFormatListener(listener: TextFormatListener) {
        this.formatListener = listener
    }

    /**
     * Sets custom sample text to display in the live preview box.
     */
    fun setPreviewText(text: String?) {
        this.previewSampleText = text
    }

    /**
     * Shows the text format dialog using Kotlin lambda callbacks.
     *
     * @param initialSizeSp Current font size in SP (default: 20sp).
     * @param initialAlignment Current text alignment (default: CENTER).
     * @param previewText Optional custom sample text for the live preview box.
     * @param onFormatChanged Callback triggered when user updates text size or alignment.
     */
    fun show(
        initialSizeSp: Float = DEFAULT_SIZE_SP,
        initialAlignment: TextAlignment = TextAlignment.CENTER,
        previewText: String? = null,
        onFormatChanged: (textSizeSp: Float, alignment: TextAlignment) -> Unit
    ) {
        this.currentSize = initialSizeSp.coerceIn(MIN_SIZE_SP, MAX_SIZE_SP)
        this.currentAlignment = initialAlignment
        if (previewText != null) {
            this.previewSampleText = previewText
        }
        this.formatListener = TextFormatListener { size, alignment ->
            onFormatChanged(size, alignment)
        }
        showTextFormatDialog()
    }

    /**
     * Displays the text format dialog.
     */
    fun showTextFormatDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_text_format, null)
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.DialogHubAnimation)
        dialog.setContentView(dialogView)

        val btnBack = dialogView.findViewById<ImageButton>(R.id.btnBack)
        val tvLivePreview = dialogView.findViewById<TextView>(R.id.tvLivePreview)
        val tvSizeValue = dialogView.findViewById<TextView>(R.id.tvSizeValue)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBarTextSize)
        val toggleGroup = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupAlignment)

        if (!previewSampleText.isNullOrEmpty()) {
            tvLivePreview.text = previewSampleText
        }

        // Initialize preview appearance
        tvLivePreview.textSize = currentSize
        tvLivePreview.gravity = currentAlignment.gravity
        tvSizeValue.text = "${currentSize.toInt()}sp"

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        // Configure seek bar
        val maxProgress = (MAX_SIZE_SP - MIN_SIZE_SP).toInt()
        seekBar.max = maxProgress
        seekBar.progress = (currentSize - MIN_SIZE_SP).toInt()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                currentSize = MIN_SIZE_SP + progress
                tvSizeValue.text = "${currentSize.toInt()}sp"
                tvLivePreview.textSize = currentSize
                formatListener?.onFormatChanged(currentSize, currentAlignment)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Configure initial checked alignment button
        when (currentAlignment) {
            TextAlignment.LEFT -> toggleGroup.check(R.id.btnAlignLeft)
            TextAlignment.CENTER -> toggleGroup.check(R.id.btnAlignCenter)
            TextAlignment.RIGHT -> toggleGroup.check(R.id.btnAlignRight)
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnAlignLeft -> currentAlignment = TextAlignment.LEFT
                    R.id.btnAlignCenter -> currentAlignment = TextAlignment.CENTER
                    R.id.btnAlignRight -> currentAlignment = TextAlignment.RIGHT
                }
                tvLivePreview.gravity = currentAlignment.gravity
                formatListener?.onFormatChanged(currentSize, currentAlignment)
            }
        }

        dialog.show()
    }
}
