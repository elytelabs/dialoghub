package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.models.PresentationStyle
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * Dialog for adjusting text formatting (size and gravity alignment) with live interactive preview.
 * Supports standard Dialog and BottomSheet presentation styles, fluent Builder, and Kotlin DSL.
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
    private var dismissListener: (() -> Unit)? = null
    private var currentSize: Float = DEFAULT_SIZE_SP
    private var currentAlignment: TextAlignment = TextAlignment.CENTER
    private var presentationStyle: PresentationStyle = PresentationStyle.DIALOG
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

    fun setOnDismissListener(listener: () -> Unit) {
        this.dismissListener = listener
    }

    /**
     * Sets custom sample text to display in the live preview box.
     */
    fun setPreviewText(text: String?) {
        this.previewSampleText = text
    }

    /**
     * Configures initial font size in SP.
     */
    fun setInitialTextSize(sizeSp: Float) {
        this.currentSize = sizeSp.coerceIn(MIN_SIZE_SP, MAX_SIZE_SP)
    }

    /**
     * Configures initial text alignment.
     */
    fun setInitialAlignment(alignment: TextAlignment) {
        this.currentAlignment = alignment
    }

    /**
     * Configures presentation mode (Standard Dialog or BottomSheet).
     */
    fun setPresentationStyle(style: PresentationStyle) {
        this.presentationStyle = style
    }

    /**
     * Shows the text format dialog using Kotlin lambda callbacks.
     *
     * @param initialSizeSp Current font size in SP (default: 20sp).
     * @param initialAlignment Current text alignment (default: CENTER).
     * @param previewText Optional custom sample text for the live preview box.
     * @param presentationStyle DIALOG or BOTTOM_SHEET (default: DIALOG).
     * @param onFormatChanged Callback triggered when user updates text size or alignment.
     */
    fun show(
        initialSizeSp: Float = DEFAULT_SIZE_SP,
        initialAlignment: TextAlignment = TextAlignment.CENTER,
        previewText: String? = null,
        presentationStyle: PresentationStyle = this.presentationStyle,
        onFormatChanged: (textSizeSp: Float, alignment: TextAlignment) -> Unit
    ) {
        this.currentSize = initialSizeSp.coerceIn(MIN_SIZE_SP, MAX_SIZE_SP)
        this.currentAlignment = initialAlignment
        this.presentationStyle = presentationStyle
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

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_text_format, null)

        val dialog: Dialog = if (presentationStyle == PresentationStyle.BOTTOM_SHEET) {
            val bottomSheet = BottomSheetDialog(themedContext)
            bottomSheet.setContentView(dialogView)
            dialogView.setBackgroundResource(R.drawable.bg_bottom_sheet)
            bottomSheet.behavior.apply {
                isFitToContents = true
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
            bottomSheet
        } else {
            val standardDialog = Dialog(themedContext)
            standardDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            standardDialog.window?.setWindowAnimations(R.style.DialogHubAnimation)
            standardDialog.setContentView(dialogView)
            dialogView.setBackgroundResource(R.drawable.rounded_background)
            standardDialog
        }

        dialog.setOnDismissListener {
            dismissListener?.invoke()
        }

        val dragHandle = dialogView.findViewById<View>(R.id.dragHandle)
        dragHandle?.visibility = if (presentationStyle == PresentationStyle.BOTTOM_SHEET) View.VISIBLE else View.GONE

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
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

        btnClose?.setOnClickListener {
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

        if (presentationStyle == PresentationStyle.DIALOG) {
            dialog.window?.setLayout(
                (themedContext.resources.displayMetrics.widthPixels * 0.92f).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /**
     * Fluent Builder for [TextFormatDialog].
     */
    class Builder(private val context: Context) {
        private var initialSizeSp: Float = DEFAULT_SIZE_SP
        private var initialAlignment: TextAlignment = TextAlignment.CENTER
        private var previewSampleText: String? = null
        private var presentationStyle: PresentationStyle = PresentationStyle.DIALOG
        private var listener: TextFormatListener? = null
        private var dismissListener: (() -> Unit)? = null

        fun setTextSize(sizeSp: Float) = apply { this.initialSizeSp = sizeSp }
        fun setAlignment(alignment: TextAlignment) = apply { this.initialAlignment = alignment }
        fun setPreviewText(text: String?) = apply { this.previewSampleText = text }
        fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
        fun setOnFormatChanged(listener: (Float, TextAlignment) -> Unit) = apply {
            this.listener = TextFormatListener { size, align -> listener(size, align) }
        }
        fun setOnFormatChanged(listener: TextFormatListener) = apply { this.listener = listener }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): TextFormatDialog {
            val dialog = TextFormatDialog(context)
            dialog.setInitialTextSize(initialSizeSp)
            dialog.setInitialAlignment(initialAlignment)
            dialog.setPreviewText(previewSampleText)
            dialog.setPresentationStyle(presentationStyle)
            listener?.let { dialog.setTextFormatListener(it) }
            dismissListener?.let { dialog.setOnDismissListener(it) }
            return dialog
        }

        fun show(onFormatChanged: ((Float, TextAlignment) -> Unit)? = null): TextFormatDialog {
            val dialog = build()
            if (onFormatChanged != null) {
                dialog.setTextFormatListener { size, align -> onFormatChanged(size, align) }
            }
            dialog.showTextFormatDialog()
            return dialog
        }
    }
}
