package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.FontStyleAdapter
import androidx.core.graphics.drawable.toDrawable

/**
 * Dialog for selecting fonts from app font resources.
 */
class FontStyleDialog(private val context: Context) {

    private var fonts: List<Int> = emptyList()
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
     * Convenience method to show the font style dialog using a Kotlin lambda callback.
     *
     * @param fonts Optional list of font resource IDs (if not previously set).
     * @param onFontSelected Lambda invoked with the selected font resource ID.
     */
    fun show(fonts: List<Int>? = null, onFontSelected: (fontResId: Int) -> Unit) {
        if (fonts != null) {
            this.fonts = fonts
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

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_font_selector, null)
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.fontRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        val adapter = FontStyleAdapter(context)
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageView>(R.id.backButton).setOnClickListener {
            dialog.dismiss()
        }

        adapter.setOnFontClickListener { fontResId ->
            fontPickerListener?.onFontSelected(fontResId)
            dialog.dismiss()
        }

        adapter.setFonts(fonts)

        dialog.show()
    }
}