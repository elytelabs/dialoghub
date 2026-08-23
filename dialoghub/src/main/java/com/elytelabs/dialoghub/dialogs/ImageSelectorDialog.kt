package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.ImageAdapter
import com.elytelabs.dialoghub.models.PresentationStyle
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Dialog for selecting background drawable images, custom photos from gallery, or custom colors.
 * Supports standard Dialog and BottomSheet presentation styles, fluent Builder, and Kotlin DSL.
 */
class ImageSelectorDialog(private val context: Context) {

    private var backgrounds: List<Int> = emptyList()
    private var enableGalleryPick: Boolean = false
    private var selectedBackgroundResId: Int? = null
    private var presentationStyle: PresentationStyle = PresentationStyle.DIALOG
    private var imagePickerListener: ImagePickerListener? = null
    private var galleryClickListener: (() -> Unit)? = null
    private var dismissListener: (() -> Unit)? = null
    private val colorPickerDialog = ColorPickerDialog(context)

    /**
     * Traditional interface listener for Java/Kotlin interoperability.
     */
    interface ImagePickerListener {
        fun onImageSelected(imageResource: Int)
        fun onColorSelected(color: Int)
    }

    /**
     * Sets the image picker listener using the traditional interface.
     */
    fun setImageSelectedListener(listener: ImagePickerListener) {
        this.imagePickerListener = listener
    }

    /**
     * Sets dismissal listener.
     */
    fun setOnDismissListener(listener: () -> Unit) {
        this.dismissListener = listener
    }

    /**
     * Sets the list of background drawable resource IDs.
     */
    fun setBackgroundsList(backgrounds: List<Int>) {
        this.backgrounds = backgrounds
    }

    /**
     * Enables or disables the "Pick from Gallery" tile.
     */
    fun setEnableGalleryPick(enable: Boolean, onGalleryClick: (() -> Unit)? = null) {
        this.enableGalleryPick = enable
        this.galleryClickListener = onGalleryClick
    }

    /**
     * Sets the initially selected background resource ID for highlighting.
     */
    fun setSelectedBackground(resId: Int?) {
        this.selectedBackgroundResId = resId
    }

    /**
     * Configures presentation mode (Standard Dialog or BottomSheet).
     */
    fun setPresentationStyle(style: PresentationStyle) {
        this.presentationStyle = style
        this.colorPickerDialog.setPresentationStyle(style)
    }

    /**
     * Convenience method to show the image selector dialog using Kotlin lambda callbacks.
     *
     * @param backgrounds Optional list of drawable resource IDs.
     * @param selectedBackgroundResId Optional currently selected background resource ID.
     * @param presentationStyle DIALOG or BOTTOM_SHEET (default: DIALOG).
     * @param onPickFromGallery Optional callback if user clicks the "Pick from Gallery" tile.
     * @param onImageSelected Callback invoked when a background image is chosen.
     * @param onColorSelected Callback invoked when a color is chosen from the color picker.
     */
    fun show(
        backgrounds: List<Int>? = null,
        selectedBackgroundResId: Int? = null,
        presentationStyle: PresentationStyle = this.presentationStyle,
        onPickFromGallery: (() -> Unit)? = null,
        onImageSelected: ((imageResId: Int) -> Unit)? = null,
        onColorSelected: ((color: Int) -> Unit)? = null
    ) {
        if (backgrounds != null) {
            this.backgrounds = backgrounds
        }
        if (selectedBackgroundResId != null) {
            this.selectedBackgroundResId = selectedBackgroundResId
        }
        this.presentationStyle = presentationStyle
        this.colorPickerDialog.setPresentationStyle(presentationStyle)
        if (onPickFromGallery != null) {
            this.enableGalleryPick = true
            this.galleryClickListener = onPickFromGallery
        }
        this.imagePickerListener = object : ImagePickerListener {
            override fun onImageSelected(imageResource: Int) {
                onImageSelected?.invoke(imageResource)
            }

            override fun onColorSelected(color: Int) {
                onColorSelected?.invoke(color)
            }
        }
        showImageSelectionDialog()
    }

    /**
     * Displays the image and color selection dialog.
     */
    fun showImageSelectionDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_image_selector, null)

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

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.imageRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(themedContext, 3)
        val adapter = ImageAdapter()
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener {
            dialog.dismiss()
        }

        adapter.setOnImageClickListener { imageResource ->
            imagePickerListener?.onImageSelected(imageResource)
            dialog.dismiss()
        }

        adapter.setOnGalleryClickListener {
            galleryClickListener?.invoke()
            dialog.dismiss()
        }

        adapter.setOnColorPickerClickListener {
            colorPickerDialog.setColorSelectedListener { color ->
                imagePickerListener?.onColorSelected(color)
            }

            colorPickerDialog.showColorPickerDialog()
            dialog.dismiss()
        }

        adapter.setBackgrounds(backgrounds)
        adapter.setEnableGalleryPick(enableGalleryPick)
        adapter.setSelectedBackground(selectedBackgroundResId)

        dialog.show()

        if (presentationStyle == PresentationStyle.DIALOG) {
            dialog.window?.setLayout(
                (themedContext.resources.displayMetrics.widthPixels * 0.92f).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /**
     * Fluent Builder for [ImageSelectorDialog].
     */
    class Builder(private val context: Context) {
        private var backgrounds: List<Int> = emptyList()
        private var enableGalleryPick: Boolean = false
        private var selectedBackgroundResId: Int? = null
        private var presentationStyle: PresentationStyle = PresentationStyle.DIALOG
        private var imagePickerListener: ImagePickerListener? = null
        private var galleryClickListener: (() -> Unit)? = null
        private var dismissListener: (() -> Unit)? = null

        fun setBackgrounds(backgrounds: List<Int>) = apply { this.backgrounds = backgrounds }
        fun setSelectedBackground(resId: Int?) = apply { this.selectedBackgroundResId = resId }
        fun setEnableGalleryPick(enable: Boolean, onGalleryClick: (() -> Unit)? = null) = apply {
            this.enableGalleryPick = enable
            this.galleryClickListener = onGalleryClick
        }
        fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
        fun setOnImageSelected(listener: (Int) -> Unit) = apply {
            val prev = this.imagePickerListener
            this.imagePickerListener = object : ImagePickerListener {
                override fun onImageSelected(imageResource: Int) = listener(imageResource)
                override fun onColorSelected(color: Int) { prev?.onColorSelected(color) }
            }
        }
        fun setOnColorSelected(listener: (Int) -> Unit) = apply {
            val prev = this.imagePickerListener
            this.imagePickerListener = object : ImagePickerListener {
                override fun onImageSelected(imageResource: Int) { prev?.onImageSelected(imageResource) }
                override fun onColorSelected(color: Int) = listener(color)
            }
        }
        fun setImagePickerListener(listener: ImagePickerListener) = apply { this.imagePickerListener = listener }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): ImageSelectorDialog {
            val dialog = ImageSelectorDialog(context)
            dialog.setBackgroundsList(backgrounds)
            dialog.setSelectedBackground(selectedBackgroundResId)
            dialog.setEnableGalleryPick(enableGalleryPick, galleryClickListener)
            dialog.setPresentationStyle(presentationStyle)
            imagePickerListener?.let { dialog.setImageSelectedListener(it) }
            dismissListener?.let { dialog.setOnDismissListener(it) }
            return dialog
        }

        fun show(): ImageSelectorDialog {
            val dialog = build()
            dialog.showImageSelectionDialog()
            return dialog
        }
    }
}
