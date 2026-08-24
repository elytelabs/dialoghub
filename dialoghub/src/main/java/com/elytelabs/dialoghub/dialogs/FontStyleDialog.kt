package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.FontStyleAdapter
import com.elytelabs.dialoghub.monetization.DefaultItemLockProvider
import com.elytelabs.dialoghub.monetization.ItemLockProvider
import com.elytelabs.dialoghub.monetization.LockableItem
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Dialog for selecting fonts from app font resources with custom script preview text.
 * Supports lockable items (IAP/Rewarded Ads), fluent Builder, and Kotlin DSL.
 */
class FontStyleDialog(private val context: Context) {

    private var fonts: List<Int> = emptyList()
    private var previewText: String? = null
    private var selectedFontResId: Int? = null
    private var fontPickerListener: FontPickerListener? = null
    private var lockProvider: ItemLockProvider? = null
    private var lockedItemClickListener: ((LockableItem.Font, unlock: () -> Unit) -> Unit)? = null
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
     * Sets monetization item lock provider.
     */
    fun setLockProvider(provider: ItemLockProvider?) {
        this.lockProvider = provider
    }

    /**
     * Sets click listener for locked font items.
     */
    fun setOnLockedItemClickListener(listener: (LockableItem.Font, unlock: () -> Unit) -> Unit) {
        this.lockedItemClickListener = listener
    }

    /**
     * Convenience method to show the font style dialog using a Kotlin lambda callback.
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

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.fontRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(themedContext, 2)
        val adapter = FontStyleAdapter(themedContext)
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener {
            bottomSheet.dismiss()
        }

        adapter.setOnFontClickListener { fontResId ->
            fontPickerListener?.onFontSelected(fontResId)
            bottomSheet.dismiss()
        }

        adapter.setLockProvider(lockProvider)
        lockedItemClickListener?.let { listener ->
            adapter.setOnLockedItemClickListener(listener)
        }

        adapter.setFonts(fonts)
        adapter.setPreviewText(previewText)
        adapter.setSelectedFont(selectedFontResId)

        bottomSheet.show()
    }

    /**
     * Fluent Builder for [FontStyleDialog].
     */
    class Builder(private val context: Context) {
        private var fonts: List<Int> = emptyList()
        private var previewText: String? = null
        private var selectedFontResId: Int? = null
        private var listener: FontPickerListener? = null
        private var lockProvider: ItemLockProvider? = null
        private var lockedItemClickListener: ((LockableItem.Font, unlock: () -> Unit) -> Unit)? = null
        private var dismissListener: (() -> Unit)? = null

        fun setFonts(fonts: List<Int>) = apply { this.fonts = fonts }
        fun setFonts(vararg fonts: Int) = apply { this.fonts = fonts.toList() }
        fun setPreviewText(text: String?) = apply { this.previewText = text }
        fun setSelectedFont(fontResId: Int?) = apply { this.selectedFontResId = fontResId }
        fun setLockProvider(provider: ItemLockProvider) = apply { this.lockProvider = provider }
        fun setLockedFonts(vararg fontResIds: Int) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockFonts(*fontResIds)
        }
        fun setLockedFonts(fontResIds: Collection<Int>) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockFonts(fontResIds)
        }
        fun setOnLockedItemClicked(listener: (LockableItem.Font, unlock: () -> Unit) -> Unit) = apply {
            this.lockedItemClickListener = listener
        }
        fun setOnFontSelected(listener: (Int) -> Unit) = apply { this.listener = FontPickerListener { listener(it) } }
        fun setOnFontSelected(listener: FontPickerListener) = apply { this.listener = listener }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): FontStyleDialog {
            val dialog = FontStyleDialog(context)
            dialog.setFontsList(fonts)
            dialog.setPreviewText(previewText)
            dialog.setSelectedFont(selectedFontResId)
            dialog.setLockProvider(lockProvider)
            lockedItemClickListener?.let { dialog.setOnLockedItemClickListener(it) }
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
