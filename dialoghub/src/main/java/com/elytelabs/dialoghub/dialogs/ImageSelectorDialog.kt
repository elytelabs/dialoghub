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
import com.elytelabs.dialoghub.adapters.ImageAdapter
import androidx.core.graphics.drawable.toDrawable

/**
 * Dialog for selecting background drawable images or picking custom background colors.
 */
class ImageSelectorDialog(private val context: Context) {

    private var backgrounds: List<Int> = emptyList()
    private var imagePickerListener: ImagePickerListener? = null
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
     * Sets the list of background drawable resource IDs.
     */
    fun setBackgroundsList(backgrounds: List<Int>) {
        this.backgrounds = backgrounds
    }

    /**
     * Convenience method to show the image selector dialog using Kotlin lambda callbacks.
     *
     * @param backgrounds Optional list of drawable resource IDs.
     * @param onImageSelected Callback invoked when a background image is chosen.
     * @param onColorSelected Callback invoked when a color is chosen from the color picker.
     */
    fun show(
        backgrounds: List<Int>? = null,
        onImageSelected: ((imageResId: Int) -> Unit)? = null,
        onColorSelected: ((color: Int) -> Unit)? = null
    ) {
        if (backgrounds != null) {
            this.backgrounds = backgrounds
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

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image_selector, null)
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.imageRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        val adapter = ImageAdapter()
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageView>(R.id.backButton).setOnClickListener {
            dialog.dismiss()
        }

        adapter.setOnImageClickListener { imageResource ->
            imagePickerListener?.onImageSelected(imageResource)
            dialog.dismiss()
        }

        adapter.setOnColorPickerClickListener {
            // Show the color picker dialog
            colorPickerDialog.setColorSelectedListener { color ->
                imagePickerListener?.onColorSelected(color)
            }

            colorPickerDialog.showColorPickerDialog()
            dialog.dismiss()
        }

        adapter.setBackgrounds(backgrounds)

        dialog.show()
    }
}