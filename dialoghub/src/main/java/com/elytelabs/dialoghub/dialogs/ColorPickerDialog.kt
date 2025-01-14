package com.elytelabs.dialoghub.dialogs

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

class ColorPickerDialog(private val context: Context) {

    private var colorPickerListener: ColorPickerListener? = null

    fun setColorSelectedListener(listener: ColorPickerListener) {
        this.colorPickerListener = listener
    }

    interface ColorPickerListener {
        fun onColorSelected(color: Int)
    }

    fun showColorPickerDialog() {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null)
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
            val transparentColor = Color.argb(transparency, Color.red(color), Color.green(color), Color.blue(color))
            colorPickerListener?.onColorSelected(transparentColor)
            dialog.dismiss()
        }

        adapter.setColors(ColorGenerator.getColorList())

        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                       adapter.setTransparency(progress)
                       adapter.notifyDataSetChanged()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialog.show()
    }
}