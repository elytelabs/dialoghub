package com.elytelabs.dialoghub.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.ImageAdapter
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

import com.elytelabs.dialoghub.monetization.DefaultItemLockProvider
import com.elytelabs.dialoghub.monetization.ItemLockProvider
import com.elytelabs.dialoghub.monetization.LockableItem
import androidx.core.graphics.drawable.toDrawable

/**
 * Dialog for selecting background drawable images, custom photos from gallery, or custom colors.
 * Supports monetization locking (IAP/Rewarded Ads), fluent Builder, and Kotlin DSL.
 */
class ImageSelectorDialog(private val context: Context) {

    private var backgrounds: List<Int> = emptyList()
    private var enableGalleryPick: Boolean = false
    private var selectedBackgroundResId: Int? = null
    private var imagePickerListener: ImagePickerListener? = null
    private var galleryClickListener: (() -> Unit)? = null
    private var lockProvider: ItemLockProvider? = null
    private var lockedItemClickListener: ((LockableItem, unlock: () -> Unit) -> Unit)? = null
    private var dismissListener: (() -> Unit)? = null

    /**
     * Traditional interface listener for Java/Kotlin interoperability.
     */
    interface ImagePickerListener {
        fun onImageSelected(imageResource: Int)
        fun onColorSelected(color: Int)
    }

    fun setImageSelectedListener(listener: ImagePickerListener) {
        this.imagePickerListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        this.dismissListener = listener
    }

    fun setBackgroundsList(backgrounds: List<Int>) {
        this.backgrounds = backgrounds
    }

    fun setEnableGalleryPick(enable: Boolean, onGalleryClick: (() -> Unit)? = null) {
        this.enableGalleryPick = enable
        this.galleryClickListener = onGalleryClick
    }

    fun setSelectedBackground(resId: Int?) {
        this.selectedBackgroundResId = resId
    }

    fun setLockProvider(provider: ItemLockProvider?) {
        this.lockProvider = provider
    }

    fun setOnLockedItemClickListener(listener: (LockableItem, unlock: () -> Unit) -> Unit) {
        this.lockedItemClickListener = listener
    }

    fun show(
        backgrounds: List<Int>? = null,
        selectedBackgroundResId: Int? = null,
        onPickFromGallery: (() -> Unit)? = null,
        onImageSelected: ((imageResId: Int) -> Unit)? = null,
        onColorSelected: ((color: Int) -> Unit)? = null
    ) {
        if (backgrounds != null) this.backgrounds = backgrounds
        if (selectedBackgroundResId != null) this.selectedBackgroundResId = selectedBackgroundResId
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

    @SuppressLint("InflateParams")
    fun showImageSelectionDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_image_selector, null)

        val bottomSheet = BottomSheetDialog(themedContext)
        bottomSheet.setContentView(dialogView)
        dialogView.setBackgroundResource(R.drawable.bg_bottom_sheet)
        bottomSheet.behavior.apply {
            isFitToContents = false
            peekHeight = (context.resources.displayMetrics.heightPixels * 0.65).toInt()
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        bottomSheet.window?.setDimAmount(0.05f)
        bottomSheet.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        bottomSheet.setOnShowListener { dialog ->
            val d = dialog as? BottomSheetDialog
            val bottomSheetInternal = d?.findViewById<android.widget.FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetInternal?.background = null
            bottomSheetInternal?.setBackgroundColor(Color.TRANSPARENT)
        }

        bottomSheet.setOnDismissListener {
            dismissListener?.invoke()
        }

        val dragHandle = dialogView.findViewById<View>(R.id.dragHandle)
        dragHandle?.visibility = View.VISIBLE

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.imageRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(themedContext, 3)
        val adapter = ImageAdapter()
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener {
            bottomSheet.dismiss()
        }

        adapter.setOnImageClickListener { imageResource ->
            imagePickerListener?.onImageSelected(imageResource)
            bottomSheet.dismiss()
        }

        adapter.setOnGalleryClickListener {
            galleryClickListener?.invoke()
            bottomSheet.dismiss()
        }

        adapter.setOnColorPickerClickListener {
            TextStudioDialog.Builder(context)
                .setTabs(com.elytelabs.dialoghub.models.StudioTab.COLOR)
                .setShowPreviewPane(false)
                .apply {
                    lockProvider?.let { setLockProvider(it) }
                    lockedItemClickListener?.let { listener ->
                        setOnLockedItemClicked(listener)
                    }
                }
                .setOnTypographyApplied { applied ->
                    imagePickerListener?.onColorSelected(applied.textColor)
                }
                .show()
            bottomSheet.dismiss()
        }

        adapter.setLockProvider(lockProvider)
        lockedItemClickListener?.let { listener ->
            adapter.setOnLockedItemClickListener(listener)
        }

        adapter.setBackgrounds(backgrounds)
        adapter.setSelectedBackground(selectedBackgroundResId)
        adapter.setEnableGalleryPick(enableGalleryPick)
        bottomSheet.show()
    }

    class Builder(private val context: Context) {
        private var backgrounds: List<Int> = emptyList()
        private var enableGalleryPick: Boolean = false
        private var selectedBackgroundResId: Int? = null
        private var lockProvider: ItemLockProvider? = null
        private var lockedItemClickListener: ((LockableItem, unlock: () -> Unit) -> Unit)? = null
        private var imageListener: ((Int) -> Unit)? = null
        private var colorListener: ((Int) -> Unit)? = null
        private var galleryListener: (() -> Unit)? = null
        private var dismissListener: (() -> Unit)? = null

        fun setBackgrounds(backgrounds: List<Int>) = apply { this.backgrounds = backgrounds }
        fun setBackgrounds(vararg backgrounds: Int) = apply { this.backgrounds = backgrounds.toList() }
        fun setSelectedBackground(resId: Int?) = apply { this.selectedBackgroundResId = resId }
        fun setEnableGalleryPick(enable: Boolean, onGalleryClick: (() -> Unit)? = null) = apply {
            this.enableGalleryPick = enable
            this.galleryListener = onGalleryClick
        }
        fun setLockProvider(provider: ItemLockProvider) = apply { this.lockProvider = provider }
        fun setLockedBackgrounds(vararg resIds: Int) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockBackgrounds(*resIds)
        }
        fun setLockedBackgrounds(resIds: Collection<Int>) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockBackgrounds(resIds)
        }
        fun setLockGallery(lock: Boolean = true) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockGallery(lock)
        }
        fun setOnLockedItemClicked(listener: (LockableItem, unlock: () -> Unit) -> Unit) = apply {
            this.lockedItemClickListener = listener
        }
        fun setOnImageSelected(listener: (Int) -> Unit) = apply { this.imageListener = listener }
        fun setOnColorSelected(listener: (Int) -> Unit) = apply { this.colorListener = listener }
        fun setOnGalleryClick(listener: () -> Unit) = apply {
            this.enableGalleryPick = true
            this.galleryListener = listener
        }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): ImageSelectorDialog {
            val dialog = ImageSelectorDialog(context)
            dialog.setBackgroundsList(backgrounds)
            dialog.setSelectedBackground(selectedBackgroundResId)
            dialog.setEnableGalleryPick(enableGalleryPick, galleryListener)
            dialog.setLockProvider(lockProvider)
            lockedItemClickListener?.let { dialog.setOnLockedItemClickListener(it) }

            dialog.setImageSelectedListener(object : ImagePickerListener {
                override fun onImageSelected(imageResource: Int) {
                    imageListener?.invoke(imageResource)
                }

                override fun onColorSelected(color: Int) {
                    colorListener?.invoke(color)
                }
            })
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
