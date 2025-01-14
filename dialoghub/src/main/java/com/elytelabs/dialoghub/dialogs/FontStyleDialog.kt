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
import com.elytelabs.dialoghub.adapters.FontStyleAdapter

class FontStyleDialog(private val context: Context) {

    private var fonts: List<Int> = emptyList()
    private var fontPickerListener: FontPickerListener? = null


    interface FontPickerListener {
        fun onFontSelected(font: Int)
    }

    fun setFontSelectedListener(listener: FontPickerListener) {
        this.fontPickerListener = listener
    }

    fun setFontsList(fonts: List<Int>) {
        this.fonts = fonts
    }


    fun showFontSelectionDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_font_selector, null)
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.fontRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        val adapter = FontStyleAdapter(context)
        recyclerView.adapter = adapter

        dialogView.findViewById<ImageView>(R.id.backButton).setOnClickListener {
            dialog.dismiss()
        }

        adapter.setOnFontClickListener {
            fontPickerListener?.onFontSelected(it)
            dialog.dismiss()
        }

        adapter.setFonts(fonts)

        dialog.show()

    }
}