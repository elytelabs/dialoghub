package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.FontStyleAdapter
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import androidx.core.graphics.drawable.toDrawable

/**
 * Dialog for selecting fonts from app font resources with custom script preview text.
 */
class FontStyleDialog(private val context: Context) {

    private var fonts: List<Int> = emptyList()
    private var previewText: String? = null
    private var selectedFontResId: Int? = null
    private var fontPickerListener: FontPickerListener? = null

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
     * Convenience method to show the font style dialog using a Kotlin lambda callback.
     *
     * @param fonts Optional list of font resource IDs.
     * @param previewText Optional custom preview text (e.g., "اردو شاعری" or "Custom Preview").
     * @param selectedFontResId Optional resource ID of the currently selected font.
     * @param onFontSelected Lambda invoked with the selected font resource ID.
     */
    fun show(
        fonts: List<Int>? = null,
        previewText: String? = null,
        selectedFontResId: Int? = null,
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
        val dialog = Dialog(themedContext)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setWindowAnimations(R.style.DialogHubAnimation)
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.fontRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(themedContext, 2)
        val adapter = FontStyleAdapter(themedContext)
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            dialog.dismiss()
        }

        adapter.setOnFontClickListener { fontResId ->
            fontPickerListener?.onFontSelected(fontResId)
            dialog.dismiss()
        }

        adapter.setFonts(fonts)
        adapter.setPreviewText(previewText)
        adapter.setSelectedFont(selectedFontResId)

        dialog.show()
    }
}