package com.elytelabs.dialoghub.dialogs

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

class ImageSelectorDialog(private val context: Context) {

    private var backgrounds: List<Int> = emptyList()
    private var imagePickerListener: ImagePickerListener? = null


    private val colorPickerDialog = ColorPickerDialog(context)


    interface ImagePickerListener {
        fun onImageSelected(imageResource: Int)
        fun onColorSelected(color: Int)
    }

    fun setImageSelectedListener(listener: ImagePickerListener) {
        this.imagePickerListener = listener
    }

    fun setBackgroundsList(backgrounds: List<Int>) {
        this.backgrounds = backgrounds
    }

    fun showImageSelectionDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image_selector, null)
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
            colorPickerDialog.setColorSelectedListener(object : ColorPickerDialog.ColorPickerListener {
                override fun onColorSelected(color: Int) {
                    imagePickerListener?.onColorSelected(color)
                }
            })

            colorPickerDialog.showColorPickerDialog()
            dialog.dismiss()
        }

        adapter.setBackgrounds(backgrounds)

        dialog.show()
    }

}