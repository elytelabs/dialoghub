package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.FontStyleAdapter
import com.elytelabs.dialoghub.models.PresentationStyle
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Dialog for selecting fonts from app font resources with custom script preview text.
 * Supports standard Dialog and BottomSheet presentation modes, fluent Builder, and Kotlin DSL.
 */
class FontStyleDialog(private val context: Context) {

    private var fonts: List<Int> = emptyList()
    private var previewText: String? = null
    private var selectedFontResId: Int? = null
    private var presentationStyle: PresentationStyle = PresentationStyle.DIALOG
    private var fontPickerListener: FontPickerListener? = null
    private var dismissListener: (() -> Unit)? = null

    /**
     * Traditional interface listener for Java/Kotlin interoperability.
     */
    fun interface FontPickerListener {
        fun onFontSelected(font: Int)
    }

    /**
     * Sets the font picker listener using the traditional interface.
     */
    fun setFontSelectedListener(listener: FontPickerListener) {
        this.fontPickerListener = listener
    }

    /**
     * Sets dismissal listener.
     */
    fun setOnDismissListener(listener: () -> Unit) {
        this.dismissListener = listener
    }

    /**
     * Sets the font resource list.
     */
    fun setFontsList(fonts: List<Int>) {
        this.fonts = fonts
    }

    /**
     * Sets custom preview sample text (e.g., Urdu/Arabic/Hindi text for localized apps).
     */
    fun setPreviewText(text: String?) {
        this.previewText = text
    }

    /**
     * Sets the initially selected font resource ID for highlighting.
     */
    fun setSelectedFont(fontResId: Int?) {
        this.selectedFontResId = fontResId
    }

    /**
     * Configures presentation mode (Standard Dialog or BottomSheet).
     */
    fun setPresentationStyle(style: PresentationStyle) {
        this.presentationStyle = style
    }

    /**
     * Convenience method to show the font style dialog using a Kotlin lambda callback.
     *
     * @param fonts Optional list of font resource IDs.
     * @param previewText Optional custom preview text (e.g., "اردو شاعری" or "Custom Preview").
     * @param selectedFontResId Optional resource ID of the currently selected font.
     * @param presentationStyle DIALOG or BOTTOM_SHEET (default: DIALOG).
     * @param onFontSelected Lambda invoked with the selected font resource ID.
     */
    fun show(
        fonts: List<Int>? = null,
        previewText: String? = null,
        selectedFontResId: Int? = null,
        presentationStyle: PresentationStyle = this.presentationStyle,
        onFontSelected: (fontResId: Int) -> Unit
    ) {
        if (fonts != null) {
            this.fonts = fonts
        }
        if (previewText != null) {
            this.previewText = previewText
        }
        if (selectedFontResId != null) {
            this.selectedFontResId = selectedFontResId
        }
        this.presentationStyle = presentationStyle
        this.fontPickerListener = FontPickerListener { font -> onFontSelected(font) }
        showFontSelectionDialog()
    }

    /**
     * Displays the font style selection dialog.
     */
    fun showFontSelectionDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_font_selector, null)

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

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.fontRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(themedContext, 2)
        val adapter = FontStyleAdapter(themedContext)
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener {
            dialog.dismiss()
        }

        adapter.setOnFontClickListener { fontResId ->
            fontPickerListener?.onFontSelected(fontResId)
            dialog.dismiss()
        }

        val etFontPreviewInput = dialogView.findViewById<EditText>(R.id.etFontPreviewInput)
        val chipUrdu1 = dialogView.findViewById<TextView>(R.id.chipUrdu1)
        val chipUrdu2 = dialogView.findViewById<TextView>(R.id.chipUrdu2)
        val chipEnglish = dialogView.findViewById<TextView>(R.id.chipEnglish)
        val chipNumbers = dialogView.findViewById<TextView>(R.id.chipNumbers)

        if (!previewText.isNullOrEmpty()) {
            etFontPreviewInput.setText(previewText)
        }

        etFontPreviewInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.setPreviewText(s?.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        chipUrdu1.setOnClickListener { etFontPreviewInput.setText(chipUrdu1.text) }
        chipUrdu2.setOnClickListener { etFontPreviewInput.setText(chipUrdu2.text) }
        chipEnglish.setOnClickListener { etFontPreviewInput.setText(chipEnglish.text) }
        chipNumbers.setOnClickListener { etFontPreviewInput.setText(chipNumbers.text) }

        adapter.setFonts(fonts)
        adapter.setPreviewText(previewText ?: etFontPreviewInput.text.toString().ifEmpty { null })
        adapter.setSelectedFont(selectedFontResId)

        dialog.show()

        if (presentationStyle == PresentationStyle.DIALOG) {
            dialog.window?.setLayout(
                (themedContext.resources.displayMetrics.widthPixels * 0.92f).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /**
     * Fluent Builder for [FontStyleDialog].
     */
    class Builder(private val context: Context) {
        private var fonts: List<Int> = emptyList()
        private var previewText: String? = null
        private var selectedFontResId: Int? = null
        private var presentationStyle: PresentationStyle = PresentationStyle.DIALOG
        private var listener: FontPickerListener? = null
        private var dismissListener: (() -> Unit)? = null

        fun setFonts(fonts: List<Int>) = apply { this.fonts = fonts }
        fun setPreviewText(text: String?) = apply { this.previewText = text }
        fun setSelectedFont(fontResId: Int?) = apply { this.selectedFontResId = fontResId }
        fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
        fun setOnFontSelected(listener: (Int) -> Unit) = apply { this.listener = FontPickerListener { listener(it) } }
        fun setOnFontSelected(listener: FontPickerListener) = apply { this.listener = listener }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): FontStyleDialog {
            val dialog = FontStyleDialog(context)
            dialog.setFontsList(fonts)
            dialog.setPreviewText(previewText)
            dialog.setSelectedFont(selectedFontResId)
            dialog.setPresentationStyle(presentationStyle)
            listener?.let { dialog.setFontSelectedListener(it) }
            dismissListener?.let { dialog.setOnDismissListener(it) }
            return dialog
        }

        fun show(onFontSelected: ((Int) -> Unit)? = null): FontStyleDialog {
            val dialog = build()
            if (onFontSelected != null) {
                dialog.setFontSelectedListener { onFontSelected(it) }
            }
            dialog.showFontSelectionDialog()
            return dialog
        }
    }
}
