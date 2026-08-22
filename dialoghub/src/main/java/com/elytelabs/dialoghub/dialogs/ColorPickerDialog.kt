package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.SeekBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.ColorAdapter
import com.elytelabs.toolbox.ColorGenerator
import androidx.core.graphics.drawable.toDrawable

/**
 * Dialog for selecting colors from a predefined palette with customizable transparency.
 */
class ColorPickerDialog(private val context: Context) {

    private var colorPickerListener: ColorPickerListener? = null

    /**
     * Traditional interface listener for Java/Kotlin interoperability.
     */
    fun interface ColorPickerListener {
        fun onColorSelected(color: Int)
    }

    /**
     * Sets the color picker listener using the traditional interface.
     */
    fun setColorSelectedListener(listener: ColorPickerListener) {
        this.colorPickerListener = listener
    }

    /**
     * Convenience method to show the color picker dialog using a Kotlin lambda callback.
     *
     * @param onColorSelected Lambda invoked when a color is picked.
     */
    fun show(onColorSelected: (color: Int) -> Unit) {
        this.colorPickerListener = ColorPickerListener { color -> onColorSelected(color) }
        showColorPickerDialog()
    }

    /**
     * Displays the color picker dialog.
     */
    fun showColorPickerDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null)
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.colorRecyclerView)
        val transparencySeekBar = dialogView.findViewById<SeekBar>(R.id.transparencySeekBar)
        recyclerView.layoutManager = GridLayoutManager(context, 5)
        val adapter = ColorAdapter()
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageView>(R.id.colorBackButton).setOnClickListener {
            dialog.dismiss()
        }

        adapter.setOnItemClickListener { color ->
            val transparency = transparencySeekBar.progress
            val transparentColor = Color.argb(
                transparency,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
            colorPickerListener?.onColorSelected(transparentColor)
            dialog.dismiss()
        }

        adapter.setColors(ColorGenerator.getColorList())

        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                adapter.setTransparency(progress)
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialog.show()
    }
}